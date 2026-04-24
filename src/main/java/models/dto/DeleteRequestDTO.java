package models.dto;

import java.io.Serializable;

public class DeleteRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;

    public DeleteRequestDTO() {}

    public DeleteRequestDTO(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}