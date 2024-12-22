package com.sgbd;

import com.sgbd.*;

import java.util.*;
import java.io.*;
import java.net.*;

public class Relation implements Serializable {

    String nom;
    ArrayList<Domaine> domaines;
    ArrayList<Nuplet> nuplets; 

    public Relation() {
        this.domaines = new ArrayList<>();
        this.nuplets = new ArrayList<>();
    }   

    public Relation(ArrayList<Domaine> domaines) {
        this.domaines = domaines;
        this.nuplets = new ArrayList<Nuplet>();
    }

    public Relation(ArrayList<Domaine> domaines,ArrayList<Nuplet> nupletList) throws Exception {
        this.domaines = domaines;
        this.nuplets = nupletList;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return this.nom;
    }

    public ArrayList<Domaine> getDomaines() {
        return this.domaines;
    }

    public void setDomaines(ArrayList<Domaine> domaines ) {
        this.domaines = domaines;
    }

    public void addDomaine(Domaine domaine) {
        this.domaines.add(domaine);
    }

    public void applyAleasToDomaines() throws Exception {
        if(this.getNom() == null) {
            throw new Exception("Aleas relation is null");
        }
        for(Domaine domaine:this.getDomaines()) {
            domaine.setNom(this.getNom()+"."+domaine.getNom());
        }
    }

    public ArrayList<Nuplet> getNuplets() {
        return this.nuplets;
    }

    public void setNuplets(ArrayList<Nuplet> nuplets) {
        this.nuplets = nuplets;
    }

    public void addNuplet(Nuplet nuplet) throws Exception {
        Nuplet validNuplet = new Nuplet();
        for(Domaine domaine:this.getDomaines()) {
            if(domaine.validate(nuplet.get(domaine.getNom()))) {
                validNuplet.add(domaine.getNom(),nuplet.get(domaine.getNom()));
            } else {
                throw new Exception("Argument does not match datatype for "+domaine.getNom()+" with input "+nuplet.get(domaine.getNom()));
            }
            if(nuplet.get("_index") != null) {
                validNuplet.add("_index",nuplet.get("_index"));
            }
        }
        this.nuplets.add(validNuplet);
    }

    public boolean containsDomaine(String columnName) {
        boolean inside = false;
        for(Domaine domaine:this.getDomaines()) {
            if(domaine.getNom().equals(columnName)) {
                inside = true;
                break;
            }
        }
        return inside;
    }

    public List<Object> getListObjectOf(String columnName) throws Exception {
        List<Object> objects = new ArrayList<>();
        for(Nuplet nuplet:this.getNuplets()) {
            objects.add(nuplet.get(columnName));
        }

        if(objects.isEmpty()) {
            throw new Exception("No such column");
        }

        return objects;
    }

    public Relation union(Relation relation) throws Exception {
        if(this.getDomaines().size() != relation.getDomaines().size()) {
            return null;
        }

        ArrayList<Domaine> nouveauxDomaines = new ArrayList<>();
        ArrayList<Nuplet> nouveauNuplets = new ArrayList<Nuplet>();
        for(int i=0;i<this.getDomaines().size();i++) {
            Domaine d1 = this.getDomaines().get(i);
            Domaine d2 = relation.getDomaines().get(i);
            Domaine d3 = d1.union(d2);
            nouveauxDomaines.add(d3);
        }

        for (Nuplet nuplet : this.getNuplets()) {
            Nuplet nouveauNuplet = changerCleNuplet(nuplet, this.getDomaines(), nouveauxDomaines);
            nouveauNuplets.add(nouveauNuplet);
        }

        for (Nuplet nuplet : relation.getNuplets()) {
            Nuplet nouveauNuplet = changerCleNuplet(nuplet, relation.getDomaines(), nouveauxDomaines);
            nouveauNuplets.add(nouveauNuplet);
        }

        return new Relation(nouveauxDomaines, nouveauNuplets);
    }

    public Nuplet changerCleNuplet(Nuplet ancienNuplet, ArrayList<Domaine> anciensDomaines, ArrayList<Domaine> nouveauxDomaines) {
        Nuplet nouveauNuplet = new Nuplet();
        for(int i = 0 ; i < anciensDomaines.size(); i++) {
            Domaine ancien = anciensDomaines.get(i);
            Domaine nouveau = nouveauxDomaines.get(i);
            Object valeur = ancienNuplet.get(ancien.getNom());
            nouveauNuplet.add(nouveau.getNom(),valeur);
        }
        if(ancienNuplet.get("_index") != null) {
            nouveauNuplet.add("_index",ancienNuplet.get("_index"));
        }
        return nouveauNuplet;
    }

    public Relation selection(Filtre filtre) throws Exception {
        Relation r = filtre.applyFromDomaine(this);
        return r;
    }

    public Relation selection(Filtre filtre1,String operateurLogique,Filtre filtre2) throws Exception {
        Relation val = null;
        Relation r1 = filtre1.applyFromDomaine(this);
        Relation r2 = filtre2.applyFromDomaine(this);
        if(operateurLogique.equals("AND")) {
            //si l'operateur logique est "AND", le deuxieme filtre est appliqué a r1
            val = r2;
        }
        else if(operateurLogique.equals("OR")) {
            //si l'operateur logique est "OR", on retourne l'union des deux filtres
            Relation r3 = r1.union(r2);
            val = r3;
        }
        return val;
    }

    public Relation selection(ArrayList<Filtre> filtres, ArrayList<String> operateursLogiques) throws Exception {
        Relation result = filtres.get(0).applyFromDomaine(this); 
        for (int i = 1; i < filtres.size(); i++) {
            Filtre filtre = filtres.get(i);
            String operateurLogique = operateursLogiques.get(i - 1);  
            if (operateurLogique.equals("AND")) {
                result = filtre.applyFromDomaine(result);  
            } else if (operateurLogique.equals("OR")) {
                result = result.union(filtre.applyFromDomaine(this));  
            }
        }
        
        return result;
    }

    private boolean compareValues(Object columnValue, String operator, Object value) throws Exception {
        String strColumnValue = columnValue.toString();
        String strValue = value.toString();
    
        if (strValue.startsWith("'") && strValue.endsWith("'")) {
            // Enlève les quotes autour de la chaîne
            strValue = strValue.substring(1, strValue.length() - 1);
    
            // Comparaison insensible à la casse pour "=" ou "!="
            switch (operator) {
                case "=":
                    return strColumnValue.equalsIgnoreCase(strValue);
                case "!=":
                    return !strColumnValue.equalsIgnoreCase(strValue);
                default:
                    throw new Exception("Opérateur non valide pour les chaînes : " + operator);
            }
        } else if (columnValue instanceof Comparable && value instanceof Comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> compColumnValue = (Comparable<Object>) columnValue;
            Comparable<Object> compValue = (Comparable<Object>) value;
    
            switch (operator) {
                case "=":
                    return compColumnValue.compareTo(compValue) == 0;
                case "!=":
                    return compColumnValue.compareTo(compValue) != 0;
                case "<":
                    return compColumnValue.compareTo(compValue) < 0;
                case ">":
                    return compColumnValue.compareTo(compValue) > 0;
                case "<=":
                    return compColumnValue.compareTo(compValue) <= 0;
                case ">=":
                    return compColumnValue.compareTo(compValue) >= 0;
                default:
                    throw new Exception("Opérateur non valide pour les comparables : " + operator);
            }
        } else {
            throw new Exception("Les types des valeurs ne sont pas compatibles pour la comparaison.");
        }
    }
    

    public Relation selection(String targetedColumn, String operator, Object value) throws Exception {
        Relation result = new Relation();
        result.setDomaines(this.getDomaines());
    
        for (Nuplet nuplet : this.getNuplets()) {
            Object columnValue = nuplet.get(targetedColumn);
            boolean comparisonResult = compareValues(columnValue, operator, value);
    
            if (comparisonResult) {
                result.addNuplet(nuplet);
            }
        }
    
        return result;
    }
    
        
    public Relation projection(ArrayList<String> nomsDomaines) throws Exception {
        ArrayList<Domaine> domaines = new ArrayList<>();
        for(Domaine domaine:this.getDomaines()) {
            if(nomsDomaines.contains(domaine.getNom())) {
                domaines.add(domaine);
            }
        }
        ArrayList<Nuplet> nuplets = new ArrayList<>();
        for(Nuplet nuplet:this.getNuplets()) {
            Nuplet projectedNuplet = new Nuplet();
            for(String domaine:nomsDomaines) {
                String nupletCall = domaine;
                if(this.getNom() != null) {
                    nupletCall = domaine.substring(domaine.indexOf('.')+1);
                }
                projectedNuplet.add(domaine,nuplet.get(nupletCall));
            }
            if(nuplet.get("_index") != null) {
                projectedNuplet.add("_index",nuplet.get("_index"));
            }
            nuplets.add(projectedNuplet);
        }

        Relation result = new Relation(domaines,nuplets);

        return result;
    }

    public Relation produitCartesien(Relation relation) throws Exception {
        @SuppressWarnings("unchecked")
        ArrayList<Domaine> thisDomaine = (ArrayList<Domaine>) this.getDomaines().clone();
        @SuppressWarnings("unchecked")
        ArrayList<Domaine> autreDomaine = (ArrayList<Domaine>) relation.getDomaines().clone();
        for(Domaine domaine:thisDomaine) {
            domaine.setNom(this.getNom()+"."+domaine.getNom());
        }
        for(Domaine domaine:autreDomaine) {
            domaine.setNom(relation.getNom()+"."+domaine.getNom());
        }
        ArrayList<Domaine> domaineProduit = new ArrayList<Domaine>(thisDomaine);
        domaineProduit.addAll(autreDomaine);
        Relation produit = new Relation(domaineProduit);
        ArrayList<Nuplet> produitNuplet = new ArrayList<Nuplet>();
        for(Nuplet nuplet_init: this.getNuplets()) {
            for(Nuplet nuplet_relation:relation.getNuplets()) {
                Nuplet newProduit = new Nuplet();
                for(String key: nuplet_init.elements.keySet()) {
                    newProduit.add(this.getNom()+"."+key,nuplet_init.get(key));
                }
                for(String key: nuplet_relation.getElements().keySet()) {
                    newProduit.add(relation.getNom()+"."+key,nuplet_relation.get(key));
                }
                produitNuplet.add(newProduit);
            } 
        }
        produit.setNuplets(produitNuplet);

        return produit;
    }

    public static boolean findColumnsTable(Relation table,String columnName) throws Exception {
        boolean belongs = false;
        if(columnName.contains(".")) {
            String tableName = columnName.substring(0,columnName.indexOf("."));
            if(table.getNom() != null && table.getNom().equals(tableName)) {
                belongs = true;
            }
        } else {
            for(Domaine domaine:table.getDomaines()) {
                if(domaine.getNom().equals(columnName)) {
                    belongs = true;
                    break;
                }
            }
        }

        return belongs;
    }

    public Relation join(Relation relation,String colA,String operator,String colB) throws Exception {

        Relation produit = this.produitCartesien(relation);
        Relation joined = new Relation(produit.getDomaines());
        for(Nuplet nuplet:produit.getNuplets()) {
            Object valA = nuplet.get(colA);
            Object valB = nuplet.get(colB);
            boolean equal = compareValues(valA, operator, valB);
            if(equal) {
                joined.addNuplet(nuplet);
            }
        }        
        return joined;
    }

    public Relation join(Relation relation,Filtre filtre) throws Exception {
        Relation produit = this.produitCartesien(relation);
        produit = filtre.applyFromOperands(produit);
        return produit;
    }

    public Relation intersection(Relation relation) throws Exception {

        ArrayList<Nuplet> nuplets = new ArrayList<Nuplet>();
        for(Nuplet nuplet_init: this.getNuplets()) {
            for(Nuplet nuplet_relation: relation.getNuplets()) {
                if(nuplet_init.equals(nuplet_relation)) {
                    nuplets.add(nuplet_init);
                }
            }
        }

        return new Relation(this.getDomaines(),nuplets);
    }

    public Relation difference(Relation relation) throws Exception {
        Relation intersect = this.intersection(relation);
        ArrayList<Nuplet> nuplets = new ArrayList<Nuplet>();
        for(Nuplet nuplet_init: this.getNuplets()) {
            if(intersect.getNuplets().contains(nuplet_init) == false) {
                nuplets.add(nuplet_init);
            }
        }
        for(Nuplet nuplet_relation: relation.getNuplets()) {
            if(intersect.getNuplets().contains(nuplet_relation) == false) {
                nuplets.add(nuplet_relation);
            }
        }

        return new Relation(this.getDomaines(),nuplets);
    }

    public void afficher() {
        if (domaines.isEmpty()) {
            System.out.println("La relation n'a pas de domaines.");
            return;
        }
        if (nuplets.isEmpty()) {
            System.out.println("La relation est vide.");
            return;
        }

        // Déterminer les largeurs maximales de chaque colonne
        List<Integer> columnWidths = new ArrayList<>();
        for (Domaine domaine : domaines) {
            int maxWidth = domaine.getNom().length();
            for (Nuplet nuplet : nuplets) {
                Object valeur = nuplet.get(domaine.getNom());
                if (valeur != null) {
                    maxWidth = Math.max(maxWidth, valeur.toString().length());
                }
            }
            columnWidths.add(maxWidth);
        }

        // Afficher la ligne des noms de colonnes
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < domaines.size(); i++) {
            String nom = domaines.get(i).getNom();
            header.append(String.format("%-" + columnWidths.get(i) + "s | ", nom));
        }
        System.out.println(header.toString());
        String ligneDeSeparation = "-";
        for(int i = 0; i < header.length();i++) {
            ligneDeSeparation += "-"; 
        }

        System.out.println(ligneDeSeparation);

        // Afficher les données des nuplets
        for (Nuplet nuplet : nuplets) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < domaines.size(); i++) {
                Domaine domaine = domaines.get(i);
                Object valeur = nuplet.get(domaine.getNom());
                row.append(String.format("%-" + columnWidths.get(i) + "s | ", valeur != null ? valeur : "NULL"));
            }
            System.out.println(row.toString());
        }
    }

    public String getAffichage() {
        if (domaines.isEmpty()) {
            return "La relation n'a pas de domaines.";
        }
        if (nuplets.isEmpty()) {
            return "La relation est vide.";
        }

        // Déterminer les largeurs maximales de chaque colonne
        List<Integer> columnWidths = new ArrayList<>();
        for (Domaine domaine : domaines) {
            int maxWidth = domaine.getNom().length();
            for (Nuplet nuplet : nuplets) {
                Object valeur = nuplet.get(domaine.getNom());
                if (valeur != null) {
                    maxWidth = Math.max(maxWidth, valeur.toString().length());
                }
            }
            columnWidths.add(maxWidth);
        }

        // Construire le contenu dans un StringBuilder
        StringBuilder output = new StringBuilder();

        // Ajouter la ligne des noms de colonnes
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < domaines.size(); i++) {
            String nom = domaines.get(i).getNom();
            header.append(String.format("%-" + columnWidths.get(i) + "s | ", nom));
        }
        output.append(header.toString()).append("!!");
        output.append("-".repeat(header.length())).append("!!"); // Ligne de séparation

        // Ajouter les données des nuplets
        for (Nuplet nuplet : nuplets) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < domaines.size(); i++) {
                Domaine domaine = domaines.get(i);
                Object valeur = nuplet.get(domaine.getNom());
                row.append(String.format("%-" + columnWidths.get(i) + "s | ", valeur != null ? valeur : "NULL"));
            }
            output.append(row.toString()).append("!!");
        }
        // Retourner le contenu sous forme de chaîne
        return output.toString();
    }

    public void suppDoublons() {
        for(int i = 0; i < this.getNuplets().size(); i++) {
            for(int j = 0; j < this.getNuplets().size(); j++) {
                if(this.getNuplets().get(i).equals(this.getNuplets().get(j)) && i != j) {
                    this.getNuplets().remove(j);
                }
            }
        }
    }

    public static Number parseNumber(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("La chaîne est vide ou nulle.");
        }

        try {
            // Si c'est un entier
            if (input.matches("-?\\d+")) { 
                return Integer.parseInt(input); // Retourne un Integer
            }

            // Si c'est un nombre décimal
            if (input.matches("-?\\d*\\.\\d+")) {
                return Double.parseDouble(input); // Retourne un Double
            }

            // Si le format est incorrect
            throw new NumberFormatException("Format invalide : " + input);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La chaîne n'est pas un nombre valide.", e);
        }
    }

    public static void main(String[] args) throws Exception {
        Relation table = new Relation();
        table.setNom("ecolier");
        ArrayList<Domaine> domaines = new ArrayList<>();
        domaines.add(new Domaine("nom","VARCHAR"));
        domaines.add(new Domaine("prenom","VARCHAR"));
        domaines.add(new Domaine("age","INT"));
        table.setDomaines(domaines);
        boolean belongs = findColumnsTable(table,"ecolier.voude");
        System.out.println(belongs);

    }
}