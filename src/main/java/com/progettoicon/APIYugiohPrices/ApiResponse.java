package com.progettoicon.APIYugiohPrices;

import java.util.List;

public class ApiResponse {
    private String status;
    private List<CardData> data;

    // Getters e Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CardData> getData() {
        return data;
    }

    public void setData(List<CardData> data) {
        this.data = data;
    }
}