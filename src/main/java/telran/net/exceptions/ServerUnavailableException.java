package telran.net.exceptions;

public class ServerUnavailableException extends IllegalStateException{
    public ServerUnavailableException(String host, int port){
        super(String.format("Server %s on port %d is unavailable, repeat later on", host, port));
    }
}