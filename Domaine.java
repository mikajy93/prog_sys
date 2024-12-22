package com.sgbd;

import com.sgbd.*;

import java.util.*;
import java.lang.reflect.*;

import java.io.*;

public class Domaine implements Serializable {
    String nom;
    String type;

    public Domaine() {}

    public Domaine(String nom,String type) {
        this.nom = nom;
        this.type = type;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Domaine union(Domaine domaine) throws Exception {
        if(this.equals(domaine)) {
            return domaine;
        }
        Domaine nouveauDomaine = new Domaine();
        nouveauDomaine.setNom(this.getNom()+"+"+domaine.getNom());
        if(this.getType().equals(domaine.getType())) {
            nouveauDomaine.setType(this.getType());
        } else {
            throw new Exception("Incompatible datatypes");
        }
        return nouveauDomaine;
    }

    public boolean validate(Object x) throws Exception {
        SQLTypeChecker checker = new SQLTypeChecker();
        Method checkerMethod = checker.getClass().getMethod("if"+this.getType(),Object.class);
        boolean result = (Boolean) checkerMethod.invoke(checker,x);
        return result;
    }
}