package models.tcp;

import enums.RequestType;
import java.io.Serializable;

public class Request implements Serializable { // упаковщики в JSON, передача через сокет
    private static final long serialVersionUID = 1L;//любые данные без изменения структуры сообщения
    private RequestType requestType; // не просто объектами
    private String payload;

    public Request() {}

    public Request(RequestType requestType, String payload) {
        this.requestType = requestType;
        this.payload = payload;
    }

    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}