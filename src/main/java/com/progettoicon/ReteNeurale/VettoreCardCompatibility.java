package com.progettoicon.ReteNeurale;

public class VettoreCardCompatibility {
    private String vettore;
    private Double target;
    private double[] vet;

    public VettoreCardCompatibility(String vettore, Double target) {
        this.vettore = vettore;
        this.target = target;
    }

    public VettoreCardCompatibility(double[] vet, Double target) {
        this.vet = vet;
        this.target = target;
    }

    public VettoreCardCompatibility(double[] vet) {
        this.vet = vet;
    }

    public double[] getVettore() {

        double[] result = null;

        if (vettore == null || vettore.isBlank()) {
            throw new IllegalArgumentException("La stringa di input non può essere null o vuota.");
        }

        try {
            // Dividi la stringa in base alla virgola e mappa ogni elemento in un double
            String[] parts = vettore.split(",");
            result = new double[parts.length];

            for (int i = 0; i < parts.length; i++) {
                result[i] = Double.parseDouble(parts[i].trim()); // Rimuove eventuali spazi
            }

            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La stringa contiene valori non validi per un double.", e);
        }

    }

    public void setVettore(String vettore) {
        this.vettore = vettore;
    }

    public Double getTarget() {
        return target;
    }

    public void setTarget(Double target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "VettoreCardCompatibility{" +
                "vettore='" + vettore + '\'' +
                ", target=" + target +
                '}';
    }

    public double[] getVet() {
        return vet;
    }

    public void setVet(double[] vet) {
        this.vet = vet;
    }
}