import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectedClient {
    DataInputStream in;
    PrintWriter out;
    Socket socket;
    private int id;
    private final String STOP_STRING = ",,";

    ConnectedClient(Socket socket, int id){
        this.id = id;
        this.socket = socket;
        try{
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new PrintWriter(socket.getOutputStream(), true);
            out.println("You are connected as client "+id);
        }catch(IOException e){
            e.printStackTrace();
        }
    }  

    public void readMessages() throws IOException{
        String inputMessage = "";
        String responseMessage = "";
        while(!(inputMessage = in.readUTF()).equals(STOP_STRING)){
            responseMessage = "Server Response: Hello Client " + id;
            System.out.println("Received from client " +id +" : "+inputMessage);
            out.println(responseMessage);
        }
    }
    
    public void close() throws IOException{
        in.close();
        out.close();
    }
}
