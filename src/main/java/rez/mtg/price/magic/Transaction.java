package rez.mtg.price.magic;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.sql.Date;
import java.util.Collection;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vednor_id", nullable = false)
    @JsonIgnore
    private Vendor vendor;
    private Date date;
    private double cost;
    @OneToMany(mappedBy = "transaction")
    private Collection<CardPurchaseAssociation> cardPurchaseAssociation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Collection<CardPurchaseAssociation> getCardPurchaseAssociation() {
        return cardPurchaseAssociation;
    }

    public void setCardPurchaseAssociation(Collection<CardPurchaseAssociation> cardPurchaseAssociation) {
        this.cardPurchaseAssociation = cardPurchaseAssociation;
    }
}
