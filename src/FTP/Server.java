package FTP;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 * The Server class implements a program that creates a server socket to open connection between the server and the clients.
 */
public class Server {
    /**
     * This method Creates a server socket and waits to any client connection request to accept this request and the server will be connected with the client.
     * It creates a Thread object and call the constructor of class MultipleClients.
     * It calls the method run() in class MultipleClients.
     * If anything wrong happened in the connection between the server and the client it will throw an exception and the program will end.
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        try {
            ServerSocket serversocket = new ServerSocket(9000);
            System.out.println("Server is booted up and is waiting for clients to connect.");
            while (true) {
                Socket clientSocket = serversocket.accept();
                System.out.println("A new client [" + clientSocket + "] is connected to the server.");
                Thread client = new MultipleClients(clientSocket);
                client.start();
            }
        } catch (IOException e) {
            System.out.println("Problem with ServerSocket.");
        }
    }
}
