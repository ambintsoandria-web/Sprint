package mg.itu.Servlet;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        Map<String, Integer> EleveNotes = new HashMap<>();
        EleveNotes.put("Jean", 18);
        EleveNotes.put("MARIE", 19);
        for (Map.Entry<String, Integer> note : EleveNotes.entrySet()) {
            System.out.println("clée : " + note.getKey() + "; valeur : " + note.getValue());
        }
        for (int i = 0; i < EleveNotes.size(); i++) {
            System.out.println(
                    "Clée : " + EleveNotes.keySet().toArray()[i] + "; valeur : " + EleveNotes.values().toArray()[i]);
        }
    }
}

// Surdefinition de la fonction equals donc on teste si c'est genre : Post ou
// bien Get et tout le reste
// on fait aussi :