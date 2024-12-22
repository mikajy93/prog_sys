package com.ast.mapping;

import java.util.ArrayList;

public class GroupBy {
    ArrayList<String> columns;

    public GroupBy() {
        this.columns = new ArrayList<>();
    }

    public GroupBy(ArrayList<String> columns) {
        this.columns = columns;
    }

    public ArrayList<String> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<String> columns) {
        this.columns = columns;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }
}
