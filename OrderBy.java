package com.ast.mapping;

import java.util.HashMap;

public class OrderBy {

    HashMap<String,String> filtre;

    public OrderBy() {
        this.filtre = new HashMap<>();
    }

    public HashMap<String, String> getFiltre() {
        return filtre;
    }

    public void setFiltre(HashMap<String, String> filtre) {
        this.filtre = filtre;
    }

    public void addFiltre(String column,String order) {
        this.filtre.put(column,order);
    }
}
