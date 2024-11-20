import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import com.sun.management.OperatingSystemMXBean;

public class ConnectedClient {
    private Socket clientSocket;
    private OperatingSystemMXBean osBean;
    private DataInputStream in;
    private int id;
    private PrintWriter out;

    public ConnectedClient(Socket clientSocket, int id, OperatingSystemMXBean osBean) {
        this.clientSocket = clientSocket;
        this.id = id;
        this.osBean = osBean;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Client " + this.id + " has connected");
            this.in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessages() {
        String line = "";
        String responseMessage = "";
        while(!(line.equals(Server.STOP_STRING))) {
            try {
                line = in.readUTF();
                double cpuLoad = getCPULoad();
                long totalMemory = getTotalMemory();
                long freeMemory = getFreeMemory();
                long usedMemory = getUsedMemory();
                responseMessage = "Current CPU Load: " + cpuLoad + "%\n" +
                                    "Total Memory Size: " + totalMemory + " MB\n" +
                                    "Used Memory Size: " + usedMemory + " MB\n" +
                                    "Free Memory Size: " + freeMemory + " MB";
                out.println(responseMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client " + this.id + " has disconnected");
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
 
    public void close() {
        try {
            clientSocket.close();
            in.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
