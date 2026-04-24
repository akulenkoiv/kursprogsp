package enums;

public enum TaxRegime {
    USN_INCOME(0.06), USN_INCOME_EXPENSE(0.15);

    private final double rate;

    TaxRegime(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}