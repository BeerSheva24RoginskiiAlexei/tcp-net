package telran.net;

import java.net.*;
import java.util.concurrent.*;

public class TcpServer implements Runnable {
    private static final int DEFAULT_SOCKET_TIMEOUT = 100;
    private static final int DEFAULT_IDLE_CONNECTION_TIMEOUT = 60000;
    private static final int DEFAULT_LIMIT_REQUESTS_PER_SEC = 5;
    private static final int DEFAULT_LIMIT_NON_OK_RESPONSES_IN_ROW = 10;
    Protocol protocol;
    int port;
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    int socketTimeout;
    int idleConnectionTimeout;
    int limitRequestsPerSecond;
    int limitNonOkResponsesInRow;

    public TcpServer(Protocol protocol, int port, int socketTimeout, int idleConnectionTimeout,
            int limitRequestsPerSecond, int limitNonOkResponsesInRow) {
        this.protocol = protocol;
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.idleConnectionTimeout = idleConnectionTimeout;
        this.limitRequestsPerSecond = limitRequestsPerSecond;
        this.limitNonOkResponsesInRow = limitNonOkResponsesInRow;
    }

    public TcpServer(Protocol protocol, int port, int idleConnectionTimeout) {
        this(protocol, port, DEFAULT_SOCKET_TIMEOUT, idleConnectionTimeout, DEFAULT_LIMIT_REQUESTS_PER_SEC,
                DEFAULT_LIMIT_NON_OK_RESPONSES_IN_ROW);
    }

    public TcpServer(Protocol protocol, int port) {
        this(protocol, port, DEFAULT_SOCKET_TIMEOUT, DEFAULT_IDLE_CONNECTION_TIMEOUT, DEFAULT_LIMIT_REQUESTS_PER_SEC,
                DEFAULT_LIMIT_NON_OK_RESPONSES_IN_ROW);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on the port " + port);
            serverSocket.setSoTimeout(socketTimeout);
            while (!executor.isShutdown()) {
                try {
                    Socket socket = serverSocket.accept();
                    socket.setSoTimeout(socketTimeout);
                    var session = new TcpClientServerSession(protocol, socket, this);
                    executor.execute(session);
                } catch (SocketTimeoutException e) {

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void shutdown() {
        executor.shutdownNow();

    }

}