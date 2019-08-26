package ily.xieqing.com.myapplication;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Logger;

import ily.xieqing.com.myapplication.mms.GenericPdu;
import ily.xieqing.com.myapplication.mms.NotificationInd;
import ily.xieqing.com.myapplication.mms.PduBody;
import ily.xieqing.com.myapplication.mms.PduParser;
import ily.xieqing.com.myapplication.mms.PduPart;
import ily.xieqing.com.myapplication.mms.RetrieveConf;

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

        if (SMS_RECEIVE_ACTION.equals(intent.getAction())) {
            Log.e("duanxin", "Context");
        } else {
            Log.e("duanxin", "MmsSmsReceiver2");
            final String SMS_URI_MMS = "content://mms"; //此处查询的表是pdu表
            ContentResolver MMScr = context.getContentResolver();
            Uri MMSuri = Uri.parse(SMS_URI_MMS);
            Cursor MMScursor = null;
            MMScursor = MMScr.query(MMSuri, null, null, null, null);//查出所有彩信
            if (MMScursor == null) {
                return;
            }
            if (!MMScursor.moveToFirst()) {//跳转到第一行，如果失败跳出方法，成功则进入do while循环
                return;
            }
            do {
                JSONObject jsonObject = new JSONObject();
                String id = MMScursor.getString(MMScursor.getColumnIndex("_id"));// 获取pdu表里 彩信的id
                String phonenumber = getAddressNumber(context, id);

                int timess = MMScursor.getInt(MMScursor.getColumnIndex("date"));
                long timesslong = (long) timess * 1000;//彩信获取的时间是以秒为单位的。
                Date d = new Date(timesslong);
                // String date = dateFormat.format(d);
                String sub = MMScursor.getString(MMScursor.getColumnIndex("sub"));//获取彩信主题
                try {
                    if (!TextUtils.isEmpty(sub)) {
                        sub = new String(sub.getBytes("ISO8859_1"), "UTF-8");
                    } else {
                        sub = " ";
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                final String SMS_URI_MMS_PART = "content://mms/part"; //从part表 获取彩信详情
                String selectionPart = "mid=" + id;// part表mid字段即为 pdu表 _id 字段
                ContentResolver Scr = context.getContentResolver();
                //String[]        projection = new String[]{"_id", "address", "person", "body", "date", "type","protocol"};
                Uri uri = Uri.parse(SMS_URI_MMS_PART);
                Cursor cursor = Scr.query(uri, null, selectionPart, null, null); //查询 part 指定mid的数据
                if (cursor == null) {
                    continue;
                } else {
                    if (cursor.moveToFirst()) {//游标移至第一行成功 则进入do while 循环 一个mid查询出来的结果可能是多条，彩信的结构也
                        //是多层的 有点类似html的结构

                        do {
                            String type = cursor.getString(cursor.getColumnIndex("ct"));
                            //part表 ct字段 标识 此part内容类型，彩信始末：application/smil；如果是文本附件：text/plain；
                            //图像附件：jpg：image/jpeg，gif：image/gif；音频附件：audio/mpeg
                            if ("text/plain".equals(type)) {
                                String data = cursor.getString(cursor.getColumnIndex("_data"));
                                // 当此part类型为文本附件时，通过_data拿到文本附件地址
                                String body;//彩信文本
                                if (data != null) {//附件地址不为空
                                    // implementation of this method below
                                    String partId = cursor.getString(cursor.getColumnIndex("_id"));
                                    body = getMmsText(context, partId);
                                    try {
                                        jsonObject.putOpt("content", sub + body);
                                        //因为要存数据库 删掉所有emoji
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {//附件地址为空时通过text获取文本
                                    //如果是彩信始末，为彩信的SMIL内容；如果是文本附件，为附件内容；如果是视频、音频附件，text为空
                                    body = cursor.getString(cursor.getColumnIndex("text"));
                                    try {
                                        jsonObject.putOpt("content", sub + body);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }else if("image/jpeg".equals(type)){
                                String data = cursor.getString(cursor.getColumnIndex("_data"));
                                // 当此part类型为文本附件时，通过_data拿到文本附件地址
                                Bitmap body;//彩信文本
                                if (data != null) {//附件地址不为空
                                    // implementation of this method below
                                    String partId = cursor.getString(cursor.getColumnIndex("_id"));
                                    body = getMmsImage(context, partId);
                                    try {
                                        jsonObject.putOpt("content", sub + body);
                                        //因为要存数据库 删掉所有emoji
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {//附件地址为空时通过text获取文本

                                }
                            }
                        } while (cursor.moveToNext());
                    }
                }
                Log.e("duanxin", "phonenumber" + phonenumber + "彩信=" + jsonObject.toString());
            } while (MMScursor.moveToNext());
        }
        //广播不在发送
        abortBroadcast();
    }



    private static String getAddressNumber(Context context, String id) {
        //此处id 也是pdu表的_id字段
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = context.getContentResolver().query(uriAddress, null,
                null, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        return name;
    }

    private Bitmap getMmsImage(Context context, String _id) { //读取图片附件
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = context.getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "读取图片异常" + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.v(TAG, "读取图片异常" + e.getMessage());
                }
            }
        }
        return bitmap;
    }
    private static String getMmsText(Context context, String id) {
        //此处id 为part表的_id 字段
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            try {
                is = context.getContentResolver().openInputStream(partURI);
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }
}
