package ily.xieqing.com.myapplication;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Bundle bundle = intent.getExtras();
            if (bundle!=null){
                Object[] smsObj = (Object[]) bundle.get("pdus");
                for (Object o : smsObj){
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) o);
                    Date date = new Date(smsMessage.getTimestampMillis());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Toast.makeText(context,smsMessage.getOriginatingAddress()+"-"+smsMessage.getMessageBody()+"-"+simpleDateFormat.format(date),Toast.LENGTH_SHORT).show();
                }
            }

            //Toast.makeText(context,getSmsMessage(context),Toast.LENGTH_SHORT).show();
        }
    }


    public String getSmsMessage(Context context) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[] {"_id", "address", "person","body", "date", "type" };
        Cursor cur = cr.query(Uri.parse("content://sms/"), projection, null, null, "date desc");
        if (null == cur) {
            return "null";
        }
        while(cur.moveToNext()) {
            String _id = cur.getString(cur.getColumnIndex("_id"));//_id
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));//短信内容

            String date = cur.getString(cur.getColumnIndex("date"));//日期

            String type = cur.getString(cur.getColumnIndex("type"));//类型
            //cr.delete(Uri.parse("content://sms/"+_id),null,null);//立马删除
            return _id+","+number+","+name+","+body+","+date+","+type;
        }
        return "NULL("+cur.getCount()+")";
    }
}
