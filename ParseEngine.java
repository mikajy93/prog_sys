package com.ast.parse;

import com.node.*;
import com.ast.node.*;

import java.util.*;

public class ParseEngine {

     public static int getPrecedence(String keyword) {
        Map<String, Integer> precedenceMap = new HashMap<>();
        
        precedenceMap.put("INSERT",1);
        precedenceMap.put("INTO",2);
        precedenceMap.put("VALUES",2);
        precedenceMap.put("SELECT",1);
        precedenceMap.put("DELETE",1);
        precedenceMap.put("DISTINCT",2);
        precedenceMap.put("FROM",1);
        precedenceMap.put("CASE",2);
        precedenceMap.put("WHEN",3);
        precedenceMap.put("THEN",3);
        precedenceMap.put("ELSE",3);
        precedenceMap.put("END",2);
        precedenceMap.put("JOIN",2);
        precedenceMap.put("LEFT JOIN",2);
        precedenceMap.put("RIGHT JOIN",2);
        precedenceMap.put("INNER JOIN",2);
        precedenceMap.put("ON",3);
        precedenceMap.put("GROUP",1);
        precedenceMap.put("GROUP BY",1);
        precedenceMap.put("ORDER",1);
        precedenceMap.put("ORDER BY",1);
        precedenceMap.put("BY",2);
        precedenceMap.put("DESC",2);
        precedenceMap.put("ASC",2);
        precedenceMap.put("WHERE",1);
        precedenceMap.put("BETWEEN",3);
        precedenceMap.put("AND",2);
        precedenceMap.put("OR",2);
        precedenceMap.put("CONDITION",2);
        precedenceMap.put("HAVING",1);
        precedenceMap.put("=",4);
        precedenceMap.put("<=",4);        
        precedenceMap.put(">=",4);
        precedenceMap.put("<>",4);
        precedenceMap.put("!=",4);
        precedenceMap.put("IN",4);

        return precedenceMap.getOrDefault(keyword.toUpperCase(), Integer.MAX_VALUE);
    }

    public boolean isSqlKey(String word) {

        word.trim();

        /*
         * Left join, right join, distinct, like ,union
         * between, case,limit
         * commande {update,drop,alter}
         * subquery {from,where,in,not in}
         */

        String[] sqlKeywords = {
            "SELECT", "FROM", "WHERE", "JOIN", "INNER","INNER JOIN", "LEFT JOIN", "RIGHT JOIN","RIGHT","LEFT", "FULL", "OUTER",
            "GROUP","GROUP BY", "BY", "HAVING", "ORDER","ORDER BY","LIMIT", "OFFSET",
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
            "CREATE", "TABLE", "VIEW", "DROP", "ALTER", "ADD",
            "DISTINCT", "AS", "NOT", "IN", "IS",
            "LIKE", "EXISTS", "UNION", "ALL",
            "AVG","SUM","COUNT","MAX","MIN",
            "AND","OR","BETWEEN","ON",
            "DESC","ASC",
            "=",">","<","<=",">=","<>","!=","IN","NOT","CASE","WHEN","THEN","ELSE"
        };

        for (String keyword : sqlKeywords) {
            if (keyword.equalsIgnoreCase(word)) {
                return true;
            }
        }

        return false;
    }

    public boolean isOperator(String word) {
        word.trim();

        String[] sqlKeywords = {
            "=",">","<","<=",">=","<>","!=","IN","NOT","AND","OR","BETWEEN"
        };

        for (String keyword : sqlKeywords) {
            if (keyword.equalsIgnoreCase(word)) {
                return true;
            }
        }

        return false;
    }

    public boolean isFunction(String word) {
        word.trim();

        String[] sqlKeywords = {
            "AVG","SUM","COUNT","MAX","MIN",
        };

        for (String keyword : sqlKeywords) {
            if (keyword.equalsIgnoreCase(word)) {
                return true;
            }
        }

        return false;
    }

    public static String addSpaceAfterParentheses(String sqlQuery) {
        String modifiedQuery = sqlQuery.replaceAll("\\(", " ( ")
                                       .replaceAll("\\)", " ) ");
        return modifiedQuery.trim().replaceAll("\\s+", " ");
    }

    public ListIterator<String> getTokens(String sql) {
        sql = addSpaceAfterParentheses(sql);
        String input = sql.replaceAll("\\s+"," ")
                                    .replaceAll(", ",",")
                                    .replaceAll(" ,",",")
                                    .trim();
        String delim = " ,";
        StringTokenizer tokenizer = new StringTokenizer(input,delim,true);
        ArrayList<String> tokens = new ArrayList<>();
        while(tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }

        ListIterator<String> list = tokens.listIterator();

        return list;
    }

    public ListIterator<String> getTokens(String sql,String delim,boolean uppercased) {
        if(uppercased) {
            sql.toUpperCase();
        } 
        sql = addSpaceAfterParentheses(sql);
        String input = sql.replaceAll("\\s+"," ")
                                    .replaceAll(", ",",")
                                    .replaceAll(" ,",",")
                                    .trim();
        StringTokenizer tokenizer = new StringTokenizer(input,delim,true);
        ArrayList<String> tokens = new ArrayList<>();
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            tokens.add(token);
        }

        ListIterator<String> list = tokens.listIterator();

        return list;
    }

    public void tokenDebugger(ListIterator<String> tokens) {
        // Mémoriser l'index actuel
        int currentIndex = tokens.nextIndex();

        // Créer un nouvel itérateur en utilisant listIterator() sur la liste d'origine (ici supposée être une liste)
        List<String> tokenList = new ArrayList<>(); // Remplacez cela par la liste réelle contenant les tokens.
        tokens.forEachRemaining(tokenList::add); // Ajouter tous les éléments de tokens à tokenList
        ListIterator<String> tempIterator = tokenList.listIterator(); // Créer un nouvel itérateur à partir de tokenList

        // Affichage des tokens sous forme de cases alignées
        System.out.println("---------- Token Debugger ----------");
        
        while (tempIterator.hasNext()) {
            String token = tempIterator.next();

            // Affichage des cases alignées avec le token
            if (tempIterator.nextIndex() - 1 == currentIndex) {
                // Lorsque nous sommes à la position actuelle, on affiche "CURRENT" avant le token
                System.out.print("[CURRENT] ");
            }
            
            // Affichage de chaque token dans une "case"
            System.out.print("[" + token + "] ");
        }

        System.out.println("\n-------------------------------------");

        // Remettre l'itérateur à sa position d'origine
        while (tokens.previousIndex() != currentIndex) {
            tokens.previous();
        }
        System.out.println("Position du token rétablie.");
    }



    public void buildAst(Node root, Stack<Node> pile, String sql) {
        Node currentNode = root;
        ListIterator<String> tokens = this.getTokens(sql);
        while (tokens.hasNext()) {
            Node node = new Node();
            Node parent = new Node();
            String token = tokens.next();

            if(token.startsWith("'") && !token.endsWith("'")) {
                token += tokens.next() ;
                while(!token.endsWith("'") && tokens.hasNext()) {
                    token += tokens.next();
                }
            }

            // Ignorer les virgules
            if (token.equalsIgnoreCase(",")) {
                continue;
            }

            // Gestion des espaces
            if (token.equalsIgnoreCase(" ")) {
                Node lastNode = pile.peek();
                token = tokens.next();
                if (token.equalsIgnoreCase("AS")) {
                    tokens.next();
                    token = tokens.next();
                    lastNode.setKey(token);
                } else if ((lastNode instanceof Leaf && !this.isSqlKey(token))) {
                    lastNode.setKey(token);
                } else {
                    tokens.previous();
                }
                continue;
            }

            // Gestion des jointures complexes
            if (token.equalsIgnoreCase("RIGHT") || token.equalsIgnoreCase("LEFT") || token.equalsIgnoreCase("INNER")) {
                tokens.next();
                String nextToken = tokens.next();
                if (!nextToken.equalsIgnoreCase("JOIN")) {
                    // throw new Exception("Missing join statement");
                }
                token += " " + nextToken;
            }

            // Gestion des clauses ORDER BY et GROUP BY
            if (token.equalsIgnoreCase("ORDER") || token.equalsIgnoreCase("GROUP")) {
                tokens.next();
                String nextToken = tokens.next();
                if (!nextToken.equalsIgnoreCase("BY")) {
                    // throw new Exception("Missing by statement");
                }
                token += " " + nextToken;
            }

            // Si le token est un mot-clé SQL
            if (this.isSqlKey(token)) {
                node.setKey(token);
                parent = this.determineParent(root, currentNode, node);

                // Gestion des opérateurs
                if (this.isOperator(node.getKey())) {
                    this.handleOperator(node, parent);
                }

                // Gestion des commandes spécifiques
                if (node.getKey().equalsIgnoreCase("CASE")) {
                    node = this.handleCaseNode(node, tokens, pile);
                } else if (node.getKey().equalsIgnoreCase("INSERT")) {
                    node = this.handleInsert(node, tokens, pile);
                } else if (this.isFunction(node.getKey())) {
                    node = this.handleFunctionNode(node, tokens);
                } else if (node.getKey().equalsIgnoreCase("BETWEEN")) {
                    node = this.handleBetweenNode(tokens, pile);
                }

                // Gestion des types et tri
                this.handleSortingNode(node, pile);

                if (node.getType() == null) {
                    if (node.getKey().equalsIgnoreCase("SELECT")) {
                        node.setType("COMMAND");
                    } else {
                        node.setType("CLAUSE");
                    }
                }

                pile.push(node);
                currentNode = node;
            } else if (token.equalsIgnoreCase("(")) {
                Node relay = this.handleSubQueryNode(node, parent, currentNode, tokens, pile);
                if (!relay.getChildren().isEmpty()) {
                    parent = relay.getChildren().get(0);
                    node = relay.getChildren().get(1);
                }
            } else {
                Leaf leaf = new Leaf(token);
                pile.push(leaf);
                parent = currentNode;
                node = leaf;
                node.setType("IDENTIFIER");
            }

            parent.addChild(node);
            node.setParent(parent);
        }
    }

    public String concatToken(ListIterator<String> tokens,String openDelim,String closeDelim,boolean singleDelimiter) {
        // Assumine current token start with openDelim or is equal to openDelim
        String token = "";
        Stack<String> pile = new Stack<>();
        pile.push(openDelim);
        String nextToken = "";
        if(tokens.hasPrevious()) {
            nextToken = tokens.previous();
        } else {
            nextToken = tokens.next();
        }
        while(!pile.isEmpty()) {
            if(tokens.hasNext()) {
                nextToken = tokens.next();
                if(nextToken.equalsIgnoreCase(openDelim)) {
                    nextToken = tokens.next();
                }
            } else {
                break;
            }
            if(singleDelimiter) {
                if(nextToken.equalsIgnoreCase(closeDelim)) {
                    pile.pop();
                } 
                if(nextToken.equalsIgnoreCase(openDelim)) {
                    pile.push(nextToken);
                } 
            } else {
                if(nextToken.endsWith(closeDelim)) {
                    nextToken = nextToken.substring(0,nextToken.lastIndexOf(closeDelim));
                    pile.pop();
                }
                if(nextToken.startsWith(openDelim)) {
                    nextToken = nextToken.substring(nextToken.indexOf(openDelim)+1);
                    if(pile.isEmpty()) {
                        token += " "+nextToken;
                        break;
                    }
                    pile.push(openDelim);
                }
            }
            if(pile.isEmpty()) {
                break;
            }
            token += " "+nextToken;
        }

        if(token.endsWith(closeDelim)) {
            token = token.substring(0,token.indexOf(closeDelim));
        }

        return token.trim().replaceAll("\\s+", " ");
    }

    private Node handleInsert(Node node, ListIterator<String> tokens, Stack<Node> pile) {
        node.setType("COMMAND");

        // Vérification du mot-clé "INTO"
        if (tokens.hasNext()) {
            tokens.next();
            String nextToken = tokens.next().trim();
 
            if (nextToken.equalsIgnoreCase("INTO")) {
                Node intoNode = new Node();
                intoNode.setKey("INTO");
                intoNode.setType("CLAUSE");
                node.addChild(intoNode);
                intoNode.setParent(node);

                // Identifier la table cible
                if (tokens.hasNext()) {
                    tokens.next();
                    String tableName = tokens.next().trim();
                    Leaf tableNode = new Leaf(tableName);
                    tableNode.setType("IDENTIFIER");
                    intoNode.addChild(tableNode);
                    tableNode.setParent(intoNode);
                }

                // Gérer les colonnes (optionnel)
                tokens.next();
                Node columns = new Node();
                columns.setType("EXPRESSION");
                columns.setKey("COLUMNS");
                if (tokens.hasNext() && tokens.next().trim().equalsIgnoreCase("(")) {
                    while (tokens.hasNext()) {
                        tokens.next();
                        String columnToken = tokens.next().trim();
                        if (columnToken.equalsIgnoreCase(")")) {
                            break;
                        }
                        if (!columnToken.equalsIgnoreCase(",")) {
                            Leaf columnNode = new Leaf(columnToken);
                            columnNode.setType("IDENTIFIER");
                            columns.addChild(columnNode);
                            columnNode.setParent(columns);
                        }
                    }
                    node.addChild(columns);
                    columns.setParent(node);
                }
            }
        }

        if (tokens.hasNext()) {
            tokens.next(); 
            String valuesToken = tokens.next().trim(); 
            if (valuesToken.equalsIgnoreCase("VALUES")) {
                Node valuesNode = new Node();
                valuesNode.setKey("VALUES");
                valuesNode.setType("CLAUSE");
                node.addChild(valuesNode);
                valuesNode.setParent(node);
                
                tokens.next();
                if (tokens.hasNext() && tokens.next().trim().equalsIgnoreCase("(")) {
                    while (tokens.hasNext()) {
                        String valueToken = tokens.next().trim();
                        if (valueToken.equalsIgnoreCase(")")) {
                            break;
                        }

                        Node nuplet = new Node();
                        nuplet.setType("EXPRESSION");
                        nuplet.setKey("NUPLET");
                        nuplet.setParent(valuesNode);
                        valuesNode.addChild(nuplet);

                        String value = tokens.next();
                        while(value.equalsIgnoreCase(" ") || value.equalsIgnoreCase("(")) {
                            value = tokens.next();
                        }

                        if(tokens.hasNext()) {
                            String concated = this.concatToken(tokens,"(",")",true);
                            String columns[] = concated.split(",");
                            for(String column:columns) {
                                column = column.trim();
                                if(column.trim().startsWith("'")) {
                                    column = column.substring(column.indexOf("'")+1,column.lastIndexOf("'"));
                                }
                                Leaf leaf = new Leaf(column.trim());
                                leaf.setType("IDENTIFIER");
                                leaf.setParent(nuplet);
                                nuplet.addChild(leaf);
                            }
        
                        } else {
                            // throw new Exception("You have an sql typo near VALUES...");
                        }
                        
                    }
                }
            }
        }

        return node;
    }

    

    public Node handleBetweenNode(ListIterator<String> tokens,Stack<Node> pile) {

        Node betweenNode = new Node();
        betweenNode.setKey("BETWEEN");
        betweenNode.setType("OPERATOR");

        tokens.next();
        String token = tokens.next();
        if(this.isSqlKey(token)) {
            // throw new Exception("You have an sql typo near BETWEEN...");
        }
        Node logicalOperatorNode = new Node();
        logicalOperatorNode.setType("OPERATOR");
        Node args1 = new Node();
        args1.setKey(token);
        args1.setType("IDENTIFIER");

        tokens.next();
        token = tokens.next();
        if(!this.isOperator(token) && !(token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR"))) {
            // throw new Exception("You have an sql typo near BETWEEN...");
        }

        logicalOperatorNode.setKey(token);

        tokens.next();
        token = tokens.next();
        if(this.isSqlKey(token)) {
            // throw new Exception("You have an sql typo near BETWEEN...");
        }
        Node args2 = new Node();
        args2.setKey(token);
        args2.setType("IDENTIFIER");

        logicalOperatorNode.addChild(args1);
        args1.setParent(logicalOperatorNode);
        logicalOperatorNode.addChild(args2);
        args2.setParent(logicalOperatorNode);

        Node lastNode = pile.peek();
        lastNode.getParent().removeChild(lastNode);
        lastNode.setParent(betweenNode);
        betweenNode.addChild(lastNode);

        betweenNode.addChild(logicalOperatorNode);
        logicalOperatorNode.setParent(betweenNode);


        return betweenNode;

    }

    public void handleSortingNode(Node node,Stack<Node> pile) {
        if(node.getKey().equalsIgnoreCase("DESC") || node.getKey().equalsIgnoreCase("ASC")) {
            Node lastNode = pile.peek();
            lastNode.getParent().removeChild(lastNode);
            lastNode.setParent(node);
            node.addChild(lastNode);
        }
    }

    public Node handleSubQueryNode(Node node,Node parent,Node currentNode, ListIterator<String> tokens, Stack<Node> pile) {
        Stack<String> stack = new Stack<>();
        stack.push("(");

        StringBuilder subquery = new StringBuilder();
        String token = tokens.next();
        while (!stack.isEmpty()) {
            subquery.append(" ").append(token);
            token = tokens.next();
            if ("(".equalsIgnoreCase(token)) {
                stack.push(token);
            } else if (")".equalsIgnoreCase(token)) {
                stack.pop();
            }
        }

        String sub = subquery.toString();
        Node subRoot;
        Node relay = new Node();
        if (sub.contains("SELECT")) {
            Node subqueryNode = new Node();
            subqueryNode.setKey("SubQuery");

            try {
                while (token.equalsIgnoreCase(")") || token.equalsIgnoreCase(" ")) {
                    token = tokens.next();
                }

                if ("AS".equalsIgnoreCase(token)) {
                    tokens.next();
                    token = tokens.next();
                    subqueryNode.setKey(token);
                } else if (!this.isSqlKey(token)) {
                    subqueryNode.setKey(token);
                } else {
                    tokens.previous();
                }
            } catch (Exception e) {
                // Gérer les exceptions 
            }
            parent = currentNode;
            node = subqueryNode;
            node.setType("SUBQUERY");
            relay.addChild(parent);
            relay.addChild(node);
            subRoot = node;
        } else {
            parent = currentNode;
            node.setType("EXPRESSION");
            node.setKey("CONDITION");
            relay.addChild(parent);
            relay.addChild(node);
            subRoot = node;        
        }

        this.buildAst(subRoot, pile, sub);

        return relay;

    }

    public Node handleFunctionNode(Node node, ListIterator<String> tokens) {
        Node functionNode = new Node();
        functionNode.setKey("Function");
        functionNode.setType("FUNCTION");
        Node headNode = new Node();
        headNode.setKey(node.getKey());
        headNode.setType("HEAD FUNCTION");
        functionNode.addChild(headNode);
        headNode.setParent(functionNode);

        String token = "";
        while (tokens.hasNext() && !(token = tokens.next()).equalsIgnoreCase("(")) {
        }

        if ("(".equalsIgnoreCase(token)) {
            token = tokens.next();
            while (tokens.hasNext() && token.equalsIgnoreCase(" ")) {
                token = tokens.next();
            }

            Leaf args = new Leaf(token);
            args.setType("IDENTIFIER");
            headNode.addChild(args);
            args.setParent(headNode);

            while (tokens.hasNext() && !(token = tokens.next()).equalsIgnoreCase(")")) {
            }

            if (tokens.hasNext()) {
                tokens.next(); // sauter l'espace 
                token = tokens.next();
                if ("AS".equalsIgnoreCase(token)) {
                    tokens.next();
                    token = tokens.next();
                    if (!this.isSqlKey(token)) {
                        functionNode.setKey(token);
                    }
                } else if (!this.isSqlKey(token) && Character.isLetter(token.charAt(0))) {
                    functionNode.setKey(token);
                } else {
                    tokens.previous();
                }
            }
        } else {
            throw new IllegalArgumentException("Malformed function: missing opening parenthesis");
        }
        
        return functionNode;    
        
    }

    public Node handleCaseNode(Node node, ListIterator<String> tokens, Stack<Node> pile) {
        Node caseNode = new Node();
        caseNode.setKey(node.getKey());
        caseNode.setType("CONDITION");
        Node headNode = new Node();
        headNode.setKey(node.getKey());
        headNode.setType("HEAD CASE");
        caseNode.addChild(headNode);
        headNode.setParent(caseNode);

        StringBuilder conditionToken = new StringBuilder();
        try {
            String token = tokens.next();
            while (!token.equalsIgnoreCase("END")) {
                conditionToken.append(" ").append(token);
                token = tokens.next();
            }
            tokens.next(); 
        } catch (Exception e) {
        }

        Stack<Node> conditionPile = new Stack<>();
        this.buildAst(headNode, conditionPile, conditionToken.toString());

        String token = "";
        while (tokens.hasNext() && (token = tokens.next()).equalsIgnoreCase(" ")) {
        }

        if ("AS".equalsIgnoreCase(token)) {
            tokens.next();
            token = tokens.next();
            if (!this.isSqlKey(token)) {
                caseNode.setKey(token);
            }
        } else if (!this.isSqlKey(token) && Character.isLetter(token.charAt(0))) {
            caseNode.setKey(token);
        } else {
            tokens.previous(); 
        }

        return caseNode;
    }

    public void handleOperator(Node node,Node parent) {
        int lastIndex = parent.getChildren().size()-1;
        Node lastChild = parent.getChildren().get(lastIndex);
        if(lastChild instanceof Leaf) {
            lastChild = (Leaf) lastChild;
        } 
        parent.removeChild(lastChild);
        lastChild.setParent(node);
        node.addChild(lastChild);
        node.setType("OPERATOR");
    }


    public Node determineParent(Node root,Node currentNode,Node node) {
        Node parent = root;

        if(!(currentNode instanceof Leaf)) {

            int currentPriority = getPrecedence(currentNode.getKey());
            int nodePriority = getPrecedence(node.getKey());

            if(currentPriority < nodePriority) { // verifie si la clause precedente est prioritaire , si oui on apparente la clause actuelle
                parent = currentNode;

            } else if(nodePriority == currentPriority) { // apparente les clauses qui ne sont pas prioritaire a la derniere clause enfant du tronc
                parent = currentNode.getParent();
            } 
            else if(nodePriority > 1) {
                if(root.getChildren().isEmpty()) {
                    parent = root;
                } else {
                    int lastIndex = root.getChildren().size()-1;
                    Node lastChild = root.getChildren().get(lastIndex);
                    while(lastChild instanceof Leaf && lastChild != null) {
                        lastChild = lastChild.getParent();
                    } 
                    if(lastChild != null) {
                        if(getPrecedence(lastChild.getKey()) < nodePriority) {
                            parent = lastChild;
                        }
                    }
                }
            }
        } else {
            parent = root;
        }

        return parent;
    }

    public void printTree(Node root, String prefix, boolean isLast) {
        String adjustedPrefix = prefix + (isLast ? "└── " : "├── ");
        String output = "";
        if (root instanceof Leaf) {
            output = adjustedPrefix + ((Leaf) root).getValue();
            if (root.getKey() != null) {
                output += "(" + root.getKey() + ")";
            }
        } else {
            output = adjustedPrefix + root.getKey();
        }

        System.out.println(output+"----"+root.getType());

        String childPrefix = prefix + (isLast ? "    " : "│   ");
        List<Node> children = root.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean childIsLast = (i == children.size() - 1);
            printTree(children.get(i), childPrefix, childIsLast);
        }
    }

    public static void main(String[] args) {
        ParseEngine engine = new ParseEngine();
    String query = 
        "SELECT " +
        "u.user_id, " +
        "u.username, " +
        "(SELECT COUNT(*) " +
        "FROM orders o " +
        "WHERE o.user_id = u.user_id " +
        "AND o.status = 'completed') AS completed_orders, " +
        "(SELECT AVG(p.price) price " +
        "FROM products p " +
        "WHERE p.product_id IN ( " +
        "SELECT oi.product_id " +
        "FROM order_items oi " +
        "WHERE oi.order_id IN ( " +
        "SELECT o.order_id " +
        "FROM orders o " +
        "WHERE o.user_id = u.user_id " +
        "AND o.status = 'completed' " +
        ") " +
        ")) avg_price_of_completed_orders, " +
        "CASE " +
        "WHEN u.is_active = 1 THEN 'Active' " +
        "ELSE 'Inactive' " +
        "END AS user_status " +
        "FROM users u " +
        "LEFT JOIN user_roles ur ON u.user_id = ur.user_id " +
        "LEFT JOIN roles r ON ur.role_id = r.role_id " +
        "WHERE " +
        "u.registration_date >= '2020-01-01' " +
        "AND r.role_name = 'customer' " +
        "AND EXISTS ( " +
        "SELECT 1 " +
        "FROM orders o " +
        "WHERE o.user_id = u.user_id " +
        "AND o.order_date BETWEEN '2022-01-01' AND '2023-01-01' " +
        ") " +
        "ORDER BY " +
        "avg_price_of_completed_orders DESC, " +
        "completed_orders ASC";

    // String query = "create table Produits (id int, nom varchar(30), prix decimal)";


        Stack<Node> pile = new Stack<>();
        Node root = new Node();
        root.setKey("Query");
        root.setType("QUERY");
        engine.buildAst(root,pile,query);      
        engine.printTree(root,"",true);

    }
}

