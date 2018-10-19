package wenkael.multirecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by wenkael™ on 26.10.2015.
 */
public class SurfaceCamera extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback
{
    
    private Camera           camera;
    private SurfaceView surfaceView;
    private SurfaceHolder    holder;
    private Button         btnStart;
    private static Net   net = null;
//    private Spinner         spinner;
    private SeekBar         seekbar;
    private TextView    txtProgress;
    private static UDPOld  udp=null;

    private int[] formats = new int[]{
            ImageFormat.RGB_565, ImageFormat.NV16,ImageFormat.YUY2,ImageFormat.YV12,
            ImageFormat.JPEG,ImageFormat.NV21,ImageFormat.YUV_420_888,
            ImageFormat.YUV_422_888,ImageFormat.YUV_444_888,ImageFormat.FLEX_RGB_888,
            ImageFormat.FLEX_RGBA_8888,ImageFormat.RAW_SENSOR,
            ImageFormat.RAW10,ImageFormat.RAW12,ImageFormat.DEPTH16,
            ImageFormat.DEPTH_POINT_CLOUD,ImageFormat.PRIVATE};

    private String[] formatstxt = new String[]{
            "RGB_565", "NV16","YUY2","YV12",
            "JPEG","NV21","YUV_420_888",
            "YUV_422_888","YUV_444_888","FLEX_RGB_888",
            "FLEX_RGBA_8888","RAW_SENSOR",
            "RAW10","RAW12","DEPTH16",
            "DEPTH_POINT_CLOUD","PRIVATE"};
    private int ACTIVE_FORMAT;

    private int qality;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        btnStart = (Button)findViewById(R.id.button);
//        imgview = (ImageView) findViewById(R.id.imageView2);
        txtProgress = (TextView) findViewById(R.id.textView4);

        seekbar = (SeekBar)findViewById(R.id.seekBar3);

        seekbar.setMax(99);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qality = progress + 1;
                txtProgress.setText(String.valueOf(qality));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbar.setProgress(5);

//        MediaRecorder recorder = new MediaRecorder();
//        MediaRecorder.VideoSource video =
//        spinner = (Spinner) findViewById(R.id.spinner2);
//        ACTIVE_FORMAT = ImageFormat.NV21;
//        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formatstxt) {};
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setSelection(5);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ACTIVE_FORMAT = formats[(int)id];
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//            }
//        });

    }

    public void onClick(View view)
    {
        if(view == btnStart)
        {
            if(udp==null)
            {
                udp = new UDPOld(MainActivity.IP, 1488);
                udp.setPackageSize(NpS.IMAGE_SIZE_NORMAL);
                System.out.println("START UDP");
            }
            else
            {
                udp.destroy();udp = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean permission = false;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            int requestCode = 0;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, requestCode);
//            if(requestCode==PackageManager.PERMISSION_GRANTED){
//                permission = true;
//            }
        }else{
            permission = true;
        }
        if(permission) {
            camera = Camera.open(1);
            System.out.println("CAMERA OPEN");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
        {
//            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            System.out.println("CAMERA PAUSE");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

//        float aspect = (float) previewSize.width / previewSize.height;
//        int previewSurfaceWidth = surfaceView.getWidth();
//        int previewSurfaceHeight = surfaceView.getHeight();
//        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
//            lp.height = previewSurfaceHeight;
//            lp.width = (int) (previewSurfaceHeight / aspect);
//        Camera.Parameters param = camera.getParameters();
////        param.setPreviewFpsRange(5, 20);
//        param.setPreviewFrameRate(5);
//        param.setPictureSize(300, 300);
//        surfaceView.setLayoutParams(lp);

        System.out.println("SuRFace Create");
        try
        {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallbackWithBuffer(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        int rotation = SurfaceCamera.this.getWindowManager().getDefaultDisplay().getRotation();
        camera.setDisplayOrientation(rotation);

        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        int dataBufferSize= (previewSize.height*previewSize.width*(ImageFormat.getBitsPerPixel(camera.getParameters().getPreviewFormat())));

        camera.addCallbackBuffer(new byte[dataBufferSize]);
        camera.startPreview();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        YuvImage yav = new YuvImage(data, ImageFormat.NV21,
                camera.getParameters().getPreviewSize().width,
                camera.getParameters().getPreviewSize().height, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yav.compressToJpeg( new Rect(0, 0, yav.getWidth(), yav.getHeight()), 80, baos );

        byte[] buffer = baos.toByteArray();
        if(udp!=null) {
            System.out.println(udp.send(buffer));
        }
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.addCallbackBuffer(data);
    }

    private static boolean save = false;
    private static ByteBuffer yuv = ByteBuffer.allocate(118000);
    private void saveYUV(int width, int height) {
//            *****************************************************************
        String name = String.format("/sdcard/%dVIDEO.WEBP", System.currentTimeMillis());
        FileOutputStream outStream = null;
        try
        {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsToBuffer(yuv);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out);

            outStream = new FileOutputStream(name);
            outStream.write(out.toByteArray());
            outStream.close();

            save=true;
            System.out.println("***SAVE!!!");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//            *****************************************************************
    }


    //        Bitmap bitmap = Bitmap.createBitmap(0,0,imgview.getWidth(),imgview.getHeight());
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            try {
//                int size = bit.getRowBytes() * bit.getHeight();
//                ByteBuffer b = ByteBuffer.allocate(size);
//                bit.copyPixelsToBuffer(b);
//                byte[] bufferIMG = new byte[size];
//                b.get(bufferIMG, 0, bufferIMG.length);

//                byte[] array = baos.toByteArray();
//                int count = 0;
//                for(int i=0;i<array.length;i++)
//                {
//                    if(array[i]==0){array[i]=(byte)random(-128, 127); count++;}
//
////                    if(i%30==0){
////                        System.out.println(" |"+i+":- ["+array[i]+"]");
////                    }else{
////                        System.out.print(" |"+i+":- ["+array[i]+"]");}
//                }

//                System.out.println("****************************** "+count);



//                String name = String.format("/sdcard/%dPHOTO.jpg", System.currentTimeMillis());
//                FileOutputStream outStream = new FileOutputStream(name);
//                outStream.write(baos.toByteArray());
//                outStream.close();

//                File testFile = new File(name);
//                FileInputStream inputStream = new FileInputStream(testFile);
//                byte[]testBuffer = new byte[(int)testFile.length()];
//                inputStream.read(testBuffer);

//                System.out.println("***READ " + testBuffer.length);


//            }catch (Exception e){System.out.println("***ERROR : "+e);}



    public int random(int min, int max){
        return (int)Math.random()*(max-min)+min;
    }

//    public static byte[] buffers;
    class Net extends Thread {

        private UDP           udp;
//        private SOCKET     socket;
        private boolean isWriting;
        private String ip =  null;
//        private byte[] buffers = null;
//        private ByteBuffer buffer  = ByteBuffer.allocateDirect(50_000);
//        private int position;

//        private volatile int length;

//        private ArrayList<int[]> position = new ArrayList<>();
        private ArrayList<byte[]> array = new ArrayList<>();
//        private HashMap<Integer, ArrayList> hash = new HashMap();

        public void setImg(byte[]b)
        {
//            if(buffer.remaining()>=b.length) {
//                position.add( new int[]{ buffer.put(b.clone()).position(), b.length } );
//            }else
//            {
//                position.clear();
//                position.trimToSize();
//                buffer.clear();
//                position.add( new int[]{ buffer.put(b.clone()).position(), b.length });
//            }

//                array.clear();
//                array.trimToSize();

            array.add(b.clone());

        }

        public Net(String IP) {
            this.ip = IP;
            System.out.println("***START CAMERA THREAD");
        }

        @Override
        public void run() {
            udp = new UDP(ip, 1488);

//            udp.setSoTimeOut(5000);
//            udp.send("\n".getBytes());

//            socket = new SOCKET(ip, 1488);

//            if(socket.connect()) {
                isWriting = true;
//            }
//            else
//            {
//                socket.dispose();
//                net=null;
//                System.out.println("Connected Destroy");
//            }

            while (isWriting) {
//                if (buffers != null) {

//                Iterator<Integer> iter = position.iterator();
//                    if() {
//                while(iter.hasNext()) {
//                for(int i=0;i<position.size();i++)
//                {
////                    if(position.get(i)[0]<=position.get(i)[1]) {
//                        byte[] buffers = new byte[position.get(i)[1]];
//                        buffer.position(position.get(i)[0] - buffers.length);
//                        buffer.get(buffers);
////                        socket.send(new String("**********[" + buffers.length + "]**********").getBytes());
//                        socket.send(buffers);
//                        System.out.println("***send: " + buffers.length);
////                    }else System.out.println("*** BAG ***");
//                }

                for(int i=0;i<array.size()-1;i++)
                {
//                    socket.send((""+array.get(i).length).getBytes());
//                    int length = "\b".length()+array.get(i).length;
//                    byte[]sendBuffer = new byte[array.get(i).length+length];
//                    System.arraycopy();
//                    socket.send(array.get(i));

//                    byte[]len = ;
//                    udp.send(len);
//                    socket.send(Arrays.copyOf(("[" + array.get(i).length + "]").getBytes(), 18));
//                    socket.send(array.get(i));

                    udp.send(array.get(i));
                    array.remove(i);
                    array.trimToSize();

//                    System.out.println("***send: " + array.get(i).length);
                }

//                if(array.size()>=20)
//                {
//                    array.clear();
//                    array.trimToSize();
//                }

//                try {
//                    sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                    }
//                }
//                iter.remove();
//                position.clear();
//                buffer.clear();
//                buffer.position(0);

//                        isWriting = false;
//                        net=null;
//                        buffers=null;
//                        socket.dispose();
//                        System.out.println("***DISCONNECT!");
//                    }else {buf = null;isWriting=false;}
//                }
            }

        }

    }

}
