package telran.net;

import java.io.*;
import java.net.*;
import java.time.Instant;

import org.json.JSONObject;

import telran.net.exceptions.ServerUnavailableException;

import static telran.net.TcpConfigurationProperties.*;

public class TcpClient implements Closeable, NetworkClient {
    Socket socket;
    PrintStream writer;
    BufferedReader reader;
    int interval;
    int nTrials;
    String host;
    int port;

    public TcpClient(String host, int port, int interval, int nTrials) {
        this.interval = interval;
        this.port = port;
        this.host = host;
        this.nTrials = nTrials;
        connect();
    }

    public TcpClient(String host, int port) {
        this(host, port, DEFAULT_INTERVAL_CONNECTION, DEFAULT_TRIALS_NUMBER_CONNECTION);
    }

    private void connect() {
        int count = nTrials;
        do {
            try {
                socket = new Socket(host, port);
                writer = new PrintStream(socket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                count = 0;
            } catch (IOException e) {
                waitForInterval();
                count--;
            }

        } while (count != 0);
        if(socket == null) {
            throw new ServerUnavailableException(host, port);
        }
    }

    private void waitForInterval() {
        Instant finish = Instant.now().plusMillis(interval);
        while(Instant.now().isBefore(finish));
    }

    @Override
    public void close() throws IOException {
        if(socket != null) {
            socket.close();
        }
       
    }
    @Override
    public String sendAndReceive(String requestType, String requestData) {
        Request request = new Request(requestType, requestData);
       
        try {
            if(socket == null) {
                throw new ServerUnavailableException(host, port);
            }
            writer.println(request);
            String responseJSON = reader.readLine();
            JSONObject jsonObj = new JSONObject(responseJSON);
            ResponseCode responseCode = jsonObj.getEnum(ResponseCode.class, RESPONSE_CODE_FIELD);
            String responseData = jsonObj.getString(RESPONSE_DATA_FIELD);
            if(responseCode != ResponseCode.OK) {
                throw new RuntimeException(responseData);
            }
            return responseData;
        } catch (IOException e) {
            connect();
            throw new ServerUnavailableException(host, port);
        }
    }

}