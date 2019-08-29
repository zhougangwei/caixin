package ily.xieqing.com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import ily.xieqing.com.myapplication.mms.MmsException;
import ily.xieqing.com.myapplication.mms.NotificationInd;
import ily.xieqing.com.myapplication.mms.PduParser;
import ily.xieqing.com.myapplication.mms.PduPersister;

import static ily.xieqing.com.myapplication.MmsSmsReceiver2.MMS_RECEIVE_ACTION;

public class MMSReceiver extends BroadcastReceiver {
    private NotificationInd mNotificationInd;
    private String mContentLocation;
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO Auto-generated method stub
        this.context=context;
        byte[] header = intent.getByteArrayExtra("header");
        int transactionId = intent.getIntExtra("transactionId",0);
        String action = intent.getAction();

        //彩信
        if(action.equals(MMS_RECEIVE_ACTION)){
        PduParser parser = new PduParser(intent.getByteArrayExtra("data"),false);



            mUri = Uri.parse(uriString);

            try {
                mNotificationInd = (NotificationInd)
                        PduPersister.getPduPersister(context).load(mUri);
            } catch (MmsException e) {
                throw new IllegalArgumentException();
            }

            mContentLocation = new String(mNotificationInd.getContentLocation());
            mId = mContentLocation;




    }
    protected byte[] getPdu(String url) throws IOException {
        //    ensureRouteToHost(url, mTransactionSettings);
            return HttpUtils.httpConnection(
                                    context, -1L,
                                    url, null, HttpUtils.HTTP_GET_METHOD,
                                    true,
                   "10.0.0.172",
                    80);
        }

}
