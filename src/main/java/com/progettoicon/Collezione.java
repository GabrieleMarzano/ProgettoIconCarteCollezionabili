package com.progettoicon;

import java.util.ArrayList;
import java.util.List;

public class Collezione {
    private int id;
    private List<Carta> cartePossedute;
    private List<Carta> carteDesideri;
    private List<Deck> decks;

    // Costruttore vuoto
    public Collezione() {
        this.cartePossedute = new ArrayList<>();
        this.carteDesideri = new ArrayList<>();
        this.decks = new ArrayList<>();
    }

    // Costruttore
    public Collezione(int id) {
        this.id = id;
        this.cartePossedute = new ArrayList<>();
        this.carteDesideri = new ArrayList<>();
        this.decks = new ArrayList<>();
    }

    public Collezione(int id, List<Carta> cartePossedute, List<Carta> carteDesideri, List<Deck> decks) {
        this.id = id;
        this.cartePossedute = cartePossedute;
        this.carteDesideri = carteDesideri;
        this.decks = decks;
    }

    // Getter e Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Carta> getCartePossedute() {
        return cartePossedute;
    }

    public void setCartePossedute(List<Carta> cartePossedute) {
        this.cartePossedute = cartePossedute;
    }

    public List<Carta> getCarteDesideri() {
        return carteDesideri;
    }

    public void setCarteDesideri(List<Carta> carteDesideri) {
        this.carteDesideri = carteDesideri;
    }

    public List<Deck> getDecks() {
        return decks;
    }

    public void setDecks(List<Deck> decks) {
        this.decks = decks;
    }

    // Aggiungi una carta alla lista delle carte possedute
    public void aggiungiCartaPosseduta(Carta carta) {
        if (!cartePossedute.contains(carta)) {
            cartePossedute.add(carta);
            System.out.println("Carta aggiunta alle carte possedute: " + carta.getName());
        } else {
            System.out.println("Questa carta è già nelle carte possedute.");
        }
    }

    // Aggiungi una carta alla lista delle carte desiderate
    public void aggiungiCartaDesiderata(Carta carta) {
        if (!carteDesideri.contains(carta)) {
            carteDesideri.add(carta);
            System.out.println("Carta aggiunta alla lista dei desideri: " + carta.getName());
        } else {
            System.out.println("Questa carta è già nei desideri.");
        }
    }

    // Aggiungi un deck alla collezione
    public void aggiungiDeck(Deck deck) {
        decks.add(deck);
        System.out.println("Deck aggiunto alla collezione: " + deck.getName());
    }

    // Rimuovi una carta dalle carte possedute
    public boolean rimuoviCartaPosseduta(Carta carta) {
        if (cartePossedute.contains(carta)) {
            cartePossedute.remove(carta);
            System.out.println("Carta rimossa dalle carte possedute: " + carta.getName());
            return true;
        } else {
            System.out.println("La carta non è nelle carte possedute.");
            return false;
        }
    }

    // Rimuovi una carta dai desideri
    public boolean rimuoviCartaDesiderata(Carta carta) {
        if (carteDesideri.contains(carta)) {
            carteDesideri.remove(carta);
            System.out.println("Carta rimossa dai desideri: " + carta.getName());
            return true;
        } else {
            System.out.println("La carta non è nei desideri.");
            return false;
        }
    }

    // Rimuovi un deck dalla collezione
    public boolean rimuoviDeck(Deck deck) {
        if (decks.contains(deck)) {
            decks.remove(deck);
            System.out.println("Deck rimosso dalla collezione: " + deck.getName());
            return true;
        } else {
            System.out.println("Il deck non è nella collezione.");
            return false;
        }

    }

    // Metodo toString per visualizzare le informazioni della collezione
    @Override
    public String toString() {
        return "Collezione{" +
                "id=" + id +
                ", cartePossedute=" + cartePossedute.size() + " carte" +
                ", carteDesideri=" + carteDesideri.size() + " carte" +
                ", decks=" + decks.size() + " deck" +
                '}';
    }
}