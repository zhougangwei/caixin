package ily.xieqing.com.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SmsService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
