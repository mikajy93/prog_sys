package com.ast.node;

import com.node.*;

public class Leaf extends Node {
    String value;

    public Leaf() {
        super();
    }

    public Leaf(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}