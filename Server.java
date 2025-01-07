import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    public ConcurrentHashMap<InetAddress, Double> activityCount = new ConcurrentHashMap<InetAddress, Double>();
    private ArrayList<Socket> listOfConnectedClient = new ArrayList<>();
    private static final double ENTROPY_THRESHOLD = 0.3;
    private final int DETECTION_TIMER = 5000;
    private final double ZSCORE_THRESHOLD = 0.3;

    public Server(){
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        try{
            server = new ServerSocket(PORT);
            System.out.println("Server started");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                entropyDetection();
                activityCount.clear();
            }, 0, DETECTION_TIMER, TimeUnit.MILLISECONDS);
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
        boolean isBanned = checkBannedIp(clientSocket.getInetAddress());
        if(isBanned){
            clientSocket.close();
            return;
        }
        else{
            //kalo udh di close seharusnya isConnected bakal gagal
            if(clientSocket.isConnected()){
                listOfConnectedClient.add(clientSocket);
                Thread clientThread = new Thread(){
                    @Override
                    public void run() {
                        ConnectedClient client = new ConnectedClient(clientSocket, clientCount, osBean, clientSocket.getInetAddress(), getServer());
                        // System.out.println("Client "+clientCount+" connected");
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
    }

    public synchronized boolean checkBannedIp(InetAddress ip){
        for(InetAddress bannedIp: bannedIpAddressArray){
            if(bannedIp.equals(ip)) return true;
        }
        return false;
    }

    public void entropyDetection(){
        double entropy = 0;
        double totalActivity = 0;
        System.out.println(activityCount);
        for(InetAddress ip: activityCount.keySet()){
            totalActivity += activityCount.get(ip);
        }
        for(InetAddress ip: activityCount.keySet()){
            //entropy -= activity*(log2 activity)
            //10log b / 10log a = alog b
            double probability = activityCount.get(ip)/totalActivity;
            entropy -= probability*(Math.log(probability)/(Math.log(2)));
        }
        System.out.println("Entropy: "+entropy);
        printBannedIp();
        if(activityCount.keySet().size() > 1 && entropy < ENTROPY_THRESHOLD){
            System.out.println("DDOS detected");
            findAttacker();
        }
    }

    public void findAttacker(){
        //hitung average activity
        System.out.println("ban search");
        double averageActivity=0;
        int uniqueIpCount=0;
        for(InetAddress ip: activityCount.keySet()){
            averageActivity += activityCount.get(ip);
            uniqueIpCount += 1;
        }
        uniqueIpCount = activityCount.keySet().size();
        // System.out.println(activityCount.keySet());

        averageActivity /= uniqueIpCount;
        System.out.println(averageActivity);
        // System.out.println("uipcount ="+ uniqueIpCount);

        //hitung standard deviasi
        double totalDeviasi = 0;
        for(InetAddress ip: activityCount.keySet()){
            totalDeviasi += Math.pow(activityCount.get(ip)-averageActivity, 2);
        }
        // System.out.println("totaldeviasi ="+totalDeviasi);
        totalDeviasi = Math.sqrt(totalDeviasi);
        // System.out.println("totaldeviasi ="+totalDeviasi);
        double standardDeviation = totalDeviasi/uniqueIpCount;
        System.out.println(standardDeviation);
        // System.out.println("standarddeviasi ="+standardDeviation);
        
        Set<InetAddress> ipKeyMap = activityCount.keySet();
        for(InetAddress ip: ipKeyMap){
            double requestRate = activityCount.get(ip);
            double zScore = (requestRate - averageActivity)/standardDeviation;
            System.out.println(ip+" ZSCORE ="+zScore);
            if(zScore > ZSCORE_THRESHOLD){
                ban(ip);
            }
        }
    }

    private synchronized void ban(InetAddress ip){
        System.out.println("ban in progress");
        // ArrayList<InetAddress> flaggedIp = new ArrayList<>();
        // ArrayList<Socket> flaggedSocket = new ArrayList<>();
        for(int i = 0; i < listOfConnectedClient.size();  i++){
            Socket client = listOfConnectedClient.get(i);
            if(client.getInetAddress().equals(ip)){
                try{
                    listOfConnectedClient.remove(client);
                    client.close();
                    activityCount.remove(ip);
                    if(!bannedIpAddressArray.contains(ip))bannedIpAddressArray.add(ip);
                    break;
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        // for(Socket client: listOfConnectedClient){
        //     if(client.getInetAddress().equals(ip)){
        //         // try{
        //         //     listOfConnectedClient.remove(client);
        //         //     client.close();
        //         //     activityCount.remove(ip);
        //         //     bannedIpAddressArray.add(ip);
        //         //     bannedIpAddressArray.add(ip);
        //         //     break;
        //         // }catch(IOException e){
        //         //     e.printStackTrace();
        //         // }
        //         if(!flaggedIp.contains(ip))flaggedIp.add(ip);
        //         flaggedSocket.add(client);
        //     }
        // }

        // for(InetAddress banIp: flaggedIp){
        //     activityCount.remove(banIp);
        //     bannedIpAddressArray.add(banIp);
        // }
        // for(Socket bannedSocket: flaggedSocket){
        //     try {
        //         bannedSocket.close();
        //         listOfConnectedClient.remove(bannedSocket);
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        // }
    }

    private void printBannedIp(){
        System.out.println("----------------");
        System.out.println("Banned IP:");
        System.out.println(bannedIpAddressArray);
        for(InetAddress ip: bannedIpAddressArray){
            System.out.println(ip);
        }
        System.out.println("----------------");
    }

    public static void main(String[] args) {
        new Server();
    }
}
