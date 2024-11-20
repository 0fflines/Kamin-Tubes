import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket server;
    private DataInputStream in;
    private PrintWriter out;
    public static final int PORT = 3030;
    public static final String STOP_STRING = ",,";
    private Socket clientSocket;

    public Server(){
        try{
            server = new ServerSocket(PORT);
            iniConnections();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void iniConnections() throws IOException{
        System.out.println("Server started");
        clientSocket = server.accept();
        System.out.println("Client socket accepted");
        in = new DataInputStream(clientSocket.getInputStream());
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println("You're now connected to the Server! Type in "+STOP_STRING +" to stop the program");
        readMessages();
        close();
    }

    public void close() throws IOException{
        in.close();
        out.close();
        server.close();
    }

    public void readMessages() throws IOException{
        String inputMessage = "";
        String responseMessage = "";
        while(!inputMessage.equals(STOP_STRING)){
            inputMessage = in.readUTF();
            responseMessage = "Server Response: Hello " + inputMessage;
            System.out.println("Received from client: "+inputMessage);
            out.println(responseMessage);
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
