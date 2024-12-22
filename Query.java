package com.ast.mapping;


import java.util.*;

public class Query {

    HashMap<String,Object> clausules;

    public Query() {
        this.clausules = new HashMap<>();
    }

    public void addClausule(String key,Object value) {
        this.clausules.put(key,value);
    }

    public void removeClausule(String key) {
        this.clausules.remove(key);
    }
    
    public HashMap<String,Object> getClausules() {
        return this.clausules;
    }

    public void setClausules(HashMap<String,Object> clausules) {
        this.clausules = clausules;
    }

    public Object get(String key) {
        return this.clausules.get(key);
    }
}