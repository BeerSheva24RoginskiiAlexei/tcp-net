package telran.net;


public class ClientIdlTimeout {
 public static void main(String[] args) throws InterruptedException  {
        TcpClient tcpClient = new TcpClient("localhost", 5000);
        
        try{
                tcpClient.sendAndReceive("ok", "");
                Thread.sleep(60000);
                tcpClient.sendAndReceive("ok", "");
                System.out.println("request");
               
            } catch(RuntimeException e) {
                System.out.println("Server closed connection");
               
            }
           

           
        }
       
    }