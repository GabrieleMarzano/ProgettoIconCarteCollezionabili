package com.progettoicon.APIYugiohPrices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceData {
    private String status;
    private PriceDetails data;

    // Getters e Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PriceDetails getData() {
        return data;
    }

    public void setData(PriceDetails data) {
        this.data = data;
    }
}