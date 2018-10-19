package wenkael.multirecorder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class UDP {

	private DatagramSocket  socket = null;
	private DatagramPacket	sendpacket = null;
	private DatagramPacket	recivepacket = null;
	private SocketAddress       socketAddress=null;
	private int  PORT;
	private String IP;

    final private static byte[] regx = "^/8".getBytes();
        
	private static UDP instance;
	public static UDP getInstance()
	{
		return instance;
	}
        
	public UDP(int port) {
            PORT = port;
            instance = this;
	}
        
        public boolean bind(){
            try {
                socket = new DatagramSocket(PORT);
//                socket.setBroadcast(true);
            } catch (SocketException ex) {
                System.err.println("ERROR PORT BUSY");
                return false;
            }
            return true;
        }
        
        public void connect(){
            receive(100);
        }
        
        public String info(){
            try {
                return "ReceiveBufferSize:"+socket.getReceiveBufferSize()+"; SendBufferSize: "+socket.getSendBufferSize();
            } catch (SocketException ex) {
                Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        
	public UDP(String ip, int port) {
		try {
			IP = ip;PORT = port;
                        socketAddress = new InetSocketAddress(IP, PORT);
			socket = new DatagramSocket();
                        socket.setBroadcast(true);
                } catch (SocketException e) {
			e.printStackTrace();
		}
                instance = this;
	}
        
	public void add(String ip, int port)
	{
		IP=ip;PORT=port;
        socketAddress = new InetSocketAddress(IP, PORT);
	}
	
	public void setSoTimeOut(int timeout)
	{
            try {
                socket.setSoTimeout(timeout);
            } catch (SocketException e) {
                e.printStackTrace();
            }
	}
	
	public boolean send(byte[] buffer)
	{
            try {

                if(buffer==null)return false;
//                byte[] rbuffer = new byte[buffer.length+regx.length];
//                System.arraycopy(buffer, 0, rbuffer, 0, buffer.length);
//                System.arraycopy(regx, 0, rbuffer, buffer.length, regx.length);
                
//                byte[] maxBuffer = new byte[512];
//                ByteArrayInputStream buffers = new ByteArrayInputStream(rbuffer);
//                    int i;
//                    while((i = buffers.read(maxBuffer))!=-1){
                        sendpacket = new DatagramPacket(buffer, buffer.length, socketAddress);
                        socket.send(sendpacket);
//                    }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
	}
	
	
	public byte[] receive(int size)
	{
            try {
                byte[] buffer = new byte[size];
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
//                while(!isByte(buffer, regx)){
                    recivepacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(recivepacket);
//                    baos.write(buffer);
//                }
//                socketAddress = recivepacket.getSocketAddress();
//                byte[] input = baos.toByteArray();
//                baos.close();
                return buffer;
            } catch (IOException e) {
                return null;
            }
            
	}
        
        public static boolean isByte(byte[]array, byte[]b)
        {
            int a = array.length;
            int coins = 0;int l = b.length;
            for(int i=0;i<a;i++){
                if(array[i]==b[coins])coins++;
                if(coins==l)return true;
            }
            return false;
        }
        
        public void destroy()
        {
            try{
                socket.close();
                socket = null;
                socketAddress = null;
                recivepacket = null;
                sendpacket = null;
            }catch(Exception ex){System.out.println("destroy udp error");}
        }
        
}
