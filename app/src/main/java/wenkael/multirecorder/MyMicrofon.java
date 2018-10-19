package wenkael.multirecorder;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by wenkael™ on 18.11.2015.
 */
public class MyMicrofon extends AppCompatActivity {

    private Thread                 thread = null;
    private AudioRecord            record = null;
    private static UDPOld             udp = null;
    private static boolean        status = false;

    private VisualizerView visualizerView;
    private Visualizer         visualizer;

    private Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(R.layout.activity_microphone);

        btnStart = (Button) findViewById(R.id.btnMicrophone);

        recordAudioCreate();

        visualizerView = new VisualizerView(this);
        visualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        (int) (150 * getResources().getDisplayMetrics().density))
        );
        visualizerView.setBackgroundColor(Color.GRAY);

//        visualizer = new Visualizer(record.getAudioSessionId());
//        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
//        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
//            @Override
//            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
//                visualizerView.updateVisualizer(waveform);
//            }
//
//            @Override
//            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
//                System.out.println("OUT::Fft");
//            }
//
//        }, Visualizer.getMaxCaptureRate() / 2, true, false);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeMicro);
        layout.addView(visualizerView);

//        visualizer.setEnabled(true);

    }

    public void recordAudioCreate(){
        if(record!=null)return;

        int buffersize = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, buffersize);
        record.startRecording();

        MainActivity.toast("buffer-size: "+buffersize+" --- audio session "+record.getAudioSessionId());
        System.out.println(" *** audio session "+record.getAudioSessionId());

    }

    public void ClicStart(View view){
        if(status == false) {
            status = true;
            recordAudioCreate();
            networkCreate();
            btnStart.setText("disconnect");
        }else{
            close();
            btnStart.setText("connect");
        }
    }

    public void networkCreate()
    {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                udp = new UDPOld(MainActivity.IP, 1488);
                udp.setPackageSize(NpS.AUDIO_SIZE_MIN);
//                udp.send("microphone".getBytes());
                udp.transactionConnect();
                while(status)
                {
                    byte[] buffer = new byte[NpS.AUDIO_SIZE_MIN];
                    if(record != null) record.read(buffer, 0, buffer.length);
                    if(buffer!=null && udp != null)udp.send(buffer);
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }

    public void close(){
        status = false;
        if(record!=null)record.release();
        if(thread!=null)thread.interrupt();
        if(udp!=null)udp.destroy();
        thread = null;
        record = null;
        udp = null;
    }

}
