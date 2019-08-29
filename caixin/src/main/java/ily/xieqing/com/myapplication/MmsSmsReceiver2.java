package ily.xieqing.com.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.provider.Telephony.Carriers.MMSC;

public class MmsSmsReceiver2 extends BroadcastReceiver {
    /**
     * 接收短信
     */
    public static final String SMS_RECEIVE_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    /**
     * 接收彩信
     */
    public static final String MMS_RECEIVE_ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    public static long date = 0;

    private String TAG = "MmsSmsReceiver";


    @Override
    public void onReceive(final Context context, Intent intent) {


        UpService.startActionFoo(context,"1","2");



        //  mHttpBox.read();
        //  mmsData  = mHttpBox.getInData();
        //广播不在发送
         abortBroadcast();
    }

    /*NotifyRespInd notifyRespInd = new NotifyRespInd(
            PduHeaders.CURRENT_MMS_VERSION,
            TransactionId, PduHeaders.STATUS_RETRIEVED);

    mHttpBox = new HttpBox(MMSC, new PduComposer(context,notifyRespInd).make());

        mHttpBox.setConnectTimeout(50 * 1000);
        mHttpBox.setReadTimeout(30 * 1000);
        mHttpBox.setRequestMethod(true);*/

       // mHttpBox.addHeader("User-Agent","Nokia6120c/4.21 (SymbianOS/9.2; U; Series60/3.1 Nokia6120c/4.21; Profile/MIDP-2.0 Configuration/CLDC-1.1 ) Mozilla/5.0 AppleWebK");
       // mHttpBox.addHeader("Accept","*/*, application/vnd.wap.mms-message, application/vnd.wap.sic");
       //mHttpBox.addHeader("Content-Type","application/vnd.wap.mms-message");
       // mHttpBox.addHeader("Accept-Charset","iso-8859-1, utf-8; q=0.7, *; q=0.7");
        //mHttpBox.addHeader("Accept-Language","zh-cn, zh;q=1.0,en;q=0.5");

       // mHttpBox.connect();

}
