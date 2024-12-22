package com.sgbd;

import com.sgbd.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.io.*;


public class Nuplet implements Serializable {
    HashMap<String,Object> elements;

    public Nuplet() {
        this.elements = new HashMap<>();
    }

    public HashMap<String,Object> getElements() {
        return this.elements;
    }

    public void setElements(HashMap<String,Object> elements) {
        this.elements = elements;
    }

    public void add(String nomDomaine,Object valeur) {
        this.elements.put(nomDomaine,valeur);
    }

    public Object get(String nomDomaine) {
        return this.elements.get(nomDomaine);
    }

    public int getNombreColonnes() {
        return this.elements.size();
    }

    public Set<String> getNomDomaines() {
        return this.elements.keySet();
    }

    public Nuplet produit(Nuplet nuplet) {
        Nuplet produit = new Nuplet();
        for(String key: this.elements.keySet()) {
            produit.add(key,this.get(key));
        }
        for(String key: nuplet.getElements().keySet()) {
            produit.add(key,nuplet.get(key));
        }
        return produit;
    }

    public void afficherNuplet() {
        if (this.elements.isEmpty()) {
            System.out.println("Nuplet vide.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (String nomDomaine : this.elements.keySet()) {
            sb.append(nomDomaine)
              .append(" : ")
              .append(this.elements.get(nomDomaine))
              .append(" | ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 3); 
        }

        System.out.println(sb.toString());
    }    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; 
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; 
        }
        Nuplet other = (Nuplet) obj;
        return Objects.equals(this.elements, other.elements);
    }
}