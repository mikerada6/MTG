package rez.mtg.price.magic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.sql.Date;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Builder
public
class Card {

    @Id
    String id;
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

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinTable(name = "card_formats",
            joinColumns = { @JoinColumn(name = "card_id") },
            inverseJoinColumns = { @JoinColumn(name = "format_id") })
    @JsonIgnore
    List<Format> formats;


    @JsonIgnore
    @OneToMany(mappedBy = "card")
    private Collection<CardPurchaseAssociation> cardPurchaseAssociation;
}
