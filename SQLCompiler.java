package com.compiler;


import com.ast.tree.*;
import com.ast.node.*;
import com.node.*;
import com.sgbd.*;
import com.socket.ConfigLoader;
import com.utils.Utils;
import com.dbmanager.*;
import com.ast.mapping.*;

import java.lang.reflect.Method;
import java.util.*;


/*
 * Not supported sql key word : NOT,IN,CASE,AGREGATION FUNCTION AND GROUP BY, ORDER BY
 */


public class SQLCompiler {

    String dbName;
    JSONDatabaseManager manager;

    public SQLCompiler(String dbName) {
        this.dbName = dbName.toUpperCase();
        this.manager = new JSONDatabaseManager();
    }

    public SQLCompiler(String dbName,String dbPath) {
        this.dbName = dbName.toUpperCase();
        this.manager = new JSONDatabaseManager();
        this.manager.setDbPath(dbPath);
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbPath() {
        return this.manager.getDbPath();
    }

    public void setDbPath(String dbPath) {
        this.manager.setDbPath(dbPath);
    }
    
    public CreateTable parseCreateTable(String sqlQuery) throws IllegalArgumentException {
        CreateTable createTable = new CreateTable();
    
        // Diviser la requête pour extraire le nom de la table et les colonnes
        String parts[] = sqlQuery.split("\\(", 2); // Diviser uniquement en deux parties
        if (parts.length < 2) {
            throw new IllegalArgumentException("Requête SQL invalide : impossible de trouver les colonnes.");
        }
    
        // Extraire le nom de la table
        String tableName = parts[0].toUpperCase().split("TABLE")[1].trim();
        createTable.setTable(tableName);
    
        // Extraire les colonnes (supprimer la parenthèse finale)
        String columnsDefinition = parts[1].replace(")", "").trim();
    
        // Séparer les colonnes par des virgules
        String[] columnsArray = columnsDefinition.split(",");
    
        // Stocker les colonnes dans un LinkedHashMap
        LinkedHashMap<String, String> columns = new LinkedHashMap<>();
        for (String column : columnsArray) {
            // Nettoyer chaque colonne
            String[] columnParts = column.trim().split("\\s+"); // Diviser par un ou plusieurs espaces
            if (columnParts.length != 2) {
                throw new IllegalArgumentException("Format de colonne invalide : " + column);
            }
            String columnName = columnParts[0].trim();
            String columnType = columnParts[1].trim();
            columns.put(columnName, columnType);
        }
    
        createTable.setColonnes(columns);
        return createTable;
    }
    
    public String compileCreate(String sql) throws Exception {
        String command = sql.trim().toUpperCase();
        String created = "";
        if (command.contains("TABLE")) {
            if(this.dbName.equalsIgnoreCase("rootdb")) {
                return "No database selected";
            }
            command = "TABLE";
            CreateTable createTable = parseCreateTable(sql);
            created = createTable.getTable();
            Domaine domaines[] = new Domaine[createTable.getColonnes().size()];
            int count = 0;
            
            // Utilisation de l'ordre garanti par LinkedHashMap
            for (Map.Entry<String, String> entry : createTable.getColonnes().entrySet()) {
                String columnName = entry.getKey();
                String columnType = entry.getValue();
                domaines[count] = new Domaine(columnName, columnType);
                count++;
            }
            
            this.manager.createTable(this.dbName, createTable.getTable(), domaines);
        } else if (command.contains("DATABASE")) {
            command = "DATABASE";
            String dbName = sql.split(" ")[2];
            created = dbName;
            this.manager.createDatabase(dbName);
        }

        return command+" "+created+" created successfully";
    }
    

    public Relation processOperator(String tableName,Relation table,Node operatorNode) throws Exception {
         // obtenir les domaines de la table ainsi que leur type
         HashMap<String,String> mappedDomaines = this.manager.getDomaine(this.dbName,tableName);
         // declaration de la relation filtrée
         Relation filteredRelation = new Relation();
         
         Node leftNode = operatorNode.getChildren().get(0);
         Node rightNode = operatorNode.getChildren().get(1);
         
         // instancier un nouveau string pour l'operateur
        String operator = operatorNode.getKey();

        // le nom du domaine ou de la colonne concernée
        String targetedColumn = ((Leaf) leftNode).getValue();

        // l'objet à comparer parsé en fonction du type de targetedColumn avec mappedDomaines
        Object value = JSONStringParser.parseValue(((Leaf) rightNode).getValue(),mappedDomaines.get(targetedColumn));
        
        //effectuer l'opération de selection (filtrage avec where)
        filteredRelation = table.selection(targetedColumn,operator,value);

        return filteredRelation;
    }

    public Relation processWhere(String tableName,Relation table,Node whereRoot) throws Exception {

        Relation filteredRelation = new Relation();
        filteredRelation.setDomaines(table.getDomaines());

        String key = whereRoot.getKey();

        if(key.equalsIgnoreCase("AND") || key.equalsIgnoreCase("OR")) {
            Node leftNode = whereRoot.getChildren().get(0);
            Node rightNode = whereRoot.getChildren().get(1);
            Relation r1 = processWhere(tableName,table,leftNode);
            Relation r2 = processWhere(tableName,table,rightNode);
            Relation r3 = new Relation();
            if(key.equalsIgnoreCase("AND")) {
                r3 = r1.intersection(r2);
            } else if(key.equalsIgnoreCase("OR")) {
                r3 = r1.union(r2);
            }
            filteredRelation.setNuplets(r3.getNuplets());
        } else if(key.equalsIgnoreCase("WHERE") || key.equalsIgnoreCase("CONDITION")) {
            Node whereChild = whereRoot.getChildren().get(0);
            Relation r1 = processWhere(tableName,table,whereChild);
            filteredRelation.setNuplets(r1.getNuplets());
        } else {
            Relation r = processOperator(tableName,table,whereRoot);
            filteredRelation.setNuplets(r.getNuplets());
        }

        return filteredRelation;
    }

    public List<Object> getValeursPossibles(Relation table,String columnName) throws Exception {
        List<Object> valeursPossibles = new ArrayList<>();

        for(Nuplet nuplet:table.getNuplets()) {
            Object valeur = nuplet.get(columnName);
            if(!valeursPossibles.contains(valeur)) {
                valeursPossibles.add(valeur);
            }
        }

        return valeursPossibles;
    }

    public HashMap<List<Object>,ArrayList<Nuplet>> hashGrouping(Relation sourceTable,GroupBy groupBy) throws Exception {
        
        HashMap<List<Object>,ArrayList<Nuplet>> hash = new HashMap<>();
        List<List<Object>> valeursPossibles = new ArrayList<>();
        ArrayList<String> columns = groupBy.getColumns();
        for(String column:columns) {
            List<Object> valeurs = getValeursPossibles(sourceTable, column);
            valeursPossibles.add(valeurs);
        }

        List<List<Object>> combinaisons = Utils.combinaison(valeursPossibles);

        for(List<Object> combinaison:combinaisons) {
            ArrayList<Nuplet> nuplets = new ArrayList<>();
            for(Nuplet nuplet:sourceTable.getNuplets()) {
                boolean mapped = true;
                for(int i= 0; i < combinaison.size();i++) {
                    if(!nuplet.get(columns.get(i)).equals(combinaison.get(i))) {
                        mapped = false;
                        break;
                    }   
                }
                if(mapped) {
                    nuplets.add(nuplet);
                }
            }
            if (!nuplets.isEmpty()) { 
                hash.put(Collections.unmodifiableList(combinaison), nuplets);
            }        
    
        }
        return hash;
    }

    public ArrayList<Nuplet> processGroupBy(Relation sourceTable, Select select, GroupBy groupBy) throws Exception {

        ArrayList<String> groupByColumns = groupBy.getColumns();
        HashMap<List<Object>, ArrayList<Nuplet>> groupedData = hashGrouping(sourceTable, groupBy);
        ArrayList<Nuplet> groupedResults = new ArrayList<>();
        
        // Iterating over each group
        for (List<Object> groupKey : groupedData.keySet()) {
            Nuplet groupResult = new Nuplet();
            ArrayList<Nuplet> nuplets = groupedData.get(groupKey);
            ArrayList<Node> selectColumns = select.getColumns();
            
            // Iterating over columns to apply aggregation or fetch values
            for (Node columnNode : selectColumns) {
                if (columnNode instanceof Leaf) {
                    String columnName = ((Leaf) columnNode).getValue();
                    if (!groupByColumns.contains(columnName)) {
                        throw new Exception("Columns should be in 'group by' clause or aggregation function");
                    }
                    groupResult.add(columnName, nuplets.get(0).get(columnName));  // Adding the first value of the column
                } else {
                    Node aggregationFunctionNode = columnNode.getChildren().get(0);
                    String columnName = ((Leaf) aggregationFunctionNode.getChildren().get(0)).getValue();
                    List<Object> columnValues = new ArrayList<>();
                    
                    // Collecting values of the column to apply aggregation
                    for (Nuplet nuplet : nuplets) {
                        columnValues.add(nuplet.get(columnName));
                    }
    
                    // Applying the aggregation function
                    Utils utils = new Utils();
                    Method aggregationMethod = utils.getClass().getMethod(aggregationFunctionNode.getKey().toLowerCase(), columnValues.getClass());
                    double aggregatedValue = Double.parseDouble(aggregationMethod.invoke(utils, columnValues).toString());
    
                    String finalColumnName = columnNode.getKey().equalsIgnoreCase("Function") ? aggregationFunctionNode.getKey() + "(" + columnName + ")" : columnNode.getKey();
                    groupResult.add(finalColumnName, aggregatedValue);
                }
            }
            
            groupedResults.add(groupResult);
        }
    
        return groupedResults;
    }

    public void processOrderBy(Relation table, OrderBy orderBy) throws Exception {

        ArrayList<Nuplet> nuplets = table.getNuplets();
        
        for (String column : orderBy.getFiltre().keySet()) {
            String order = orderBy.getFiltre().get(column);
            boolean ascending = order.equalsIgnoreCase("ASC");
    
            for (int pass = 0; pass < nuplets.size() - 1; pass++) {
                for (int i = 0; i < nuplets.size() - pass - 1; i++) {
                    double currentVal = 0;
                    double nextVal = 0;
    
                    try {
                        currentVal = Double.parseDouble(nuplets.get(i).get(column).toString());
                        nextVal = Double.parseDouble(nuplets.get(i + 1).get(column).toString());
                    } catch (NumberFormatException e) {
                        throw new Exception("La valeur de la colonne " + column + " n'est pas un nombre valide");
                    }
    
                    boolean swapCondition = ascending ? currentVal > nextVal : currentVal < nextVal;
    
                    if (swapCondition) {
                        Nuplet temp = nuplets.get(i);
                        nuplets.set(i, nuplets.get(i + 1));
                        nuplets.set(i + 1, temp);
                    }
                }
            }
        }

        table.setNuplets(nuplets);
    }    

    public Relation compileSelect(AbstractSyntaxTree tree) throws Exception {
        Query query = tree.getSelectQuery();
        From from = (From) query.get("FROM");
        Relation sourceTable = new Relation();
        String tableName = "";
        Node sourceNode = ((Node) from.getSource());
        if(sourceNode.getType().equals("SUBQUERY")) {
            AbstractSyntaxTree subQueryTree = new AbstractSyntaxTree();
            subQueryTree.setRoot((Node) from.getSource());
            sourceTable = compileSelect(subQueryTree);
            if(sourceNode.getType().equalsIgnoreCase("SUBQUERY")) {
                if(!sourceNode.getKey().equalsIgnoreCase("SUBQUERY")) {
                    tableName = sourceNode.getKey();
                }
                if(tableName == null || tableName.equals("")) {
                    throw new Exception("Missing alias, call to subquery is ambiguous");
                }
            } 
        } else {
            tableName = ((Leaf) from.getSource()).getValue();
            sourceTable = manager.getRelationFromJSON(this.dbName,tableName);
        }
        if(sourceNode instanceof Leaf && ((Leaf) sourceNode).getKey() != null) {
            sourceTable.setNom(((Leaf) sourceNode).getKey());
        } else {
            sourceTable.setNom(tableName);
        }

        if(query.get("JOIN") != null) {
            Join[] joins = (Join[]) query.get("JOIN");
            for(Join join:joins) {
                Object source = join.getSource();
                Leaf sourceLeaf = (Leaf) source;
                String sourceName = sourceLeaf.getValue();
                Relation sourceJoin = manager.getRelationFromJSON(this.dbName,sourceName);
                if(sourceLeaf.getKey() != null) {
                    sourceName = sourceLeaf.getKey();
                }
                sourceJoin.setNom(sourceName);
                sourceTable = sourceTable.join(sourceJoin,join.getColA(),join.getOperator(),join.getColB());
            }
        }
        if(query.get("WHERE") != null) {
            sourceTable = processWhere(tableName, sourceTable,((Where) query.get("WHERE")).getRoot());
        }

        Select select = ((Select) query.get("SELECT"));

        if(query.get("GROUP BY") != null) {
            GroupBy groupBy = (GroupBy) query.get("GROUP BY");
            ArrayList<Nuplet> nuplets = processGroupBy(sourceTable, select, groupBy);
            Relation groupedRelation = new Relation();
            ArrayList<Domaine> domaines = new ArrayList<>();
            for(Domaine domaine:sourceTable.getDomaines()) {
                for(String key:nuplets.get(0).getElements().keySet()) {
                    if(key.equals(domaine.getNom())) {
                        domaines.add(domaine);
                    }
                }
            }

            for(String key:nuplets.get(0).getElements().keySet()) {
                if(!sourceTable.containsDomaine(key)) {
                    Domaine aggregationDomaine = new Domaine();
                    aggregationDomaine.setNom(key);
                    aggregationDomaine.setType("DOUBLE");
                    domaines.add(aggregationDomaine);
                    continue;
                }
            }


            groupedRelation.setDomaines(domaines);
            groupedRelation.setNuplets(nuplets);

            if(query.get("ORDER BY") != null) {
                OrderBy orderBy = (OrderBy) query.get("ORDER BY");
                processOrderBy(groupedRelation, orderBy);
            }

            return groupedRelation;
        }

        if(query.get("ORDER BY") != null) {
            OrderBy orderBy = (OrderBy) query.get("ORDER BY");
            processOrderBy(sourceTable, orderBy);
        }

        ArrayList<Node> columnsNode = select.getColumns();
        ArrayList<String> columns = new ArrayList<>();
        if(columnsNode.size() == 1 && ((Leaf) columnsNode.get(0)).getValue().equals("*")) {
            ArrayList<Domaine> domaines = sourceTable.getDomaines();
            for(Domaine domaine:domaines) {
                columns.add(domaine.getNom());
            }
        } else {
            for(Node columnNode:columnsNode) {
                columns.add(((Leaf) columnNode).getValue());
            }
        }
        return sourceTable.projection(columns);
    }

    public String compileInsert(String sql) throws Exception {

        AbstractSyntaxTree tree = new AbstractSyntaxTree(sql);

        Query query = tree.getInsertQuery();

        Insert insert = (Insert) query.get("INSERT");

        String table = insert.getTable();

        String columns[] = new String[insert.getColumns().size()];

        ArrayList<String> columnsList = insert.getColumns();

        for(int i= 0;i< columnsList.size();i++) { 
            columns[i] = columnsList.get(i);
        }

        ArrayList<Nuplet> nuplets = insert.getNuplets();
        String values[][] = new String[nuplets.size()][columns.length];

        for(int i = 0; i < nuplets.size(); i++) {
            for(int j = 0 ; j < columns.length; j++) {
                values[i][j] = nuplets.get(i).get(columns[j]).toString();
            }
        }

        for(int i = 0; i < nuplets.size(); i++) {
            this.manager.insertData(dbName,table,columns,values[i]);
        }

        return nuplets.size()+" rows affected";

    }

    public String compileDelete(AbstractSyntaxTree tree) throws Exception {
        Query query = tree.getDeleteQuery();
        Delete delete = (Delete) query.get("DELETE");
        String tableName = delete.getTable().toUpperCase().trim();
        Relation sourceTable = this.manager.getRelationFromJSON(this.dbName, tableName);
        Relation filteredRelation = processWhere(tableName, sourceTable, delete.getWhere().getRoot());
        try {
            this.manager.deleteData(this.dbName,tableName,filteredRelation);
        }catch(Exception e) {
            e.printStackTrace();
        }
        return "Delete successful";
    }

    public String compileDropTable(String sql) throws Exception {
        sql = sql.replaceAll("\\s+", " ").trim();
        String parts[] = sql.split(" ");
        if(!parts[0].equalsIgnoreCase("DROP")  || !parts[1].equalsIgnoreCase("TABLE") || parts.length != 3) {
            throw new Exception("Your query has a typo");
        }

        this.manager.dropTable(this.dbName,parts[2]);

        return "Dropped table "+parts[2];
    }

    public String compileDropDatabase(String sql) throws Exception {
        sql = sql.replaceAll("\\s+", " ").trim();
        String parts[] = sql.split(" ");
        if(!parts[0].equalsIgnoreCase("DROP")  || !parts[1].equalsIgnoreCase("DATABASE") || parts.length != 3) {
            throw new Exception("Your query has a typo");
        }

        this.manager.dropDatabase(parts[2]);

        return "Dropped database "+parts[2];
    }

    public String compileUseDatabase(String sql) throws Exception {
        sql = sql.replaceAll("\\s+", " ").trim().toUpperCase();
        String parts[] = sql.split(" ");
        if(!parts[0].equals("USE") || parts.length != 2) {
            throw new Exception("You have an sql typo in your query");
        }
        
        boolean dbExists = this.manager.checkIfDatabaseExists(parts[1]);

        if(dbExists) {
            this.setDbName(parts[1]);
        } else {
            throw new Exception("Database doesn't exist");
        }

        return "Database selected "+parts[1];
    }

    public String compileShow(String sql) throws Exception {
        sql = sql.replaceAll("\\s+", " ").trim().toUpperCase();
        String parts[] = sql.split(" ");
        String display = "";
        if(parts[0].equals("SHOW")) {
            if(parts[1].equals("TABLES")) {
                display = this.manager.displayAllTables(this.dbName);
            } else if(parts[1].equals("DATABASES")) {
                display = this.manager.displayAllDatabases();
            } else {
                throw new Exception("You have an sql typo in your query");
            }
        } else {
            throw new Exception("You have an sql typo starting at line 1");
        }

        return display;
    }

    public Object compile(String sql) throws Exception {
        sql = sql.toUpperCase();
        AbstractSyntaxTree tree = null;
        try {
            tree = new AbstractSyntaxTree(sql);
        }catch(Exception e) {}
        if(sql.toUpperCase().contains("INSERT")) {
            return this.compileInsert(sql);
        }
        if(sql.toUpperCase().contains("SELECT")) {
            return this.compileSelect(tree);
        }
        if(sql.toUpperCase().contains("DELETE")) {
            return this.compileDelete(tree);
        }if(sql.toUpperCase().contains("CREATE")) {
            return this.compileCreate(sql.toUpperCase());
        } if(sql.toUpperCase().contains("DROP TABLE")) {
            return this.compileDropTable(sql);
        } if(sql.toUpperCase().contains("DROP DATABASE")) {
            return this.compileDropDatabase(sql);
        } if(sql.toUpperCase().contains("USE")) {
            return this.compileUseDatabase(sql);
        } if(sql.toUpperCase().contains("SHOW")) {
            return this.compileShow(sql);
        }

        return "Error while excecuting query...";
    }

    public static void main(String[] args) {
        try {
            String dbName = "COOPERATIVE";
            ConfigLoader loader = new ConfigLoader();
            String dbPath = loader.getDbPath(3000);
            SQLCompiler compiler = new SQLCompiler(dbName,dbPath);
            // Domaine[] domaine = new Domaine[4];
            // domaine[0] = new Domaine("titre","varchar");
            // domaine[1] = new Domaine("auteur","varchar");
            // domaine[2] = new Domaine("date","int");
            // domaine[3] = new Domaine("prix","int");
            // JSONDatabaseManager manager = new JSONDatabaseManager();
            // manager.createDatabase(compiler.getDbName());
            // manager.createTable(compiler.getDbName(),"Livres",domaine);
            // String resultat = (String) compiler.compile("INSERT INTO LIVRES (TITRE,AUTEUR,DATE,PRIX) "+
            // "VALUES "+
            // "('Mon mari me trompe avec mon psychologue','Bernadette Schtrompnf',2000,9), "+
            // "('Harry Potter','JK.Rowling',1999,20), "+
            // "('How do you live','some japanese man',1989,15)");
            // String result = compiler.compile("DELETE FROM livres where date = 2001").toString();
            // String insertQuery = "INSERT INTO LIVRES (TITRE, AUTEUR, DATE, PRIX) VALUES " +
            // "('Le Petit Prince', 'Antoine de Saint-Exupéry', 1943, 20), " +
            // "('Courrier Sud', 'Antoine de Saint-Exupéry', 1929, 18), " +
            // "('Vol de Nuit', 'Antoine de Saint-Exupéry', 1931, 22), " +
            // "('1984', 'George Orwell', 1949, 15), " +
            // "('Animal Farm', 'George Orwell', 1945, 12), " +
            // "('Homage to Catalonia', 'George Orwell', 1938, 17), " +
            // "('To Kill a Mockingbird', 'Harper Lee', 1960, 18), " +
            // "('Pride and Prejudice', 'Jane Austen', 1813, 25), " +
            // "('Emma', 'Jane Austen', 1815, 22), " +
            // "('Sense and Sensibility', 'Jane Austen', 1811, 20), " +
            // "('The Great Gatsby', 'F. Scott Fitzgerald', 1925, 22), " +
            // "('Tender is the Night', 'F. Scott Fitzgerald', 1934, 21)";


            // String insertion = (String) compiler.compile(insertQuery);

            
            // JSONDatabaseManager manager = new JSONDatabaseManager();
            // manager.createDatabase("MARKET");
            // Domaine id = new Domaine("ID","INT");
            // Domaine nom = new Domaine("NOM","VARCHAR");
            // Domaine categorie = new Domaine("CATEGORIE","VARCHAR");
            // Domaine prix = new Domaine("PRIX","DOUBLE");
            // Domaine stock = new Domaine("STOCK","INT");
            // Domaine disponible = new Domaine("DISPONIBLE","BOOLEAN");

            // Domaine domaines[] = {id,nom,categorie,prix,stock,disponible};

            // manager.createTable("MARKET","PRODUITS", domaines);


// Requêtes d'insertion des données
String insertQuery1 = "INSERT INTO PRODUITS (ID, NOM, CATEGORIE, PRIX, STOCK, DISPONIBLE) VALUES " +
                      "(1, 'Ordinateur Portable', 'Électronique', 800.00, 50, TRUE), " +
                      "(2, 'Smartphone', 'Électronique', 600.00, 0, FALSE), " +
                      "(3, 'Casque Audio', 'Électronique', 100.00, 200, TRUE), " +
                      "(4, 'T-Shirt', 'Vêtements', 20.00, 150, TRUE), " +
                      "(5, 'Chaussures de Sport', 'Vêtements', 50.00, 30, TRUE), " +
                      "(6, 'Télévision', 'Électronique', 1200.00, 10, TRUE), " +
                      "(7, 'Canapé', 'Meubles', 500.00, 5, TRUE), " +
                      "(8, 'Table', 'Meubles', 150.00, 0, FALSE), " +
                      "(9, 'Livre', 'Divers', 15.00, 300, TRUE), " +
                      "(10, 'Vélo', 'Sport', 250.00, 20, TRUE)";

                    //   INSERT INTO PRODUITS (ID, NOM, CATEGORIE, PRIX, STOCK, DISPONIBLE) VALUES (1, 'Ordinateur Portable', 'Électronique', 800.00, 50, TRUE),(2, 'Smartphone', 'Électronique', 600.00, 0, FALSE), (3, 'Casque Audio', 'Électronique', 100.00, 200, TRUE),(4, 'T-Shirt', 'Vêtements', 20.00, 150, TRUE), (5, 'Chaussures de Sport', 'Vêtements', 50.00, 30, TRUE),  (6, 'Télévision', 'Électronique', 1200.00, 10, TRUE),  (7, 'Canapé', 'Meubles', 500.00, 5, TRUE),  (8, 'Table', 'Meubles', 150.00, 0, FALSE),  (9, 'Livre', 'Divers', 15.00, 300, TRUE), (10, 'Vélo', 'Sport', 250.00, 20, TRUE)

// Requête a : Requête avec AND, OR et parenthèses simples
String query1 = "SELECT * FROM PRODUITS " +
                "WHERE (CATEGORIE = 'Électronique' AND PRIX > 500) " +
                "OR (STOCK > 100 AND DISPONIBLE = TRUE)";

// ├── Query----null
// │   ├── SELECT----COMMAND
// │   │   └── *----IDENTIFIER
// │   ├── FROM----CLAUSE
// │   │   └── PRODUITS----IDENTIFIER
// │   └── WHERE----CLAUSE
// │       └── CONDITION----EXPRESSION
// │           └── AND----OPERATOR
// │               ├── =----OPERATOR
// │               │   ├── CATEGORIE----IDENTIFIER     
// │               │   └── 'Électronique'----IDENTIFIER
// │               └── >----OPERATOR
// │                   ├── PRIX----IDENTIFIER
// │                   └── 500----IDENTIFIER
// WHERE
// OR
// ├── CONDITION----EXPRESSION
// │   └── STOCK----IDENTIFIER

// Requête b : Requête avec des parenthèses imbriquées
String query2 = "SELECT * FROM PRODUITS " +
                "WHERE ((CATEGORIE = 'Électronique' AND PRIX > 500) " +
                "OR (CATEGORIE = 'Vêtements' AND STOCK > 50)) " +
                "AND DISPONIBLE = TRUE";

// Requête c : Requête testant plusieurs conditions avec NOT
String query3 = "SELECT * FROM PRODUITS " +
                "WHERE NOT (CATEGORIE = 'Divers' OR DISPONIBLE = FALSE) " +
                "AND PRIX < 100";

// Requête d : Requête avec un niveau élevé de complexité
String query4 = "SELECT * FROM PRODUITS " +
                "WHERE ( " +
                "    (CATEGORIE = 'Électronique' AND (PRIX BETWEEN 100 AND 800 OR STOCK > 10)) " +
                "    OR " +
                "    (CATEGORIE = 'Meubles' AND DISPONIBLE = TRUE) " +
                ") " +
                "AND STOCK > 0";

            // Relation result = (Relation) compiler.compile(query3);
            // result.afficher();

            String createTableQuery = "CREATE TABLE Distributeur (" +
                          "ID INT," +
                          "Nom VARCHAR, " +
                          "Adresse VARCHAR, " +
                          "Telephone VARCHAR, " +
                          "Email VARCHAR," +
                          "SiteWeb VARCHAR" +
                          ")";

            // String query = "select p.nom,d.nom,p.prix from produits p join distributeur d on p.id = d.id order by p.prix asc";

            // String query = "select * from (select * from (select * from produits) as n) as p join distributeur as d on p.id = d.id";
            // Relation relation = (Relation) compiler.compile(query);
 
            // relation.afficher();

            // String query = "select * from vehicle";
            // Relation relation = (Relation) compiler.compile(query);
 
            // relation.afficher();

            // String insertDataQuery1 = "INSERT INTO Distributeur (ID, Nom, Adresse, Telephone, Email, SiteWeb) VALUES " +
                        //   "(1, 'Supermarché ABC', '123 Rue Principale', '0123456789', 'contact@abc.com', 'www.abc.com'), "+
                        //   "(2, 'Marché XYZ', '456 Rue Secondaire', '0987654321', 'contact@xyz.com', 'www.xyz.com'), "+
                        //   "(3, 'Épicerie Locale', '789 Rue Locale', '0112233445', 'contact@localepicerie.com', 'www.localepicerie.com')";

            // Relation queryResult = (Relation) compiler.compile("select d.id,d.nom,p.nom from distributeur as d join produits as p on p.id = d.id");
            // queryResult.afficher();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}