package com.progettoicon;

import java.util.List;

public class Deck {
    private int id;
    private String name;
    private String description;
    private boolean isPublic;
    private List<Integer> mainDeckCardIds;
    private List<Integer> extraDeckCardIds;
    private List<Integer> sideDeckCardIds;

    // Getter e Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<Integer> getMainDeckCardIds() {
        return mainDeckCardIds;
    }

    public void setMainDeckCardIds(List<Integer> mainDeckCardIds) {
        this.mainDeckCardIds = mainDeckCardIds;
    }

    public List<Integer> getExtraDeckCardIds() {
        return extraDeckCardIds;
    }

    public void setExtraDeckCardIds(List<Integer> extraDeckCardIds) {
        this.extraDeckCardIds = extraDeckCardIds;
    }

    public List<Integer> getSideDeckCardIds() {
        return sideDeckCardIds;
    }

    public void setSideDeckCardIds(List<Integer> sideDeckCardIds) {
        this.sideDeckCardIds = sideDeckCardIds;
    }
}