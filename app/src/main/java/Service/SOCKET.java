package Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by wenkael� on 26.10.2015.
 */
public class SOCKET {

    private ServerSocket  ss;
    private String        IP;
    private int         PORT;
    private boolean   status;
    
    public SOCKET(int port)
    {   
        PORT = port;
    }

    public SOCKET(String ip, int port)
    {
        IP = ip;PORT = port;
    }
    
    public boolean isConnect(){
        return status;
    }
    
    public boolean bind(){
        try {
            ss = new ServerSocket(PORT);
        } catch (IOException ex) {
            System.out.println("error port bussy");
            return false;
        }
        return true;
    }
    
    public SSLCompound connect()
    {
        try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(IP, PORT), 10_000);
                SSLCompound compound = new SSLCompound(socket);
                if(!compound.create()){compound = null;return null;}
                System.out.println("CONNECT! " + socket.getInetAddress());
                status = true;
            return compound;
        } catch (IOException e) {status = false;}
        return null;
    }
    
    public SSLCompound accept()
    {
        try {
            System.out.println("WAIT: "+PORT);
            
            Socket s = ss.accept();
            SSLCompound compound = new SSLCompound(s);
            if(!compound.create()){
                compound = null;
                return null;
            }
            System.out.println("accept! " + s.getInetAddress());
            status = true;
            return compound;
        }catch(IOException e){
            status = false;
        }
        return null;
    }
    
    public void dispose(){
        try {
            if(ss!=null)ss.close();
            ss = null;
            status = false;
        } catch (IOException ex) {Logger.getLogger(SOCKET.class.getName()).log(Level.SEVERE, null, ex);}
    }

}
