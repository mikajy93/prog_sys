package com.ast.mapping;


public class Delete  {
    Where where;
    String table;

public Delete() {
}

public String getTable() {
    return table;
}

public void setTable(String table) {
    this.table = table;
}

 public Where getWhere() {
        return where;
    }

    public void setWhere(Where where) {
        this.where = where;
    }

}
