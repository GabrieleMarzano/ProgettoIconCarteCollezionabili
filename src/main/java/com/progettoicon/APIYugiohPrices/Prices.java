package com.progettoicon.APIYugiohPrices;

public class Prices {
    private float high;
    private float low;
    private float average;
    private float shift;
    private float shift_3;
    private float shift_7;
    private float shift_21;
    private float shift_30;
    private float shift_90;
    private float shift_180;
    private float shift_365;
    private String updated_at;

    // Costruttore vuoto
    public Prices() {
    }

    // Costruttore con parametri
    public Prices(float high, float low, float average, float shift, float shift_3, float shift_7,
            float shift_21, float shift_30, float shift_90, float shift_180, float shift_365,
            String updated_at) {
        this.high = high;
        this.low = low;
        this.average = average;
        this.shift = shift;
        this.shift_3 = shift_3;
        this.shift_7 = shift_7;
        this.shift_21 = shift_21;
        this.shift_30 = shift_30;
        this.shift_90 = shift_90;
        this.shift_180 = shift_180;
        this.shift_365 = shift_365;
        this.updated_at = updated_at;
    }

    // Getters e Setters
    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getAverage() {
        return average;
    }

    public void setAverage(float average) {
        this.average = average;
    }

    public float getShift() {
        return shift;
    }

    public void setShift(float shift) {
        this.shift = shift;
    }

    public float getShift_3() {
        return shift_3;
    }

    public void setShift_3(float shift_3) {
        this.shift_3 = shift_3;
    }

    public float getShift_7() {
        return shift_7;
    }

    public void setShift_7(float shift_7) {
        this.shift_7 = shift_7;
    }

    public float getShift_21() {
        return shift_21;
    }

    public void setShift_21(float shift_21) {
        this.shift_21 = shift_21;
    }

    public float getShift_30() {
        return shift_30;
    }

    public void setShift_30(float shift_30) {
        this.shift_30 = shift_30;
    }

    public float getShift_90() {
        return shift_90;
    }

    public void setShift_90(float shift_90) {
        this.shift_90 = shift_90;
    }

    public float getShift_180() {
        return shift_180;
    }

    public void setShift_180(float shift_180) {
        this.shift_180 = shift_180;
    }

    public float getShift_365() {
        return shift_365;
    }

    public void setShift_365(float shift_365) {
        this.shift_365 = shift_365;
    }

    public String getUpdated_at() {

        return normalizeTimestamp(updated_at);
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public static String normalizeTimestamp(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isEmpty()) {
            throw new IllegalArgumentException("Il timestamp fornito è nullo o vuoto.");
        }

        // Trova l'indice dell'ultimo trattino
        int lastDashIndex = rawTimestamp.lastIndexOf("-");
        if (lastDashIndex > 0) {
            // Restituisce la sottostringa fino al carattere prima del trattino
            return rawTimestamp.substring(0, lastDashIndex).trim();
        }

        // Se non c'è un trattino, restituisce la stringa originale
        return rawTimestamp;
    }
}