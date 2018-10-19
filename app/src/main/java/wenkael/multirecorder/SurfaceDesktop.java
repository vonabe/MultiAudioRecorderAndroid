package wenkael.multirecorder;

import android.app.VoiceInteractor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wenkael™ on 10.11.2015.
 */
public class SurfaceDesktop extends AppCompatActivity implements Runnable {


    private ImageView   image;
    private SeekBar    seekbar;
    private Switch      switcher;
    private TextView    textView;
    private Button btnConnect;
    private SOCKET     socket;
    private Bitmap     bitmap;
    private UDPOld        udp;
    private Thread     thread;
    private boolean    status;
    private Spinner   spinSiseNpS;
    private ArrayList<String> arrayNpS = new ArrayList<>();
    private int sizepackage;
    private int sizeReceive;

    private boolean UDPorTCP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desktop_surface);

        btnConnect = (Button) findViewById(R.id.btnConnectDesktop);
        image = (ImageView) findViewById(R.id.imgDesktopView);
        seekbar = (SeekBar) findViewById(R.id.seekBar2);
        textView = (TextView) findViewById(R.id.textView3);
        spinSiseNpS = (Spinner) findViewById(R.id.spinDesktopSizeNpS);
        switcher    = (Switch) findViewById(R.id.switch1);

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UDPorTCP = isChecked;
            }
        });

        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache(true);

        String[] array = new String[]{
                "NpS_IMG_MIN    - "+NpS.IMAGE_SIZE_MIN,
                "NpS_IMG_NORMAL - "+NpS.IMAGE_SIZE_NORMAL,
                "NpS_IMG_BIG    - "+NpS.IMAGE_SIZE_BIG,
                "NpS_IMG_MAX    - "+NpS.IMAGE_SIZE_MAX
        };
        final int[] arrayVALUE = {NpS.IMAGE_SIZE_MIN,NpS.IMAGE_SIZE_NORMAL,NpS.IMAGE_SIZE_BIG,NpS.IMAGE_SIZE_MAX};

//        arrayIPaddress.addAll(Arrays.asList(array));

        spinSiseNpS = (Spinner)findViewById(R.id.spinDesktopSizeNpS);
        arrayNpS.addAll(Arrays.asList(array));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayNpS) {};
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinSiseNpS.setAdapter(adapter);
        spinSiseNpS.setPrompt("Select NpS_IMG");
        spinSiseNpS.setSelection(0);

        sizeReceive = arrayVALUE[1];

        spinSiseNpS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (udp != null) udp.setPackageSize(arrayVALUE[position]);
                sizeReceive = arrayVALUE[position];
                System.out.println("***selectNpS: " + arrayVALUE[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
//        spinSiseNpS.setEnabled(false);

        seekbar.setMax(1_000_000);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sizepackage = progress + 1000;
                textView.setText(String.valueOf(sizepackage));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbar.setProgress(1_000);

        image.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeMove(float x, float y) {
                if(socket==null)return;
                JSONArray json = new JSONArray();
                JSONObject obj = new JSONObject();
                json.put("move");
                try {
                    obj.put("x",x);
                    obj.put("y",y);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                json.put(obj);
                socket.send(json.toString().getBytes());
            }
            @Override
            public void setZoom(float zoom) {
                if(socket==null)return;
                JSONArray json = new JSONArray();
                JSONObject obj = new JSONObject();
                json.put("zoom");
                try {
                    obj.put("factor", zoom);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                json.put(obj);
                socket.send(json.toString().getBytes());
            }

        } );
    }

    public void connect(View view){
        if(status==false) {
            status = true;
            thread = new Thread(this);
            thread.start();
        }else {
            status = false;
            thread.interrupt();
            if(socket!=null)socket.dispose();
            if(udp!=null)udp.destroy();
            socket = null;udp = null;
        }
    }

    private Handler handler = new Handler();
    private void updateImage(final Bitmap bitmap){
        handler.post(new Runnable() {
            @Override
            public void run() {
                image.setImageBitmap(bitmap);
//                image.invalidate();
                if(!bitmap.isRecycled()){bitmap.recycle();};
            }
        });
    }

    @Override
    public void run() {
//        socket = new SOCKET(MainActivity.IP, 1488);
        if(!MainActivity.SERVER_ON_CLIENT){
            if(UDPorTCP) {
                socket = new SOCKET(1488);
                socket.accept();
            }else {
                udp = new UDPOld(1488);
                udp.bind();
                udp.setPackageSize(NpS.IMAGE_SIZE_MIN);
                udp.receive();
                udp.transactionAccept();
            }
        } else {
            if(UDPorTCP) {
                socket = new SOCKET(MainActivity.IP, 1488);
                socket.connect();
            }else {
                udp = new UDPOld(MainActivity.IP, 1488);
                udp.setPackageSize(NpS.IMAGE_SIZE_MIN);
                udp.transactionConnect();
                udp.send("wenkael".getBytes());
            }
        }
//         if(!socket.connect()){status = false;return;}

         while(status){

//            int size = 11000;
//            String inSize = null;
//            try{
//                byte[] si = socket.receive(18);
//                inSize = new String(new String(si).getBytes("windows-1252"), "windows-1251").trim();
//                inSize = inSize.substring(inSize.indexOf("[")+1, inSize.lastIndexOf("]"));
//                size = Integer.parseInt(inSize);
//            }catch(Exception ex){System.out.println("error debug: damaged file, size: "+inSize);}

            byte[] buffer = null;
            try {
//                buffer = socket.receive(size);
//                buffer = udp.receive();
//                buffer = socket.receive(sizeReceive);
                Bitmap bitlocal = null;
                try {
                    bitlocal = BitmapFactory.decodeStream( (UDPorTCP)?socket.getInput() : udp.getInput());
                }catch (OutOfMemoryError e){System.err.println("OutOfMemoryError decodeStream");}

                if(bitlocal!=null)bitmap=bitlocal;

//                if(buffer!=null) {
//                    try {
//                      bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
//                        BitmapFactory.decodeStream(socket.getInput());
//                    }catch (Exception ex){System.err.println("error decodeByteArray");}

                    if(bitmap!=null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                image.setImageBitmap(bitmap);
                            }
                        });
                    }

//                }else{System.out.println("error buffer null!");}
            }catch (Exception ex){System.out.println("error decode bitmap! buffer -> " + buffer);if(bitmap!=null)bitmap.recycle();}
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        status = false;
        if(bitmap!=null)bitmap.recycle();
        if(socket!=null)socket.dispose();
        if(udp!=null)udp.destroy();
        socket = null;
        udp = null;
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
