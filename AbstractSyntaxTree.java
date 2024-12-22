package com.ast.tree;

import com.node.*;
import com.ast.node.*;
import com.ast.mapping.*;
import com.ast.parse.*;
import com.sgbd.*;

import java.util.*;

public class AbstractSyntaxTree {
    Node root;
    Stack<Node> pile;
    ParseEngine parseEngine;

    public AbstractSyntaxTree() {
        this.root = new Node();
        this.root.setKey("Query");
        this.root.setType("QUERY");
        this.pile = new Stack<>();
        this.parseEngine = new ParseEngine();
    }

    public AbstractSyntaxTree(String query) {
        this.root = new Node();
        this.root.setKey("Query");
        this.root.setType("QUERY");
        this.pile = new Stack<>();
        this.parseEngine = new ParseEngine();
        parseEngine.buildAst(this.root,this.pile,query);
    }


    public Node getRoot() {
        return this.root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Stack<Node> getPile() {
        return this.pile;
    }

    public void setPile(Stack<Node> pile) {
        this.pile = pile;
    }

    public ParseEngine getParseEngine() {
        return this.parseEngine;
    }

    public void setParseEngine(ParseEngine parseEngine) {
        this.parseEngine = parseEngine;
    }

    public void print() {
        this.parseEngine.printTree(root,"",true);
    }

    public Node getBranch(Node root,String keyword) {
        ArrayList<Node> children = root.getChildren();
        Node branch = null;
        for(Node child:children) {
            if(child.getKey().equalsIgnoreCase(keyword)) {
                branch = child;
                break;
            }
        }
        return branch;
    }

    public Insert getInsert() throws Exception {
        
        Insert insert = new Insert();
        Node insertNode = this.getBranch(this.root,"INSERT");

        if(insertNode != null) {

            Node intoNode = insertNode.getChildren().get(0);
            String table = ((Leaf) intoNode.getChildren().get(0)).getValue().toUpperCase();
            insert.setTable(table);

            Node columnsNode  = insertNode.getChildren().get(1);
            ArrayList<String> columns = new ArrayList<>();
            for(Node child:columnsNode.getChildren()) {
                columns.add(((Leaf) child).getValue().toUpperCase());
            }

            insert.setColumns(columns);

            Node values  = insertNode.getChildren().get(2);
            ArrayList<Nuplet> nuplets = new ArrayList<>();
            for(Node nupletNode:values.getChildren()) {
                Nuplet nuplet = new Nuplet();
                ArrayList<Node> valuesNode = nupletNode.getChildren();
                for(int i = 0; i < valuesNode.size();i++) {
                    nuplet.add(columns.get(i),((Leaf) valuesNode.get(i)).getValue());
                }
                nuplets.add(nuplet);
            }

            insert.setNuplets(nuplets);

        } else {
            throw new Exception("Error while looking for insert statement");
        }

        return insert;
    }

    public Select getSelect() throws Exception {
        Select select = new Select();
        Node selectNode = this.getBranch(this.root,"SELECT");
        if(selectNode != null) {
            select.setColumns(selectNode.getChildren());
        } else {
            throw new Exception("Error while looking for select statement");
        }
        return select;
    }

    public From getFrom() throws Exception {
        From from = new From();
        Node fromNode = this.getBranch(this.root,"FROM");
        if(fromNode != null) {
            Node source = fromNode.getChildren().get(0);
            if(source instanceof Leaf) {
                from.setSource((Leaf) source);
            } else {
                from.setSource(source);
            }
        } else {
            throw new Exception("Error while looking for from statement");
        }
        return from;
    }

    public Join[] getJoins() {
        ArrayList<Node> joinNodes = new ArrayList<>();
        Node fromRoot = this.getBranch(this.root, "FROM");
        for(Node child:fromRoot.getChildren()) {
            if(child.getKey() != null && child.getKey().equalsIgnoreCase("JOIN")) {
                joinNodes.add(child);
            }
        }

        Join joins[] = new Join[joinNodes.size()];
        int count  = 0;
        for(Node joinNode:joinNodes) {
            Object source = (Object) joinNode.getChildren().get(0);
            Node onNode = joinNode.getChildren().get(1);
            Node operator = onNode.getChildren().get(0);
            String left = ((Leaf) operator.getChildren().get(0)).getValue();
            String right = ((Leaf) operator.getChildren().get(1)).getValue();
            joins[count] = new Join(source,left,operator.getKey(),right);
        }

        return joins;
    }

    public Where getWhere() {
        Where where = new Where();
        Node whereNode = this.getBranch(this.root,"WHERE");
        if(whereNode != null) {
            where.setRoot(whereNode);
        } else {
            return null;
        }
        return where;
    }

    public OrderBy getOrderBy() {
        OrderBy orderBy = new OrderBy();
        Node orderByNode = this.getBranch(root,"ORDER BY");
        if(orderByNode != null) {
            for(Node orderChild:orderByNode.getChildren()) {
                String order = orderChild.getKey();
                String column = ((Leaf) orderChild.getChildren().get(0)).getValue();
                orderBy.addFiltre(column, order);
            }
        } else {
            return null;
        }
        return orderBy;   
    }

    public GroupBy getGroupBy() {
        GroupBy groupBy = new GroupBy();
        Node groupByNode = this.getBranch(root, "GROUP BY");
        if(groupByNode != null) {
            for(Node child:groupByNode.getChildren()) {
                groupBy.addColumn(((Leaf)  child).getValue());
            }
        } else {
            return null;
        }
        return groupBy;
    }

    public Delete getDelete() throws Exception {
        Delete delete = new Delete();
        From from = this.getFrom();
        Where where = this.getWhere();
        delete.setTable(( (Leaf) from.getSource()).getValue().toString());
        delete.setWhere(where);
        return delete;
    }

    public Query getSelectQuery() throws Exception {
        Query query = new Query();
        Select select = this.getSelect();
        From from = this.getFrom();
        Join[] joins = this.getJoins();
        Where where = this.getWhere();
        GroupBy groupBy = this.getGroupBy();
        OrderBy orderBy = this.getOrderBy();

        query.addClausule("SELECT",select);
        query.addClausule("FROM",from);
        if(joins.length > 0) {
            query.addClausule("JOIN",joins);
        }
        if(where != null) {
            query.addClausule("WHERE",where);
        }
        if(groupBy != null) {
            query.addClausule("GROUP BY", groupBy);
        }
        if(orderBy != null) {
            query.addClausule("ORDER BY", orderBy);
        }

        return query;
    }

    public Query getInsertQuery() throws Exception {
        Query query = new Query();
        Insert insert = this.getInsert();
        query.addClausule("INSERT",insert);
        return query;
    }

    public Query getDeleteQuery() throws Exception {
        Query query = new Query();
        Delete delete = this.getDelete();
        query.addClausule("DELETE",delete);
        return query;
    }

}