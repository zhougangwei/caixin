package ily.xieqing.com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import ily.xieqing.com.myapplication.mms.InvalidHeaderValueException;
import ily.xieqing.com.myapplication.mms.PduHeaders;
import ily.xieqing.com.myapplication.mms.PduParser;

import static ily.xieqing.com.myapplication.MmsSmsReceiver2.MMS_RECEIVE_ACTION;

public class MMSReceiver extends BroadcastReceiver {

    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO Auto-generated method stub
        this.context=context;
        String action = intent.getAction();

        //彩信
        if(action.equals(MMS_RECEIVE_ACTION)){
        PduParser parser = new PduParser();

        try {
             PduHeaders headers = parser.parseHeaders(intent.getByteArrayExtra("data"));

             TransactionId = headers.getTransactionId();
             if (headers.getMessageType() == PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND) {
            //号码获取
            String from = headers.getFrom();
            final String content_location = headers.getContentLocation();
            if (content_location != null) {
                 new Thread() {
                public void run() {
                MmsConnect mmsConnect = new MmsContent(context,content_location,TransactionId);
                 try {
                      mmsConnect.connect();
                     } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();}
                                         }
                  }.start();
            }
             }

        } catch (InvalidHeaderValueException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();}
                  //广播不在发送
        abortBroadcast();
        }





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
