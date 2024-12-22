package com.node;

import java.util.*;

public class Node {
    String key;
    String type;
    Node parent;
    ArrayList<Node> children;

    public Node() {
        this.children = new ArrayList<Node>();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Node getParent() {
        return this.parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() {
        return this.children;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public void removeChild(Node child) {
        this.children.remove(child);
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }
}