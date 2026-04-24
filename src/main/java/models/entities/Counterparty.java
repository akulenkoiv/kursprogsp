package models.entities;

import java.io.Serializable;

public class Counterparty implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String inn;
    private String contactInfo;
    private boolean deleted;

    public Counterparty() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}