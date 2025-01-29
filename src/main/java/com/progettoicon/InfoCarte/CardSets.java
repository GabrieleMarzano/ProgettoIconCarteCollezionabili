package com.progettoicon.InfoCarte;

import com.progettoicon.APIYugiohPrices.Prices;

public class CardSets {
    private String set_name;
    private String set_code;
    private String set_rarity;
    private String set_rarity_code;
    private String set_price;
    private Prices prices;

    // Costruttore vuoto (necessario per Jackson)
    public CardSets() {
    }

    // Costruttore completo
    public CardSets(String set_name, String set_code, String set_rarity, String set_rarity_code, String set_price,
            Prices prices) {
        this.set_name = set_name;
        this.set_code = set_code;
        this.set_rarity = set_rarity;
        this.set_rarity_code = set_rarity_code;
        this.set_price = set_price;
        this.prices = prices;
    }

    // Getter e Setter
    public String getSet_name() {
        return set_name;
    }

    public void setSet_name(String set_name) {
        this.set_name = set_name;
    }

    public String getSet_code() {
        return set_code;
    }

    public void setSet_code(String set_code) {
        this.set_code = set_code;
    }

    public String getSet_rarity() {
        return set_rarity;
    }

    public void setSet_rarity(String set_rarity) {
        this.set_rarity = set_rarity;
    }

    public String getSet_rarity_code() {
        return set_rarity_code;
    }

    public void setSet_rarity_code(String set_rarity_code) {
        this.set_rarity_code = set_rarity_code;
    }

    public String getSet_price() {
        return set_price;
    }

    public void setSet_price(String set_price) {
        this.set_price = set_price;
    }

    @Override
    public String toString() {
        return "CardSet{" +
                "set_name='" + set_name + '\'' +
                ", set_code='" + set_code + '\'' +
                ", set_rarity='" + set_rarity + '\'' +
                ", set_rarity_code='" + set_rarity_code + '\'' +
                ", set_price='" + set_price + '\'' +
                '}';
    }

    public Prices getPrices() {
        return prices;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
    }
}