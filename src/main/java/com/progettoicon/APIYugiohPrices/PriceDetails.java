package com.progettoicon.APIYugiohPrices;

import java.util.List;

public class PriceDetails {
    private List<Object> listings; // Puoi specificare una classe se conosci la struttura degli oggetti in
                                   // "listings"
    private Prices prices;

    // Getters e Setters
    public List<Object> getListings() {
        return listings;
    }

    public void setListings(List<Object> listings) {
        this.listings = listings;
    }

    public Prices getPrices() {
        return prices;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
    }
}