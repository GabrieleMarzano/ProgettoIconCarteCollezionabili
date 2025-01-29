package com.progettoicon.InfoCarte;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Card_Set {

    private String set_name;
    private String set_code;
    private String num_of_cards;
    @JsonProperty("tcg_date")
    private String tcg_date;
    private String set_image;

    public Card_Set() {
    }

    public Card_Set(String set_name, String set_code, String num_of_cards, String tcg_date, String set_image) {
        this.set_name = set_name;
        this.set_code = set_code;
        this.num_of_cards = num_of_cards;
        this.tcg_date = tcg_date;
        this.set_image = set_image;
    }

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

    public String getNum_of_cards() {
        return num_of_cards;
    }

    public void setNum_of_cards(String num_of_cards) {
        this.num_of_cards = num_of_cards;
    }

    public String getDateTgc() {
        return tcg_date;
    }

    public void setDateTgc(String tcg_date) {
        this.tcg_date = tcg_date;
    }

    public String getSet_image() {
        return set_image;
    }

    public void setSet_image(String set_image) {
        this.set_image = set_image;
    }

}
