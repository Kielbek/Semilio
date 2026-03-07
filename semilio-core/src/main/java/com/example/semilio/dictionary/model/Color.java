package com.example.semilio.dictionary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "colors")
public class Color extends BaseDictionary {

    @Column(name = "hex_code", length = 7)
    private String hexCode;

    @Override
    public String getHexCode() {
        return this.hexCode;
    }
}
