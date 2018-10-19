package wenkael.multirecorder;

import android.media.AudioRecord;
import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Service.*;
import wenkael.multirecorder.MyService;


/**
 * Created by veniamin on 28.07.2016.
 */
public class Server implements Runnable {

    final private String TAG_SERVER = "MyServer";
    final private String pcwIP = "46.173.6.49", localWifi = "192.168.0.100";
    final private MyService service;
    private Service.SOCKET socket = null;
    private boolean runnable = false;
    private boolean pause = true;

    public Server(MyService service){
        this.service = service;
    }

    @Override
    public void run() {
        runnable = true;
        this.socket = new Service.SOCKET(this.pcwIP, 1488);
        final SSLCompound compound = this.socket.connect();
        if(compound==null){Log.d("MyService-Server","not connect");runnable=false;return;}
            compound.create();
            compound.setTimeout(20_000);

        runnable = compound.send(service.device_info.getBytes());

        Log.d("MyService-Server","connect successful");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(runnable) {
                    byte[] buffer = compound.receive(1024);
                    if (buffer != null) {
                        try {
                            String message = new String(buffer).trim();
                            Log.d(TAG_SERVER,message);
                            JSONParser parser = new JSONParser();
                            JSONObject object = (JSONObject) parser.parse(message);
                            JSONArray array = (JSONArray) object.get("command");
                            JSONObject obj = (JSONObject) array.get(0);
                            Log.d(TAG_SERVER,obj.toString());
                            pause = !((boolean)obj.get("mic"));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        runnable = false;
                    }
                }
            }
        }).start();

        final AudioRecord record = this.service.createMicrophone();

        while(runnable){
            if(!pause) {
                byte[] sendAudio = new byte[1024];
                record.read(sendAudio, 0, sendAudio.length);
                boolean send = compound.send(sendAudio);
                runnable = send;
            }
        }

//        boolean stateSEND = compound.send(this.service.getInfo().getBytes());
//        Log.d("MyService-Server","send:"+stateSEND);
//        byte[] receive = compound.receive(1024);
//        String messageReceive = new String( (receive==null)?"010101010101".getBytes():receive);
//        Log.d("MyService-Server","receive:" );
        if(compound!=null)compound.dispose();
        if(this.socket!=null)this.socket.dispose();

    }

    public boolean isRunnable(){
        return runnable;
    }

    public void stop(){
        this.runnable=false;
    }

}
