package ily.xieqing.com.myapplication.transaction;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.provider.Telephony;
import android.util.Log;

import ily.xieqing.com.myapplication.mms.ContentType;
import ily.xieqing.com.myapplication.mms.DeliveryInd;
import ily.xieqing.com.myapplication.mms.GenericPdu;
import ily.xieqing.com.myapplication.mms.MmsException;
import ily.xieqing.com.myapplication.mms.NotificationInd;
import ily.xieqing.com.myapplication.mms.PduHeaders;
import ily.xieqing.com.myapplication.mms.PduParser;
import ily.xieqing.com.myapplication.mms.PduPersister;
import ily.xieqing.com.myapplication.mms.ReadOrigInd;
import ily.xieqing.com.myapplication.mms.SqliteWrapper;

import static android.provider.Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;
import static ily.xieqing.com.myapplication.mms.PduHeaders.MESSAGE_TYPE_DELIVERY_IND;
import static ily.xieqing.com.myapplication.mms.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;

public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiver";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private class ReceivePushTask extends AsyncTask<Intent,Void,Void> {
        private Context mContext;
        public ReceivePushTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Intent intent = intents[0];

            // Get raw PDU push-data from the message and parse it
            byte[] pushData = intent.getByteArrayExtra("data");
            PduParser parser = new PduParser(pushData,false);
            GenericPdu pdu = parser.parse();

            if (null == pdu) {
                Log.e(TAG, "Invalid PUSH data");
                return null;
            }

            PduPersister p = PduPersister.getPduPersister(mContext);
            ContentResolver cr = mContext.getContentResolver();
            int type = pdu.getMessageType();
            long threadId = -1;

            try {
                switch (type) {

                    case MESSAGE_TYPE_NOTIFICATION_IND: {
                        NotificationInd nInd = (NotificationInd) pdu;

                        if (MmsConfig.getTransIdEnabled()) {
                            byte [] contentLocation = nInd.getContentLocation();
                            if ('=' == contentLocation[contentLocation.length - 1]) {
                                byte [] transactionId = nInd.getTransactionId();
                                byte [] contentLocationWithId = new byte [contentLocation.length
                                                                          + transactionId.length];
                                System.arraycopy(contentLocation, 0, contentLocationWithId,
                                        0, contentLocation.length);
                                System.arraycopy(transactionId, 0, contentLocationWithId,
                                        contentLocation.length, transactionId.length);
                                nInd.setContentLocation(contentLocationWithId);
                            }
                        }

                        if (!isDuplicateNotification(mContext, nInd)) {
                            // Save the pdu. If we can start downloading the real pdu immediately,
                            // don't allow persist() to create a thread for the notificationInd
                            // because it causes UI jank.
                            Uri uri = p.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI,
                                    !NotificationTransaction.allowAutoDownload(),
                                    true,
                                    null);

                            // Start service to finish the notification transaction.
                            Intent svc = new Intent(mContext, TransactionService.class);
                            svc.putExtra(TransactionBundle.URI, uri.toString());
                            svc.putExtra(TransactionBundle.TRANSACTION_TYPE,
                                    Transaction.NOTIFICATION_TRANSACTION);
                            mContext.startService(svc);
                        } else if (LOCAL_LOGV) {
                            Log.v(TAG, "Skip downloading duplicate message: "
                                    + new String(nInd.getContentLocation()));
                        }
                        break;
                    }
                    default:
                        Log.e(TAG, "Received unrecognized PDU.");
                }
            } catch (MmsException e) {
                Log.e(TAG, "Failed to save the data from PUSH: type=" + type, e);
            } catch (RuntimeException e) {
                Log.e(TAG, "Unexpected RuntimeException.", e);
            }

            if (LOCAL_LOGV) {
                Log.v(TAG, "PUSH Intent processed.");
            }

            return null;
        }
    }
    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WAP_PUSH_DELIVER_ACTION)
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Received PUSH Intent: " + intent);
            }
            // Hold a wake lock for 5 seconds, enough to give any
            // services we start time to take their own wake locks.
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                            "MMS PushReceiver");
            wl.acquire(5000);
            new ReceivePushTask(context).execute(intent);
        }
    }



    private static boolean isDuplicateNotification(
            Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String location = new String(rawLocation);
            String selection = Telephony.Mms.CONTENT_LOCATION + " = ?";
            String[] selectionArgs = new String[] { location };
            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI, new String[] { Telephony.Mms._ID },
                    selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        // We already received the same notification before.
                        return true;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }
}