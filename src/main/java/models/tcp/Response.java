package models.tcp;

import enums.ResponseStatus;
import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;
    private ResponseStatus status;
    private String message;
    private String data;

    public Response() {}

    public Response(ResponseStatus status, String message, String data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ResponseStatus getStatus() { return status; }
    public void setStatus(ResponseStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}