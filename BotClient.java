import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class BotClient {
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;

    public BotClient(){
        try{
            socket = new Socket("192.168.68.162", Server.PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(in.readLine());
            writeMessage();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //while loop g tau kenapa masih g bisa tapi bisa input kai client biasa
    private void writeMessage() throws IOException{
        while(true){
            out.writeUTF("r");
            out.flush();

            String serverResponse = in.readLine();
            System.out.println(serverResponse);
        }
    }

    public static void main(String[] args) {
        while(true){
            new Thread(){
                public void run() {
                    new BotClient();
                };
            }.start();
        }
    }
}
