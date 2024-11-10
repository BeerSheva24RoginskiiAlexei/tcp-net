package telran.net;
import java.net.*;
import java.io.*;
public class TcpClientServerSession implements Runnable{
    Protocol protocol;
    Socket socket;
    public TcpClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream writer = new PrintStream(socket.getOutputStream())) {
            String request = null;
            while((request = reader.readLine()) != null) {
                String response = protocol.getResponseWithJSON(request);
                writer.println(response);
            }
            socket.close();
        } catch (Exception e) {
           System.out.println(e);
        }
    }

}