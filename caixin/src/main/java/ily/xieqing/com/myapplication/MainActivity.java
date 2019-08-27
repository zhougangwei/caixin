package ily.xieqing.com.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;
import static android.content.Intent.ACTION_USER_PRESENT;

public class MainActivity extends AppCompatActivity {

   private SMSRecevier smsRecevier = new SMSRecevier();
   private MmsSmsReceiver2 smsRecevier2 = new MmsSmsReceiver2();
    private static String[] PERMISSIONS_CAMERA_AND_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
       };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //需要将本应用设置为默认短信  才能删除
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        //8.0以上，动态注册广播
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsRecevier,intentFilter);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.MMS_RECEIVED");
            intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
            registerReceiver(smsRecevier2,intentFilter);
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int storagePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //检测是否有权限，如果没有权限，就需要申请
            if (storagePermission != PackageManager.PERMISSION_GRANTED ) {
                //申请权限
                requestPermissions(PERMISSIONS_CAMERA_AND_STORAGE, 1);
                //返回false。说明没有授权
                return ;
            }
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsRecevier);
        unregisterReceiver(smsRecevier2);

    }
}
