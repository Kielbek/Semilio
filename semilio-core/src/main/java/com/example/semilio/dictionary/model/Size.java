package com.example.semilio.dictionary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "sizes")
public class Size extends BaseDictionary {
    @Column(name = "type", length = 20)
    private String type;

    @Override
    public String getType() {
        return this.type;
    }
}
