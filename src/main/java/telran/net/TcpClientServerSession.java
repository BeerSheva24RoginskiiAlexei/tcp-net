package telran.net;

import java.net.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.*;

public class TcpClientServerSession implements Runnable {
    Protocol protocol;
    Socket socket;
    TcpServer server;
    int idleTimeout;
    int requestsPerSecond;
    int nonOkResponses;
    Instant timestamp = Instant.now();

    public TcpClientServerSession(Protocol protocol, Socket socket, TcpServer server) {
        this.protocol = protocol;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream writer = new PrintStream(socket.getOutputStream())) {
            String request = null;
            while (!server.executor.isShutdown() && !isIdleTimeout()) {
                try {
                    request = reader.readLine();
                    idleTimeout = 0;
                    if (request == null || isRequestsPerSecond()) {
                        break;
                    }
                    String response = protocol.getResponseWithJSON(request);
                    if (isNonOkResponses(response)) {
                        break;
                    }
                    writer.println(response);
                } catch (SocketTimeoutException e) {
                    idleTimeout += server.socketTimeout;
                }
            }
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private boolean isRequestsPerSecond() {
        Instant current = Instant.now();
        if (ChronoUnit.SECONDS.between(timestamp, current) > 1) {
            requestsPerSecond = 0;
            timestamp = current;
        } else {
            requestsPerSecond++;
        }
        return requestsPerSecond > server.limitRequestsPerSecond;
    }

    private boolean isNonOkResponses(String response) {
        nonOkResponses = response.contains("OK") ? 0 : nonOkResponses + 1;
        return nonOkResponses > server.limitNonOkResponsesInRow;
    }

    private boolean isIdleTimeout() {
        return idleTimeout > server.idleConnectionTimeout;
    }

}