import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import com.sun.management.OperatingSystemMXBean;

public class ConnectedClient {
    DataInputStream in;
    PrintWriter out;
    public Socket clientSocket;
    private OperatingSystemMXBean osBean;
    private int id;
    private final String STOP_STRING = ",,";
    public InetAddress ipAddress;
    private Server server;

    ConnectedClient(Socket clientSocket, int id, OperatingSystemMXBean osBean, InetAddress ipAddress, Server server){
        this.id = id;
        this.clientSocket = clientSocket;
        this.osBean = osBean;
        this.ipAddress = ipAddress;
        this.server = server;
        try{
            this.in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("You are connected as client "+id +" with ip "+ipAddress);
        }catch(IOException e){
            e.printStackTrace();
        }
    }  

    public void readMessages() throws IOException{
        String inputMessage = "";
        String responseMessage = "";
        try{
            while(!inputMessage.equals(STOP_STRING)){
                inputMessage = in.readUTF();
                double cpuLoad = getCPULoad();
                long totalMemory = getTotalMemory();
                long freeMemory = getFreeMemory();
                long usedMemory = getUsedMemory();
                responseMessage = "Current CPU Load: " + cpuLoad + "%\n" +
                                    "Total Memory Size: " + totalMemory + " MB\n" +
                                    "Used Memory Size: " + usedMemory + " MB\n" +
                                    "Free Memory Size: " + freeMemory + " MB";
                // if(this.server.activityCount.get(ipAddress) == null){
                //     this.server.activityCount.put(ipAddress, 1.0);
                // }else{
                //     this.server.activityCount.put(ipAddress, this.server.activityCount.get(ipAddress)+1);
                // }
                this.server.activityCount.merge(ipAddress, 1.0, Double::sum);

                
                out.println(responseMessage);
            }
        }catch(SocketException e){
            // System.out.println("Socket "+id+" closed");
            // close();
        }
    }

    public double getCPULoad() {
        return this.osBean.getCpuLoad() * 100;
    }

    public long getTotalMemory() {
        return this.osBean.getTotalMemorySize() / (1024 * 1024);
    }

    public long getFreeMemory() {
        return this.osBean.getFreeMemorySize() / (1024 * 1024);
    }

    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }
    
    public void close() throws IOException{
        in.close();
        out.close();
    }
}
