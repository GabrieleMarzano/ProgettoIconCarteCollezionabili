package com.progettoicon.APIYugiohPrices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campi non riconosciuti

public class CardData {

    private int id;
    private String name;
    private String print_tag;
    private String rarity;
    private PriceData price_data;

    public CardData(int id, String name, String print_tag, String rarity, PriceData price_data) {
        this.id = id;
        this.name = name;
        this.print_tag = print_tag;
        this.rarity = rarity;
        this.price_data = price_data;
    }

    public CardData() {
    }

    // Getters e Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrint_tag() {
        return print_tag;
    }

    public void setPrint_tag(String print_tag) {
        this.print_tag = print_tag;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public PriceData getPrice_data() {
        return price_data;
    }

    public void setPrice_data(PriceData price_data) {
        this.price_data = price_data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}