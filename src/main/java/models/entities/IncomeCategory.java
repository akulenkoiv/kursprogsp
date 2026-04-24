package models.entities;

import java.io.Serializable;

public class IncomeCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private boolean taxable;
    private boolean deleted;

    public IncomeCategory() {}

    public IncomeCategory(String name, boolean taxable) {
        this.name = name;
        this.taxable = taxable;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isTaxable() { return taxable; }
    public void setTaxable(boolean taxable) { this.taxable = taxable; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}