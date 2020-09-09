package rez.mtg.price.magic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public
class Card implements Comparable<Card>{
    private static final Logger logger = LoggerFactory.getLogger(Card.class);


    @Id
    String id;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "card_formats", joinColumns = {@JoinColumn(name = "card_id")}, inverseJoinColumns = {
            @JoinColumn(name = "format_id")})
    @JsonIgnore
    List<Format> formats;
    private String name;
    private Color color;
    private Rarity rarity;
    private String collector_number;
    private double cmc;
    private String typeLine;
    private String set_name;
    private Date released_at;
    @JsonIgnore
    @OneToMany(mappedBy = "card")
    private Collection<Price> price;
    private double printNumber;
    private boolean promo;
    private boolean variation;
    private String lang;
    private String manaCost;
    private String uri;
    private String oracleText;
    private String set;
    private boolean reserved;
    @JsonIgnore
    @OneToMany(mappedBy = "card")
    private Collection<CardPurchaseAssociation> cardPurchaseAssociation;


    public
    Card() {
        name = null;
        color = null;
        rarity = null;
        collector_number = "";
        cmc = -1;
    }

    public static
    Logger getLogger() {
        return logger;
    }

    public
    String getId() {
        return id;
    }

    public
    void setId(String id) {
        this.id = id;
    }

    public
    String getTypeLine() {
        return typeLine;
    }

    public
    void setTypeLine(String typeLine) {
        this.typeLine = typeLine;
    }


    public
    Color getColor() {
        return color;
    }

    public
    void setColor(Color color) {
        this.color = color;
    }

    public
    void setColor(String color) {
        this.color = Color.fromLabel(color);
    }

    public
    Rarity getRarity() {
        return rarity;
    }

    public
    void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public
    void setRarity(String rarity) {
        this.rarity = Rarity.fromLabel(rarity);
    }

    public
    String getCollector_number() {
        return collector_number;
    }

    public
    void setCollector_number(String printNumber) {
        this.collector_number = printNumber;
    }

    public
    double getCmc() {
        return cmc;
    }

    public
    void setCmc(double cmc) {
        this.cmc = cmc;
    }

    public
    String getName() {
        return name;
    }

    public
    void setName(String name) {
        this.name = name;
    }

    @Override
    public
    int compareTo(Card o) {
        int thisNum = this.convertCollectorNumber();
        int oNum = o.convertCollectorNumber();
        if (thisNum == oNum) {
            return 0;
        } else if (thisNum > oNum) {
            return 1;
        } else {
            return -1;
        }
    }

    private
    HashMap<String, Object> createMapping(String methodName, Object dataType) {
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("methodName",
                 methodName);
        temp.put("dataType",
                 dataType);
        return temp;
    }

    public
    boolean setValueString(String variableName, String value) {
        String methodName = "set" +
                            variableName
                                    .substring(0,
                                               1)
                                    .toUpperCase() +
                            variableName.substring(1);
        Method method = null;
        HashMap<String, HashMap<String, Object>> scryFallMapping = new HashMap<>();
        scryFallMapping.put("name",
                            createMapping("setName",
                                          String.class));
        scryFallMapping.put("id",
                            createMapping("setId",
                                          String.class));
        scryFallMapping.put("type_line",
                            createMapping("setTypeFromScryFall",
                                          String.class));
        scryFallMapping.put("lang",
                            createMapping("setLang",
                                          String.class));
        scryFallMapping.put("rarity",
                            createMapping("setRarity",
                                          String.class));
        scryFallMapping.put("collector_number",
                            createMapping("setCollector_number",
                                          String.class));
        scryFallMapping.put("cmc",
                            createMapping("setCmc",
                                          double.class));
        scryFallMapping.put("released_at",
                            createMapping("setReleased_at",
                                          Date.class));
        scryFallMapping.put("collector_number",
                            createMapping("setCollector_number",
                                          String.class));
        scryFallMapping.put("promo",
                            createMapping("setPromo",
                                          boolean.class));
        scryFallMapping.put("variation",
                            createMapping("setVariation",
                                          boolean.class));
        scryFallMapping.put("set_name",
                            createMapping("setSet_name",
                                          String.class));
        scryFallMapping.put("mana_cost",
                            createMapping("setManaCost",
                                          String.class));
        scryFallMapping.put("uri",
                            createMapping("setURI",
                                          String.class));
        scryFallMapping.put("oracle_text",
                            createMapping("setOracleText",
                                          String.class));
        scryFallMapping.put("set",
                            createMapping("setSet",
                                          String.class));

        if (!scryFallMapping.containsKey(variableName)) {
            logger.error("Could not find where to put variable {}.  Error: ",
                         variableName);
            return false;
        }
        Class dataType = (Class) scryFallMapping.get(variableName).get("dataType");

        try {
            method = Card.class.getDeclaredMethod(scryFallMapping.get(variableName).get("methodName").toString(),
                                                  dataType);
        } catch (NoSuchMethodException ex) {
            logger.error("Could not find method {}.  Error: ",
                         methodName,
                         ex);
            return false;
        }
        try {
            if (dataType.equals(int.class)) {
                method.invoke(this,
                              Integer.parseInt(value));
            } else if (dataType.equals(double.class)) {
                method.invoke(this,
                              Double.parseDouble(value));
            } else if (dataType.equals(String.class)) {
                method.invoke(this,
                              value);
            } else if (dataType.equals(boolean.class)) {
                method.invoke(this,
                              Boolean.parseBoolean(value));
            } else if (dataType.equals(Date.class)) {
                method.invoke(this,
                              Date.valueOf(value));
            } else {
                logger.error("Could not find an action for type {} ",
                             dataType);
                return false;
            }
        } catch (IllegalAccessException ex) {
            logger.error("IllegalAccessException {}.  Error: ",
                         methodName,
                         ex);
            return false;
        } catch (InvocationTargetException ex) {
            logger.error("IllegalAccessException {}.  Error: ",
                         methodName,
                         ex);
            return false;
        } catch (Exception ex) {
            logger.error("Exception {}.  Error: ",
                         methodName,
                         ex);
            return false;
        }
        return true;
    }

    public
    Date getReleased_at() {
        return released_at;
    }

    public
    void setReleased_at(Date released_at) {
        this.released_at = released_at;
    }

    public
    String getSet_name() {
        return set_name;
    }

    public
    void setSet_name(String set) {
        this.set_name = set;
    }


    public
    Collection<Price> getPrice() {
        return price;
    }

    public
    void setPrice(Collection<Price> price) {
        this.price = price;
    }

    public
    Map<String, Price> priceHashMap() {
        return price
                .stream()
                .collect(Collectors.toMap(s -> s.getDate().toString(),
                                          s -> s));
    }

    public
    double getPrintNumber() {
        return printNumber;
    }

    public
    void setPrintNumber(double printNumber) {
        this.printNumber = printNumber;
    }

    public
    boolean isPromo() {
        return promo;
    }

    public
    void setPromo(boolean promo) {
        this.promo = promo;
    }

    public
    boolean isVariation() {
        return variation;
    }

    public
    void setVariation(boolean variation) {
        this.variation = variation;
    }

    public
    String getLang() {
        return lang;
    }

    public
    void setLang(String lang) {
        this.lang = lang;
    }

    public
    String getManaCost() {
        return manaCost;
    }

    public
    void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    public
    String getURI() {
        return uri;
    }

    public
    void setURI(String URI) {
        this.uri = URI;
    }

    public
    String getOracleText() {
        return oracleText;
    }

    public
    void setOracleText(String oracleText) {
        this.oracleText = oracleText;
    }

    public
    String getSet() {
        return set;
    }

    public
    void setSet(String set) {
        this.set = set;
    }

    public
    boolean isReserved() {
        return reserved;
    }

    public
    void setReserved(boolean reserved) {
        this.reserved = reserved;
    }


    public
    Collection<CardPurchaseAssociation> getCardPurchaseAssociation() {
        return cardPurchaseAssociation;
    }

    public
    void setCardPurchaseAssociation(Collection<CardPurchaseAssociation> cardPurchaseAssociation) {
        this.cardPurchaseAssociation = cardPurchaseAssociation;
    }

    public
    List<Format> getFormats() {
        return formats;
    }

    public
    void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    public
    void addFormat(Format format) {
        if (formats == null) {
            formats = new ArrayList<Format>();
        }
        formats.add(format);
    }

    /**
     * This method will take the collector number that is given to us by scryfall and convert it to an int removing
     * all non integerß numbers
     *
     * @return
     */
    public
    int convertCollectorNumber() {
        String temp = "";
        String[] split = collector_number.split("");

        for (String a : split) {
            try {
                temp += Integer.parseInt(a);
            } catch (Exception e) {

            }
        }
        return Integer.parseInt(temp);
    }

    public
    class FirstNameSorter implements Comparator<Card> {
        public
        int compare(Card o1, Card o2) {
            return 0;
        }
    }

    public
    class LastNameSorter implements Comparator<Card> {
        public
        int compare(Card o1, Card o2) {
            return 0;
        }
    }

    public
    class AgeSorter implements Comparator<Card> {
        public
        int compare(Card o1, Card o2) {
            return 0;
        }
    }
}
