package com.progettoicon.InfoCarte;

public class CardImage {
    private int id;
    private String image_url;
    private String image_url_small;
    private String image_url_cropped;

    // Costruttore vuoto (necessario per Jackson)
    public CardImage() {
    }

    // Costruttore completo
    public CardImage(int id, String image_url, String image_url_small, String image_url_cropped) {
        this.id = id;
        this.image_url = image_url;
        this.image_url_small = image_url_small;
        this.image_url_cropped = image_url_cropped;
    }

    // Getter e Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getImage_url_small() {
        return image_url_small;
    }

    public void setImage_url_small(String image_url_small) {
        this.image_url_small = image_url_small;
    }

    public String getImage_url_cropped() {
        return image_url_cropped;
    }

    public void setImage_url_cropped(String image_url_cropped) {
        this.image_url_cropped = image_url_cropped;
    }

    @Override
    public String toString() {
        return "CardImage{" +
                "id=" + id +
                ", image_url='" + image_url + '\'' +
                ", image_url_small='" + image_url_small + '\'' +
                ", image_url_cropped='" + image_url_cropped + '\'' +
                '}';
    }
}