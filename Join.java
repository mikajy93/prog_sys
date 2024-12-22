package com.ast.mapping;


public class Join {
    Object source;
    String colA;
    String operator;
    String colB;

    public Join() {
    }

    public Join(Object source,String colA,String operator,String colB) {
        this.source = source;
        this.colA = colA;
        this.colB = colB;
        this.operator = operator;
    }

    public String getColB() {
        return colB;
    }

    public void setColB(String colB) {
        this.colB = colB;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getColA() {
        return colA;
    }

    public void setColA(String colA) {
        this.colA = colA;
    }

    public Object getSource() {
        return this.source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

}