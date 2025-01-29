package com.progettoicon.ReteNeurale;

public class CardTrainingRecord {
    private int idCard1;
    private int idCard2;
    private double compatibility;

    // Costruttore
    public CardTrainingRecord(int idCard1, int idCard2, double compatibility) {
        this.idCard1 = idCard1;
        this.idCard2 = idCard2;
        this.compatibility = compatibility;
    }

    // Getter e Setter
    public int getIdCard1() {
        return idCard1;
    }

    public void setIdCard1(int idCard1) {
        this.idCard1 = idCard1;
    }

    public int getIdCard2() {
        return idCard2;
    }

    public void setIdCard2(int idCard2) {
        this.idCard2 = idCard2;
    }

    public double getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(double compatibility) {
        this.compatibility = compatibility;
    }

    @Override
    public String toString() {
        return "CardTrainingRecord{" +
                "idCard1=" + idCard1 +
                ", idCard2=" + idCard2 +
                ", compatibility=" + compatibility +
                '}';
    }
}