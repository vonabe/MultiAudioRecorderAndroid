package wenkael.multirecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static Context context;

    public static AudioTrack track;
    public static Visualizer visualizer = null;
    public static VisualizerView visualizerView;
    public static Equalizer equalizer;
    public static LinearLayout mLinearLayout;

    private Button btnServerOnClient;
    public static boolean SERVER_ON_CLIENT = false;
    private Button addIPBTN;
    private ImageButton btnSearch;
    private EditText  addIP;
    private TextView   myIP;
    private ArrayList<String> arrayIPaddress = new ArrayList<>();

    private static int   NpSSize;
    private ArrayAdapter adapter;
    private Spinner      spinner;
    private Spinner      spinNpS;
    public static Net net = null;
    public static int PROGRESS = 1024;
    public static TextView txtSeek;
    public static String        IP, wifi_name;

    private static Broadcast     broadcast;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

//        Лечение каких-то вылетов!
//        AudioManager am = (AudioManager)this.context.getSystemService(Context.AUDIO_SERVICE);
//        am.setMode(AudioManager.MODE_NORMAL);
//        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        addIPBTN = (Button) findViewById(R.id.btnAddIP);
        addIP = (EditText) findViewById(R.id.addAddress);
        myIP = (TextView) findViewById(R.id.MYIPaddress);
        btnServerOnClient = (Button) findViewById(R.id.btnSeverClient);
        btnSearch = (ImageButton) findViewById(R.id.btnSearchServer);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        int requestCode = 0;
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, requestCode);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            myIP.setText(Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress()));
            wifi_name = getWifiName(context);
        }else{

        }

//        addIP.requestFocus();
        addIP.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)addIP.setText("");else addIP.setText("add IP address");
            }
        });

//        btnCamera.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Intent intent = new Intent(MainActivity.this, SurfaceCamera.class);
//                startActivity(intent);
//                return false;
//            }
//        });

//        final ImageView view = (ImageView) findViewById(R.id.imageView2);
//        final Oscillogram oscillogram = new Oscillogram(view, view.getLayoutParams().width, view.getLayoutParams().height);
//        view.setImageDrawable(oscillogram);
//
//        final android.os.Handler handler = new android.os.Handler();
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        oscillogram.setSize(view.getWidth(), view.getHeight());
//                        view.invalidate();
//                    }
//                });
//            }
//        };
//
//        Timer timer = new Timer();
//        timer.schedule(task, 100, 100);



//        track = new AudioTrack.Builder()
//                .setAudioAttributes(new AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ALARM)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build())
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                        .setSampleRate(441000)
//                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
//                        .build())
//                .setBufferSizeInBytes(1024)
//                .build();

//        track.setStereoVolume(1F, 1F);


        track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                1024,
                AudioTrack.MODE_STREAM
        );

        createVisualizer();

        visualizerView = new VisualizerView(this);
        visualizerView.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        (int) (150 * getResources().getDisplayMetrics().density))
        );
        visualizerView.setBackgroundColor(Color.GRAY);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relative);
        layout.addView(visualizerView);

        mLinearLayout = new LinearLayout(this);
        mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);

        spinNpS = (Spinner) findViewById(R.id.spinNpS);
        String[] arrayNpS = new String[]{
                "NpS_AUDIO_MIN         - "+NpS.AUDIO_SIZE_MIN,
                "NpS_AUDIO_NORMAL      - "+NpS.AUDIO_SIZE_NORMAL,
                "NpS_AUDIO_STANDART    - "+NpS.AUDIO_SIZE_STANDART
        };
        final int[] arrayVALUE = {NpS.AUDIO_SIZE_MIN,NpS.AUDIO_SIZE_NORMAL,NpS.AUDIO_SIZE_STANDART};

        ArrayAdapter<String> adapterNpS = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayNpS) {};
        adapterNpS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinNpS.setAdapter(adapterNpS);
        spinNpS.setPrompt("Select NpS_Audio");
        spinNpS.setSelection(0);
        spinNpS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (udp != null) udp.setPackageSize(arrayVALUE[position]);
                NpSSize = arrayVALUE[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        String [] array = new String[]{
                "192.168.0.100", "192.168.0.101", "192.168.0.102",
                "192.168.0.103", "192.168.0.104", "192.168.0.105",
                "127.0.0.1"};

        arrayIPaddress.addAll(Arrays.asList(array));
        spinner = (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayIPaddress) {};
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select IP");
        track.play();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if(position>0){
                dispose();
                IP = spinner.getSelectedItem().toString();
                net = new Net(IP);
                net.start();
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                toast("nothing: "+spinner.getSelectedItem().toString());
            }
        });

//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                toast("itemclick: "+spinner.getSelectedItem().toString());
//            }
//        });



//        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        boolean equals = false;
//        for(ActivityManager.RunningServiceInfo in : rs) {
//            String name = in.service.getClassName();
//            if (in.started)
//                adapter.add(name);
//            if(name.equalsIgnoreCase(MyService.class.getName()))equals=true;
//        }
//        System.out.println("SEVICE - "+equals);
//        if(!equals)startService(new Intent(this, MyService.class));

    }

    public String getWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            @SuppressLint("MissingPermission") WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return null;
    }


    public void addIP(View view){
        if(view.getId() == addIPBTN.getId()){
            if(!addIP.getText().toString().isEmpty())
            {
                String valid = addIP.getText().toString().trim();
                if(validate(valid)) {
                    arrayIPaddress.add(valid);
                    adapter.notifyDataSetChanged();
                }else{toast("invalid IP");}
            }
        }else if(view.getId() == btnServerOnClient.getId()){
            if(btnServerOnClient.getText().toString().toLowerCase().contains("server")){
                SERVER_ON_CLIENT = false;
                btnServerOnClient.setText("To CLIENT");
            } else {
                SERVER_ON_CLIENT = true;
                btnServerOnClient.setText("To SERVER");
            }
        }else if(view.getId()== btnSearch.getId()){
            broadcast = new Broadcast();
            btnSearch.setEnabled(false);
            Object[] obj = broadcast.search();

            if(obj!=null)
                for(Object o : obj){
                    if(validate(o.toString())){
                        arrayIPaddress.add(o.toString());
                        adapter.notifyDataSetChanged();
                        toast("add new server");
                    }
                }
            btnSearch.setEnabled(true);
        }
    }

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
            toast("Permission Granted");
        }
    }

    private void createVisualizer(){
        if(visualizer!=null){visualizer.release();visualizer = null;}

        boolean permission = false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            int requestCode = 0;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, requestCode);
//            if(requestCode==PackageManager.PERMISSION_GRANTED){
//                permission = true;
//            }
        }else{
            permission = true;
        }
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, requestCode);
//        if(requestCode == PackageManager.PERMISSION_GRANTED){
//            permission = true;
//        }
        if(permission) {
            visualizer = new Visualizer(track.getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    visualizerView.updateVisualizer(waveform);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    System.out.println("OUT::Fft");
                }

            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            visualizer.setEnabled(true);
        }
    }

    private void setupEqualizerFxAndUI() {
        // Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
        // with a default priority (0).
//        equalizer = new Equalizer(0, track.getAudioSessionId());
        equalizer.setEnabled(true);

        TextView eqTextView = new TextView(this);
        eqTextView.setText("Equalizer:");
        mLinearLayout.addView(eqTextView);

        short bands = equalizer.getNumberOfBands();

        final short minEQLevel = equalizer.getBandLevelRange()[0];
        final short maxEQLevel = equalizer.getBandLevelRange()[1];

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        for (short i = 0; i <bands; i++) {
            final short band = i;

            TextView freqTextView = new TextView(this);
            freqTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            freqTextView.setText((equalizer.getCenterFreq(band) / 1000) + " Hz");
            mLinearLayout.addView(freqTextView);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView minDbTextView = new TextView(this);
            minDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            minDbTextView.setText((minEQLevel / 100) + " dB");

            TextView maxDbTextView = new TextView(this);
            maxDbTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            maxDbTextView.setText((maxEQLevel / 100) + " dB");

            SeekBar bar = new SeekBar(this);
            layoutParams.weight = 1;
            bar.setLayoutParams(layoutParams);
            bar.setMax(maxEQLevel - minEQLevel);
            bar.setProgress(equalizer.getBandLevel(band));

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    equalizer.setBandLevel(band, (short) (progress + minEQLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            row.addView(minDbTextView);
            row.addView(bar);
            row.addView(maxDbTextView);

            mLinearLayout.addView(row);
        }
        SeekBar SeekBufferSize = new SeekBar(this);
        layoutParams.weight = 1;
        SeekBufferSize.setLayoutParams(layoutParams);
        SeekBufferSize.setMax(maxEQLevel - minEQLevel);
        SeekBufferSize.setProgress(1024);
        SeekBufferSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                PROGRESS = seekBar.getProgress();
                txtSeek.setText("1024");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        txtSeek = new TextView(this);
        txtSeek.setLayoutParams(layoutParams);
        txtSeek.setText("1024");

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
//        visualizer.release();
//        track.stop();
//        track.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(visualizer!=null) {
            visualizer.release();
            visualizer = null;
        }
//        track.stop();
//        track.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        createVisualizer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dispose();
        if(track!=null){track.stop();track.release();}track=null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Setting.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_setting)
        {
            Intent intent = new Intent(MainActivity.this, Main.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_camera1)
        {
            Intent intent = new Intent(MainActivity.this, SurfaceCamera.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_camera2)
        {
            Intent intent = new Intent(MainActivity.this, TestCameraUDP.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.action_desktop){
            Intent intent = new Intent(MainActivity.this, SurfaceDesktop.class);
            startActivity(intent);
        }
        else if(id == R.id.action_microphone){
            Intent intent = new Intent(MainActivity.this, MyMicrofon.class);
            startActivity(intent);
        }
        else if(id == R.id.action_testlayout){
            Intent intent = new Intent(MainActivity.this, TestActivitylayout.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isWriting;
    private UDPOld        udp;

    class Net extends Thread {

//        private ByteBuffer array_buffer = ByteBuffer.allocate(10_000);
        private String ip = null;

        public Net(String IP) {
            this.ip = IP;
            toast("START AUDIO THREAD");
            System.out.println("START AUDIO THREAD");
        }

        public void createServer(){
            if(track!=null){track.release();track=null;}
            track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    44100,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    1024,
                    AudioTrack.MODE_STREAM
            );
            track.play();
            createVisualizer();

            udp = new UDPOld(1488);
            udp.bind();
            udp.setPackageSize(NpSSize);
            toast("server 1488");
            udp.transactionAccept();

            broadcast = new Broadcast();
            broadcast.bind();
        }

        public void createClient(){
            if(track!=null){track.release();track=null;}
            track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    1024,
                    AudioTrack.MODE_STREAM
            );
            track.play();
            createVisualizer();

            udp = new UDPOld(ip, 1488);
            udp.setPackageSize(NpSSize);
//                udp.send("audiostream".getBytes());
//            udp.transactionConnect();
            JSONObject object = new JSONObject();
            try {
                object.put("name", wifi_name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            udp.sendFree(object.toString().getBytes());
            toast("client 1488");
            try {
                udp.setSoTimeOut(10_000);
            } catch (SocketException e) {
                dispose();
            }


        }

        @Override
        public void run() {

            isWriting = true;
            if(SERVER_ON_CLIENT){createClient();}else{createServer();}

            while (isWriting)
            {
                if(udp==null){System.out.println("***UDP - "+udp);return;}
                byte[] buffer = udp.receive();
                if(buffer!=null) {
//                    array_buffer.put(buffer);
                    track.write(buffer, 0, buffer.length);
//                    array_buffer.clear();
                }
                else{toast("ОШИБКА ПРИ ПОЛУЧЕНИИ ДАННЫХ!"); dispose();}
//                if(array_buffer.remaining()<1024)array_buffer.clear();
            }

        }

    }

    synchronized public void dispose(){
        isWriting = false;
        if(udp!=null){udp.destroy();}
        udp = null;
        net = null;
//        if(track!=null){track.stop();track.release();}track=null;
    }

    private static String message = null;
    private static android.os.Handler handler = new android.os.Handler();

    synchronized public static void toast(Object msg) {
        message = new String(msg.toString());
        handler.post(runnable_toast);
    }

    public static Runnable runnable_toast = new Runnable() {
        @Override
        public void run() {
            Toast t = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.BOTTOM, 0, 200);
            t.show();
        }
    };

}


