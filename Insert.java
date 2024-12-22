package com.ast.mapping;

import com.sgbd.*;

import java.util.ArrayList;

public class Insert  {
    String table;
    ArrayList<String> columns;
    ArrayList<Nuplet> nuplets;

    public Insert() {
        this.columns = new ArrayList<>();
        this.nuplets = new ArrayList<>();
    }

    public Insert(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public ArrayList<String> getColumns() {
        return this.columns;
    }

    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
    }

    public ArrayList<Nuplet> getNuplets() {
        return this.nuplets;
    }

    public void setNuplets(ArrayList<Nuplet> nuplets) {
        this.nuplets = nuplets;
    }
}
