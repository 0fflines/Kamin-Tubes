import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import com.sun.management.OperatingSystemMXBean;

public class ConnectedClient {
    DataInputStream in;
    PrintWriter out;
    private Socket clientSocket;
    private OperatingSystemMXBean osBean;
    private int id;
    private final String STOP_STRING = ",,";

    ConnectedClient(Socket clientSocket, int id, OperatingSystemMXBean osBean){
        this.id = id;
        this.clientSocket = clientSocket;
        this.osBean = osBean;
        try{
            this.in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("You are connected as client "+id);
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
                out.println(responseMessage);
            }
        }catch(EOFException e){
            System.out.println("Socket "+id+" closed");
            close();
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
