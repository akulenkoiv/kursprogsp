package client.utility;

import enums.ResponseStatus;
import models.entities.User;
import models.tcp.Request;
import models.tcp.Response;
import utility.GsonUtil;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User currentUser;
    private volatile boolean connected = false;

    private static final int SOCKET_TIMEOUT = 30000;

    private ClientSocket() {}

    public static synchronized ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public boolean connect(String host, int port) {
        synchronized (this) {
            forceDisconnect();

            try {
                socket = new Socket(host, port);
                socket.setSoTimeout(SOCKET_TIMEOUT);
                socket.setKeepAlive(true);
                socket.setTcpNoDelay(true);
                socket.setReuseAddress(true);

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                connected = true;
                System.out.println("[ClientSocket] Connected to " + host + ":" + port);
                return true;
            } catch (IOException e) {
                System.err.println("[ClientSocket] Connection failed: " + e.getMessage());
                connected = false;
                return false;
            }
        }
    }

    private void forceDisconnect() {
        connected = false;
        currentUser = null;

        Socket oldSocket = socket;
        BufferedReader oldIn = in;
        PrintWriter oldOut = out;

        socket = null;
        in = null;
        out = null;

        try {
            if (oldOut != null) {
                oldOut.flush();
                oldOut.close();
            }
        } catch (Exception ignored) {}

        try {
            if (oldIn != null) oldIn.close();
        } catch (Exception ignored) {}

        try {
            if (oldSocket != null && !oldSocket.isClosed()) {
                oldSocket.setSoLinger(true, 0);
                oldSocket.close();
            }
        } catch (Exception ignored) {}
    }

    public void disconnect() {
        synchronized (this) {
            forceDisconnect();
            System.out.println("[ClientSocket] Disconnected from server");
        }
    }

    public Response sendRequestSync(Request request) {
        synchronized (this) {
            if (!connected || socket == null || socket.isClosed() || out == null || in == null) {
                System.err.println("[ClientSocket] Not connected, cannot send request");
                return new Response(ResponseStatus.ERROR, "Not connected to server", null);
            }

            try {
                out.println(GsonUtil.getGson().toJson(request));
                out.flush();

                String json = in.readLine();
                if (json == null || json.trim().isEmpty()) {
                    connected = false;
                    return new Response(ResponseStatus.ERROR, "Server closed connection", null);
                }

                Response response = GsonUtil.getGson().fromJson(json, Response.class);
                return response != null ? response : new Response(ResponseStatus.ERROR, "Invalid response", null);

            } catch (SocketTimeoutException e) {
                connected = false;
                return new Response(ResponseStatus.ERROR, "Connection timeout", null);
            } catch (SocketException e) {
                connected = false;
                return new Response(ResponseStatus.ERROR, "Connection lost: " + e.getMessage(), null);
            } catch (Exception e) {
                connected = false;
                return new Response(ResponseStatus.ERROR, "Network error: " + e.getMessage(), null);
            }
        }
    }

    public void sendRequest(Request request) {
        synchronized (this) {
            if (connected && out != null && socket != null && !socket.isClosed()) {
                try {
                    out.println(GsonUtil.getGson().toJson(request));
                    out.flush();
                    if (out.checkError()) connected = false;
                } catch (Exception e) {
                    connected = false;
                }
            }
        }
    }

    public Response readResponse() {
        synchronized (this) {
            if (!connected || in == null) {
                return new Response(ResponseStatus.ERROR, "Not connected", null);
            }
            try {
                String json = in.readLine();
                if (json != null && !json.isEmpty()) {
                    return GsonUtil.getGson().fromJson(json, Response.class);
                } else {
                    connected = false;
                    return new Response(ResponseStatus.ERROR, "Connection closed", null);
                }
            } catch (SocketTimeoutException e) {
                connected = false;
                return new Response(ResponseStatus.ERROR, "Read timeout", null);
            } catch (SocketException e) {
                connected = false;
                return new Response(ResponseStatus.ERROR, "Connection lost: " + e.getMessage(), null);
            } catch (Exception e) {
                connected = false;
                return new Response(ResponseStatus.ERROR, "Read error: " + e.getMessage(), null);
            }
        }
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }
    public boolean isConnected() { return connected && socket != null && !socket.isClosed(); }
}