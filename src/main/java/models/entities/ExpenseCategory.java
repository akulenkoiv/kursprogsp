package models.entities;

import java.io.Serializable;

public class ExpenseCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private boolean deductible;
    private boolean deleted;

    public ExpenseCategory() {}

    public ExpenseCategory(String name, boolean deductible) {
        this.name = name;
        this.deductible = deductible;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDeductible() { return deductible; }
    public void setDeductible(boolean deductible) { this.deductible = deductible; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}