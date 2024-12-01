import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class Server {
    private ServerSocket server;
    public static final int PORT = 3030;
    public static final String STOP_STRING = ",,";
    private Socket clientSocket;
    private int clientCount=1;
    private OperatingSystemMXBean osBean;

    public Server(){
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        try{
            server = new ServerSocket(PORT);
            System.out.println("Server started");
            while(true) iniConnections();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void iniConnections() throws IOException{
        clientSocket = server.accept();

        if(clientSocket.isConnected()){
            Thread clientThread = new Thread(){
                public void run() {
                    ConnectedClient client = new ConnectedClient(clientSocket, clientCount, osBean);
                    System.out.println("Client "+clientCount+" connected");
                    clientCount++;
                    try{
                        client.readMessages();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                };
            };
            clientThread.start();;
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
