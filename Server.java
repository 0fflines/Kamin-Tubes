import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.spi.InetAddressResolver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class Server {
    private ServerSocket server;
    public static final int PORT = 3030;
    public static final String STOP_STRING = ",,";
    private Socket clientSocket;
    private int clientCount=1;
    private OperatingSystemMXBean osBean;
    private ArrayList<InetAddress> bannedIpAddressArray = new ArrayList<>();
    public HashMap<InetAddress, Double> activityCount = new HashMap<>();
    private ArrayList<ConnectedClient> listOfConnectedClient = new ArrayList<>();
    private static final double ENTROPY_THRESHOLD = -1;
    private final int DETECTION_TIMER = 2000;
    private final double ZSCORE_THRESHOLD = 3;

    public Server(){
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        try{
            server = new ServerSocket(PORT);
            System.out.println("Server started");
            Thread timer = new Thread(){
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    long targetTime = currentTime+DETECTION_TIMER;
                    boolean clientExists = false;
                    while(true){
                        if(currentTime >= targetTime){
                            targetTime = currentTime + DETECTION_TIMER;
                            entropyDetection();
                            activityCount.clear();
                        }
                        currentTime = System.currentTimeMillis();
                    }
                };
            };
            timer.start();
            while(true){
                iniConnections();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public Server getServer() {
        return this;
    }

    public void iniConnections() throws IOException{
        clientSocket = server.accept();
        if(checkBannedIp(clientSocket.getLocalAddress()) == true){
            clientSocket.close();
        }

        //kalo udh di close seharusnya isConnected bakal gagal
        if(clientSocket.isConnected()){
            Thread clientThread = new Thread(){
                @Override
                public void run() {
                    ConnectedClient client = new ConnectedClient(clientSocket, clientCount, osBean, clientSocket.getLocalAddress(), getServer());
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

    public boolean checkBannedIp(InetAddress ip){
        for(InetAddress bannedIp: bannedIpAddressArray){
            if(bannedIp.equals(ip)) return true;
        }
        return false;
    }

    public void entropyDetection(){
        double entropy = 0;
        for(InetAddress ip: activityCount.keySet()){
            //entropy -= activity*(log2 activity)
            entropy -= (activityCount.get(ip))*(Math.log(activityCount.get(ip))/(Math.log(2)));
        }
        System.out.println("Entropy: "+entropy);
        printBannedIp();
        if(entropy < ENTROPY_THRESHOLD){
            System.out.println("DDOS detected");
            findAttacker();
        }
    }

    public void findAttacker(){
        //hitung average activity
        double averageActivity=0;
        int uniqueIpCount=0;
        for(InetAddress ip: activityCount.keySet()){
            averageActivity += activityCount.get(ip);
            uniqueIpCount += 1;
        }
        averageActivity /= uniqueIpCount;

        //hitung standard deviasi
        double totalDeviasi = 0;
        for(InetAddress ip: activityCount.keySet()){
            totalDeviasi += Math.pow(activityCount.get(ip)-averageActivity, 2);
        }
        totalDeviasi = Math.sqrt(totalDeviasi);
        double standardDeviation = totalDeviasi/uniqueIpCount;
        
        for(InetAddress ip: activityCount.keySet()){
            double requestRate = activityCount.get(ip)/(DETECTION_TIMER/1000);
            double zScore = (requestRate - averageActivity)/standardDeviation;
            if(zScore != ZSCORE_THRESHOLD){
                ban(ip);
            }
        }
    }

    private void ban(InetAddress ip){
        System.out.println("ban in progress");
        for(ConnectedClient client: listOfConnectedClient){
            if(client.ipAddress.equals(ip)){
                try{
                    listOfConnectedClient.remove(client);
                    client.clientSocket.close();
                    activityCount.remove(ip);
                    bannedIpAddressArray.add(ip);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void printBannedIp(){
        System.out.println("----------------");
        for(InetAddress ip: bannedIpAddressArray){
            System.out.println(ip);
        }
        System.out.println("----------------");
    }

    public static void main(String[] args) {
        new Server();
    }
}
