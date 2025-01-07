import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;
    private Scanner sc;

    public Client(){
        try{
            socket = new Socket("192.168.52.197", Server.PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sc = new Scanner(System.in);
            System.out.println(in.readLine());
            writeMessage();
            close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void writeMessage() throws IOException{
        String line;
        while(!(line = sc.nextLine()).equals(Server.STOP_STRING)){
            if(!line.equals("r")) {
                System.out.println("Wrong command");
            }else{
                out.writeUTF(line);
                out.flush();
                String serverResponse = "";
                for(int i = 0;i < 4;i++) {
                    serverResponse += in.readLine() + "\n";
                }

                System.out.println(serverResponse);
            }
        }
        close();
    }

    private void close() throws IOException{
        socket.close();
        System.out.println("closed");
        in.close();
        out.close();
        sc.close();
    }

    public static void main(String[] args) {
        new Client();
    }
}
