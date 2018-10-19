package wenkael.multirecorder;

import android.os.StrictMode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author wenkael�
 */
public class Broadcast implements Runnable {
    
    final private Thread thread = new Thread(this);
    
    private static DatagramSocket                 socket;
    private DatagramPacket    sendpacket, receiverpacket;
    private InetSocketAddress address;
    private final ArrayList<String> searchip = new ArrayList<>();
    
    private final Integer[] poolport = new Integer[]{5555,1333,1555,9999,1919};
    
    public Broadcast() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    
    public boolean bind() {
        for (int port : poolport) {
            try {
                socket = new DatagramSocket(port);
                thread.start();
                return true;
            } catch (SocketException ex) {
                System.err.println("port bussy");
            }
        }
        return false;
    }
    
    public Object[] search(){
        
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(1500);
        } catch (SocketException ex) {System.err.println("error search");}
        
        byte[]buffer = new byte[56];
        Arrays.fill(buffer, (byte)0);

        List<Integer> list = Arrays.asList(poolport);
        Iterator<Integer> it = list.iterator();
        while(it.hasNext()){
            int port = it.next();
            address = new InetSocketAddress("255.255.255.255", port);
            try {

                sendpacket = new DatagramPacket(buffer, buffer.length, address);

                try { socket.send(sendpacket); } catch (IOException ex) {System.err.println("error send");}
            
                receiverpacket = new DatagramPacket(buffer, buffer.length);
                try { socket.receive(receiverpacket); } catch (IOException ex) {System.err.println("broadcast search timeout");receiverpacket=null;}

                if(receiverpacket!=null) {
                    String ip = receiverpacket.getAddress().getHostName();
                    if (ip != null)
                        if(!searchip.contains(ip))searchip.add(ip);
                    System.out.println("ip --- " + ip);
                }

            } catch (SocketException e) {System.err.println("error address");}
        }
        return searchip.toArray();
    }
    
    @Override
    public void run() {
        System.out.println("broadcast start - "+socket.getPort());
        while(true){
            try {
                
                byte[] receive = new byte[56];
                receiverpacket = new DatagramPacket(receive, receive.length);
                socket.receive(receiverpacket);
                
                System.out.println("ping --- "+receiverpacket.getAddress().getHostName());
                
                sendpacket = new DatagramPacket(receive, receive.length, receiverpacket.getSocketAddress());
                socket.send(sendpacket);
                
                searchip.add(receiverpacket.getAddress().getHostName());
            } catch (IOException ex) {
                System.err.println("broatcast server error receive "+ex);
            }
        }
    }
    
}