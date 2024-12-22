package com.ast.mapping;

import java.util.HashMap;

public class CreateTable {
    String table;
    HashMap<String,String> colonnes;

    public HashMap<String, String> getColonnes() {
        return colonnes;
    }

    public void setColonnes(HashMap<String, String> colonnes) {
        this.colonnes = colonnes;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

}
