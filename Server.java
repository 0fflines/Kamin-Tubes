import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class Server {
    private ServerSocket server;
    private DataInputStream in;
    private PrintWriter out;
    public static final int PORT = 3030;
    public static final String STOP_STRING = ",,";
    private OperatingSystemMXBean osBean;
    private int clientCount = 0;

    public Server(){
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        System.out.println("Server started");
        try{
            server = new ServerSocket(PORT);
            while(true) {
                iniConnections();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void iniConnections() throws IOException{
        Socket clientSocket = server.accept();

        if(clientSocket.isConnected()) {
            new Thread(() -> {
                clientCount++;
                ConnectedClient client = new ConnectedClient(clientSocket, clientCount, osBean);
                client.readMessages();
                client.close();
            }).start();
        }
        close();
    }

    public void close() throws IOException{
        in.close();
        out.close();
        server.close();
    }

    public static void main(String[] args) {
        new Server();
    }
}
