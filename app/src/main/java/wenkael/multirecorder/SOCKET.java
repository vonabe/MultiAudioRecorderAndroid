package wenkael.multirecorder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wenkael™ on 26.10.2015.
 */
public class SOCKET {

    private Socket         s;
    private ServerSocket  ss;
    private InputStream   in;
    private OutputStream out;
    private String IP;private int PORT;

    public SOCKET(int port)
    {
        PORT = port;
    }

    public SOCKET(String ip, int port)
    {
        IP = ip;PORT = port;
    }

    public boolean connect()
    {
        try {
                s = new Socket(IP, PORT);
                in = s.getInputStream();
                out = s.getOutputStream();
                System.out.println("CONNECT! " + s.getInetAddress());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean accept()
    {
        try {
            ss = new ServerSocket(PORT);
            System.out.println("WAIT: "+PORT);
            s = ss.accept();
            in = s.getInputStream();
            out = s.getOutputStream();
            System.out.println("CONNECT! " + s.getInetAddress());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean send(byte[]buffer)
    {
        try {
//            BufferedInputStream buffered = new BufferedInputStream(new ByteArrayInputStream(buffer));
//            out.write(buffer, 0, buffer.length);
//            byte[] byteArray = new byte[512];
//            int in = 0;
//            while ((in = buffered.read(byteArray)) != -1) {
            out.write(buffer);
            out.flush();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public InputStream getInput(){
        return in;
    }

    public byte[] receive(int size)
    {
        byte[]buffer=new byte[size];
        try {
            in.read(buffer);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void dispose()
    {
        try {
            if(s!=null)s.close();
            if(in!=null)in.close();
            if(out!=null)out.close();
            if(ss!=null)ss.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
