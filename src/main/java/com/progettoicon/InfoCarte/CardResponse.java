package com.progettoicon.InfoCarte;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.progettoicon.Carta;

public class CardResponse {
    @JsonProperty("data")
    private List<Carta> data;

    // Getter e Setter
    public List<Carta> getData() {
        return data;
    }

    public void setData(List<Carta> data) {
        this.data = data;
    }
}