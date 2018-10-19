package wenkael.multirecorder;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by wenkael™ on 12.11.2015.
 */
public class TestCameraUDP extends AppCompatActivity implements View.OnClickListener, Camera.ErrorCallback{

    private SurfaceView     surface = null;
    private Button         btnStart = null;
    private Camera           camera = null;
//    private UDPOld              udp = null;
    private SOCKET           socket = null;
    private UDPOld              udp = null;
    private boolean          status = false;
    private TextView        txtView = null;
    private SeekBar          slider = null;

    private Switch         switcher = null;
    private boolean        UDPorTCP = false;

    private String      inputINFO = null;
    private String sizecam = null;
    private int     qality = 80;
    private int   sizeJPEG =  0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);

        txtView = (TextView) findViewById(R.id.textView4);
        slider  = (SeekBar)  findViewById(R.id.seekBar3);
        switcher    = (Switch) findViewById(R.id.switchCamera);

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UDPorTCP = isChecked;
            }
        });

        slider.setMax(99);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qality = progress + 1;
                inputINFO = "sizecam: " + sizecam + ",compression: " + qality + ",sizeJPEG: " + sizeJPEG;
                txtView.setText(inputINFO);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        slider.setProgress(80);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        surface = (SurfaceView) findViewById(R.id.surfaceView);
        btnStart = (Button) findViewById(R.id.button);
        btnStart.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button:{
                if(!status)CameraStartStream(); else {
                    camera.release();
                    status = false;
                    if(udp!=null)udp.destroy();
                    if(socket!=null)socket.dispose();
                    socket=null;
                    udp=null;
                }
            }
        }
    }

    private void CameraStartStream(){

        if(!MainActivity.SERVER_ON_CLIENT) {
            if (UDPorTCP) {
                socket = new SOCKET(1488);
                socket.accept();
            } else {
                udp = new UDPOld(1488);
                udp.bind();
                udp.setPackageSize(NpS.IMAGE_SIZE_MIN);
                udp.receive();
                udp.transactionAccept();
            }
        }else{
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
//        udp = new UDP(MainActivity.IP, 1488);
//        udp.setPackageSize(NpS.IMAGE_SIZE_NORMAL);

        SurfaceHolder holder = null;
        camera = Camera.open(1);
        camera.setErrorCallback(this);
        holder = surface.getHolder();
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Camera.Size previewSize=camera.getParameters().getPreviewSize();
        int dataBufferSize=(int)(previewSize.height*previewSize.width*
                (ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat())));
        camera.addCallbackBuffer(new byte[dataBufferSize]);
        camera.setDisplayOrientation(90);
        sizecam = camera.getParameters().getPictureSize().width + ":" + camera.getParameters().getPictureSize().height;

        inputINFO = "sizecam: " + sizecam + ",compression: " + qality + ",sizeJPEG: " + sizeJPEG;
        txtView.setText(inputINFO);

        camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

                Camera.Size size = camera.getParameters().getPictureSize();
                YuvImage yuv = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                yuv.compressToJpeg(new Rect(0, 0, yuv.getWidth(), yuv.getHeight()), qality, baos);
                send(baos.toByteArray());

                camera.addCallbackBuffer(data);
                try {
                    baos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sizeJPEG = baos.size();
                inputINFO = "sizecam: " + sizecam + ",compression: " + qality + ",sizeJPEG: " + sizeJPEG;
                txtView.setText(inputINFO);

            }
        });

        camera.startPreview();

        status = true;
    }

    private void send(byte[]buffer){
        if(udp!=null){
            udp.send(buffer);
        }else{
            socket.send(buffer);
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        camera.release();
        udp.destroy();
        udp = null;
        status = false;
    }
}
