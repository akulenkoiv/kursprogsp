package client.utility;
//tcp - соединение, ЖЦ соединения , s (все контроллеры - 1 соед)
import enums.ResponseStatus;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User currentUser;

    private ClientSocket() {}

    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendRequest(Request request) {
        if (out != null) {
            out.println(GsonUtil.getGson().toJson(request));
            out.flush();
        }
    }

    public Response readResponse() {
        try {
            if (in != null) {
                String json = in.readLine();
                if (json != null) {
                    return GsonUtil.getGson().fromJson(json, Response.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response(ResponseStatus.ERROR, "Network error", null);
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            currentUser = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}