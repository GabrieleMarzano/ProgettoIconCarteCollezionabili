package com.progettoicon;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.progettoicon.InfoCarte.Banlist_info;
import com.progettoicon.InfoCarte.CardImage;
import com.progettoicon.InfoCarte.CardPrices;
import com.progettoicon.InfoCarte.CardSets;
import com.progettoicon.InfoCarte.Misc_info;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campi non riconosciuti
public class Carta {

    private int id;
    private String name;
    private String type;
    private String race;
    private String atk;
    private String def;
    private String level;
    private String archetype;
    private String humanReadableCardType;
    private String frameType;
    private String desc;
    private String ygoprodeck_url;
    private String attribute;
    private boolean has_effect;
    private List<String> typeline;
    private List<CardPrices> card_prices; // Aggiungi il campo mancante
    private List<CardImage> card_images;
    private List<CardSets> card_sets;
    private Banlist_info banlist_info;
    private List<Misc_info> misc_info;

    public Carta() {

    }

    public Carta(int id, String name, String type, String race, String atk, String def, String level, String archetype,
            String humanReadableCardType, String frameType, String desc, String ygoprodeck_url,
            List<CardPrices> card_prices, List<CardImage> card_images, List<CardSets> card_sets,
            Banlist_info banlist_info, List<Misc_info> misc_info, List<String> typeline, String attribute,
            boolean has_effect) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.race = race;
        this.atk = atk;
        this.def = def;
        this.level = level;
        this.archetype = archetype;
        this.humanReadableCardType = humanReadableCardType;
        this.frameType = frameType;
        this.desc = desc;
        this.ygoprodeck_url = ygoprodeck_url;
        this.card_prices = card_prices;
        this.card_images = card_images;
        this.card_sets = card_sets;
        this.banlist_info = banlist_info;
        this.misc_info = misc_info;
        this.typeline = typeline;
        this.attribute = attribute;
        this.has_effect = has_effect;
    }

    public List<Misc_info> getMisc_info() {
        return misc_info;
    }

    public void setMisc_info(List<Misc_info> misc_info) {
        this.misc_info = misc_info;
    }

    public List<CardPrices> getCard_prices() {
        return card_prices;
    }

    public void setCard_prices(List<CardPrices> card_prices) {
        this.card_prices = card_prices;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getArchetype() {
        return archetype;
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    public String getHumanReadableCardType() {
        return humanReadableCardType;
    }

    public void setHumanReadableCardType(String humanReadableCardType) {
        this.humanReadableCardType = humanReadableCardType;
    }

    public String getFrameType() {
        return frameType;
    }

    public void setFrameType(String frameType) {
        this.frameType = frameType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getYgoprodeck_url() {
        return ygoprodeck_url;
    }

    public void setYgoprodeck_url(String ygoprodeck_url) {
        this.ygoprodeck_url = ygoprodeck_url;
    }

    @Override
    public String toString() {
        return "Carta {" + "\n" +
                "id=" + getId() + '\n' +
                "name='" + getName() + '\n' +
                "atk='" + getAtk() + '\n' +
                "def='" + getDef() + '\n' +
                "level='" + getLevel() + '\n' +
                "attribute='" + getAttribute() + '\n' +
                "type='" + getType() + '\n' +
                "typeline='" + getTypeline() + '\n' +
                "race='" + getRace() + '\n' +
                "archetype='" + getArchetype() + '\n' +
                "humanReadableCardType='" + getHumanReadableCardType() + '\n' +
                "frameType='" + getFrameType() + '\n' +
                "desc='" + getDesc() + '\n' +
                "ygoprodeck_url='" + getYgoprodeck_url() + '\n' +
                "card_prices=" + (getCard_prices() != null ? card_prices.toString() : "null") + '\n' +
                "card_images=" + (getCard_images() != null ? card_images.toString() : "null") + '\n' +
                "card_set=" + (getCard_sets() != null ? card_sets.toString() : "null") + '\n' +
                "banlist=" + (getBanlist_info() != null ? banlist_info.toString() : "null") + '\n' +
                "misc_info=" + (getMisc_info() != null ? misc_info.toString() : "null") + '\n' +
                '}';

    }

    public List<CardImage> getCard_images() {
        return card_images;
    }

    public void setCard_images(List<CardImage> card_images) {
        this.card_images = card_images;
    }

    public List<CardSets> getCard_sets() {
        return card_sets;
    }

    public void setCard_sets(List<CardSets> card_set) {
        this.card_sets = card_set;
    }

    public Banlist_info getBanlist_info() {
        return banlist_info;
    }

    public void setBanlist_info(Banlist_info banlist_info) {
        this.banlist_info = banlist_info;
    }

    public String getAtk() {
        return atk;
    }

    public void setAtk(String atk) {
        this.atk = atk;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getTypeline() {
        return typeline;
    }

    public void setTypeline(List<String> typeline) {
        this.typeline = typeline;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public boolean isHas_effect() {
        return has_effect;
    }

    public void setHas_effect(boolean has_effect) {
        this.has_effect = has_effect;
    }

}