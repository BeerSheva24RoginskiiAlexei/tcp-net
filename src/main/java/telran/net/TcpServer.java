package telran.net;
import java.net.*;
import java.util.concurrent.*;
public class TcpServer implements Runnable {
    private Protocol protocol;
    private int port;
    private volatile boolean isShuttingDown = false;
    private final ExecutorService executor;

    public TcpServer(Protocol protocol, int port, int threadPoolSize) {
        this.protocol = protocol;
        this.port = port;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000); 
            System.out.println("Server is listening on port " + port);

            while (!isShuttingDown) {
                try {
                    Socket socket = serverSocket.accept();
                    executor.submit(new TcpClientServerSession(protocol, socket));
                } catch (SocketTimeoutException e) {
                    if (isShuttingDown) {
                        break;
                    }
                }
            }

            executor.shutdown(); 
            executor.awaitTermination(10, TimeUnit.SECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        isShuttingDown = true;
        executor.shutdownNow(); 
    }
}