package dev.kenzi.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int totalQuantity;

    private int issuedQuantity;

    protected Coupon() {
    }

    public Coupon(String name, int totalQuantity) {
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
    }

    public boolean isSoldOut() {
        return issuedQuantity >= totalQuantity;
    }

    public void issue() {
        issuedQuantity++;
    }

    public Long getId() {
        return id;
    }
}
