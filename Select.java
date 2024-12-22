package com.ast.mapping;


import java.util.ArrayList;

import com.node.Node;

public class Select  {
    private ArrayList<Node> columns;

    public Select() {
        this.columns = new ArrayList<Node>();
    }

    public Select(ArrayList<Node> columns) {
        this.columns = columns;
    }

    public ArrayList<Node> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<Node> columns) {
        this.columns = columns;
    }

    public void addColumn(Node column) {
        this.columns.add(column);
    }
}
