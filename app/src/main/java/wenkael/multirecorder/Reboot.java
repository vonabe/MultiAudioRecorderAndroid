package wenkael.multirecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import wenkael.multirecorder.MyService;

/**
 * Created by veniamin on 29.07.2016.
 */
public class Reboot extends BroadcastReceiver{
    final private String LOG_TAG = "MyReboot";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive " + intent.getAction());
        context.startService(new Intent(context, MyService.class));
    }

}
