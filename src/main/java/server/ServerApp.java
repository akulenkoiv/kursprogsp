package server;

import config.Config;
import server.thread.ClientThread;
import utility.DBConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    public static void main(String[] args) {
        int port = Config.getIntProperty("server.port");
        int poolSize = Config.getIntProperty("server.thread.pool.size");
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize); //6

        try (ServerSocket serverSocket = new ServerSocket(port)) { //1
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept(); //2
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ClientThread clientThread = new ClientThread(clientSocket);
                threadPool.execute(clientThread); //3
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeDataSource();
        }
    }
}