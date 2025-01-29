package com.progettoicon.InfoCarte;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campi non riconosciuti

public class Misc_info {

    private String beta_name;
    private String views;
    private String viewsweek;
    private String upvotes;
    private String downvotes;
    private List<String> formats;
    private String beta_id;
    private String tcg_date;
    private String ocg_date;
    private String konami_id;
    private String has_effect;
    private String md_rarity;

    public Misc_info() {

    }

    public Misc_info(String beta_name, String views, String viewsweek, String upvotes, String downvotes,
            List<String> formats, String beta_id, String tcg_date, String ocg_date, String konami_id, String has_effect,
            String md_rarity) {
        this.beta_name = beta_name;
        this.views = views;
        this.viewsweek = viewsweek;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.formats = formats;
        this.beta_id = beta_id;
        this.tcg_date = tcg_date;
        this.ocg_date = ocg_date;
        this.konami_id = konami_id;
        this.has_effect = has_effect;
        this.md_rarity = md_rarity;
    }

    public String getBeta_name() {
        return beta_name;
    }

    public void setBeta_name(String beta_name) {
        this.beta_name = beta_name;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getViewsweek() {
        return viewsweek;
    }

    public void setViewsweek(String viewsweek) {
        this.viewsweek = viewsweek;
    }

    public String getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(String upvotes) {
        this.upvotes = upvotes;
    }

    public String getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(String downvotes) {
        this.downvotes = downvotes;
    }

    public List<String> getFormats() {
        return formats;
    }

    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    public String getBeta_id() {
        return beta_id;
    }

    public void setBeta_id(String beta_id) {
        this.beta_id = beta_id;
    }

    public String getTcg_date() {
        return tcg_date;
    }

    public void setTcg_date(String tcg_date) {
        this.tcg_date = tcg_date;
    }

    public String getOcg_date() {
        return ocg_date;
    }

    public void setOcg_date(String ocg_date) {
        this.ocg_date = ocg_date;
    }

    public String getKonami_id() {
        return konami_id;
    }

    public void setKonami_id(String konami_id) {
        this.konami_id = konami_id;
    }

    public String getHas_effect() {
        return has_effect;
    }

    public void setHas_effect(String has_effect) {
        this.has_effect = has_effect;
    }

    public String getMd_rarity() {
        return md_rarity;
    }

    public void setMd_rarity(String md_rarity) {
        this.md_rarity = md_rarity;
    }

    @Override
    public String toString() {
        return "Misc_info [beta_name=" + beta_name + ", views=" + views + ", viewsweek=" + viewsweek + ", upvotes="
                + upvotes + ", downvotes=" + downvotes + ", formats=" + formats + ", beta_id=" + beta_id + ", tcg_date="
                + tcg_date + ", ocg_date=" + ocg_date + ", konami_id=" + konami_id + ", has_effect=" + has_effect
                + ", md_rarity=" + md_rarity + "]";
    }

}
