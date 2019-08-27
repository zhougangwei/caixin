package ily.xieqing.com.myapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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


        //广播不在发送
        abortBroadcast();
    }



}
