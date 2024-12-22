package com.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utils {
     public static void chercherCombinaisons(int listIndex,List<List<Object>> valeursPossibles,List<Object> combinaisonActuelle,List<List<Object>> combinaisons) {

        if(listIndex == valeursPossibles.size()) {
            combinaisons.add(new ArrayList<>(combinaisonActuelle));
            return;
        }
        
        for(Object object:valeursPossibles.get(listIndex)) {
            combinaisonActuelle.add(object);
            chercherCombinaisons(listIndex+1,valeursPossibles,combinaisonActuelle,combinaisons);
            combinaisonActuelle.remove(combinaisonActuelle.size()-1);
        }
    }
    
    public static List<List<Object>> combinaison(List<List<Object>> valeursPossibles) {
        List<List<Object>> combinaisons = new ArrayList<>();
        List<Object> combinaisonActuelle = new ArrayList<>();
        chercherCombinaisons(0,valeursPossibles,combinaisonActuelle,combinaisons);
        return combinaisons;
    }

    public double sum(ArrayList<Object> objects) throws Exception {
        double sum = 0;
        for(Object object:objects) {
            sum += Double.parseDouble(object.toString());
        }
        return sum;
    }

    public int count(ArrayList<Object> objects) {
        return objects.size();
    }

    public double avg(ArrayList<Object> objects) throws Exception {
        double sum = sum(objects);
        double count = count(objects);
        return sum/count;
    }

}
