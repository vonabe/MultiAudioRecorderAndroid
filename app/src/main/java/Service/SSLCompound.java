package Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author wenkael™
 */
public class SSLCompound {

    private Socket socket = null;
    private InputStream input = null;
    private OutputStream output = null;
    
    private boolean create = false;
    
    public SSLCompound(Socket socket) {
        this.socket = socket;
    }
    
    public boolean create(){
        if(create==false){
            try {
                this.input = socket.getInputStream();
                this.output = socket.getOutputStream();
            } catch (IOException ex) {
                create = false;
                return false;
            }
        }else return false;
        create = true;
        return true;
    }
    
    public boolean setTimeout(int timeout){
        if(status()){
            try {
                this.socket.setSoTimeout(timeout);
            } catch (SocketException ex) {
                create = false;
                return false;
            }
        }
        return true;
    }
    
    public boolean send(byte[]buffer){
        try {
            ByteArrayInputStream buff = new ByteArrayInputStream(buffer);
            byte[] send = new byte[512];
            int i=0;
            while((i=buff.read(send))!=-1){
                output.write(send, 0, i);
                output.flush();
            }
            buff.close();
            return true;
        } catch (IOException ex) {
            create = false;
            return false;
        }
    }
    
    public byte[] receive(int size){
        try {
            byte[] buff = new byte[size];
            if(input.read(buff, 0, size)==-1){create = false;return null;}
            return buff;
        } catch (IOException ex) {
            create = false;
            return null;
        }
    }
    
    public InputStream getInput(){
        return input;
    }
    
    public OutputStream getOutput(){
        return output;
    }
    
    public Socket getSocket(){
        return socket;
    }
    
    public void dispose(){
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException ex) {

        }finally{
            socket = null;
            input = null;
            output = null;
            create = false;
        }
    }
    
    public boolean status() {
        return create;
    }
    
}
