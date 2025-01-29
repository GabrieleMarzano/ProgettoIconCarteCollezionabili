package com.progettoicon.InfoCarte;

public class CardPrices {
    private String cardmarket_price;
    private String ebay_price;
    private String amazon_price;
    private String coolstuffinc_price;
    private String tcgplayer_price;

    // Costruttore vuoto
    public CardPrices() {
    }

    // Costruttore con parametri (tutti i prezzi come stringhe)
    public CardPrices(String cardmarket_price, String ebay_price, String amazon_price, String coolstuffinc_price,
            String tcgplayer_price) {
        this.cardmarket_price = cardmarket_price;
        this.ebay_price = ebay_price;
        this.amazon_price = amazon_price;
        this.coolstuffinc_price = coolstuffinc_price;
        this.tcgplayer_price = tcgplayer_price;
    }

    // Getter e Setter
    public String getCardmarket_price() {
        return cardmarket_price;
    }

    public void setCardmarket_price(String cardmarket_price) {
        this.cardmarket_price = cardmarket_price;
    }

    public String getEbay_price() {
        return ebay_price;
    }

    public void setEbay_price(String ebay_price) {
        this.ebay_price = ebay_price;
    }

    public String getAmazon_price() {
        return amazon_price;
    }

    public void setAmazon_price(String amazon_price) {
        this.amazon_price = amazon_price;
    }

    public String getCoolstuffinc_price() {
        return coolstuffinc_price;
    }

    public void setCoolstuffinc_price(String coolstuffinc_price) {
        this.coolstuffinc_price = coolstuffinc_price;
    }

    public String getTcgplayer_price() {
        return tcgplayer_price;
    }

    public void setTcgplayer_price(String tcgplayer_price) {
        this.tcgplayer_price = tcgplayer_price;
    }

    @Override
    public String toString() {
        return "CardPrices{" +
                "cardmarket_price='" + cardmarket_price + '\'' +
                ", ebay_price='" + ebay_price + '\'' +
                ", amazon_price='" + amazon_price + '\'' +
                ", coolstuffinc_price='" + coolstuffinc_price + '\'' +
                ", tcgplayer_price='" + tcgplayer_price + '\'' +
                '}';
    }
}