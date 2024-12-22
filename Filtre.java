package com.sgbd;

import com.sgbd.*;
  
  
  import java.util.*;

  import java.io.*;

  
    //pour les selections
    public class Filtre implements Serializable {
        String nomDomaine;
        String operateur;
        Object valeur;

        String leftOperand;
        String rightOperand;

        public Filtre(String nomDomaine,String operateur,Object valeur) {
            // [domaine] > valeur
            // if(valeur instanceof Number) {
            //     this.valeur = ((Number) valeur).doubleValue();
            // }
            this.nomDomaine = nomDomaine;
            this.operateur = operateur;
            try {
                valeur = Integer.parseInt(valeur.toString());
            }catch(Exception e) {
                try {
                    valeur = Double.parseDouble(valeur.toString());
                }catch(Exception f) {}
            }
            this.valeur = valeur;
            this.leftOperand = null;
            this.rightOperand = null;
        }

        public Filtre(String leftOperand,String operateur,String rightOperand) {
            // leftOperand > rightOperand
            this.leftOperand = leftOperand;
            this.operateur = operateur;
            this.rightOperand = rightOperand;
            this.nomDomaine = null;
            this.valeur = null;
        }

        public Relation applyFromOperands(Relation relation) throws Exception {
            ArrayList<Nuplet> nuplets = new ArrayList<Nuplet>();
            for(Nuplet nuplet:relation.getNuplets()) {
                boolean condition = false;
                switch(this.operateur) {
                    case "=":
                        condition = nuplet.get(leftOperand).equals(nuplet.get(rightOperand));
                        break;
                    case ">":
                        condition = ((Comparable) nuplet.get(leftOperand)).compareTo(nuplet.get(rightOperand)) > 0 ;
                        break;
                    case "<":
                        condition = ((Comparable) nuplet.get(leftOperand)).compareTo(nuplet.get(rightOperand)) < 0 ;
                        break;
                }
                if(condition) {
                    nuplets.add(nuplet);
                }
            }

            return new Relation(relation.getDomaines(),nuplets);
        }

        public Relation applyFromDomaine(Relation relation) throws Exception {
            ArrayList<Nuplet> nuplets = new ArrayList<Nuplet>();
            for(Nuplet nuplet:relation.getNuplets()) {
                boolean condition = false;
                switch(this.operateur) {
                    case "=":
                        condition = nuplet.get(this.nomDomaine).equals(this.valeur);
                        break;
                    case ">":
                        condition = ((Comparable) nuplet.get(this.nomDomaine)).compareTo(this.valeur) > 0 ;
                        break;
                    case "<":
                        condition = ((Comparable) nuplet.get(this.nomDomaine)).compareTo(this.valeur) < 0 ;
                        break;
                }
                if(condition) {
                    nuplets.add(nuplet);
                }
            }
            Relation filtre = new Relation(relation.getDomaines(),nuplets);

            return filtre;
        }

        public String getLeftOperand() {
            return this.leftOperand;
        }

        public String getRightOperand() {
            return this.rightOperand;
        }
    } 