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
            socket = new Socket("localhost", Server.PORT);
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
            out.writeUTF(line);
            out.flush();

            String serverResponse = in.readLine();
            System.out.println(serverResponse);
        }
        close();
    }

    private void close() throws IOException{
        in.close();
        out.close();
        sc.close();
        socket.close();
    }

    public static void main(String[] args) {
        new Client();
    }
}
