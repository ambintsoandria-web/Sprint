package mg.itu.utils;

import java.util.Map;

public class ModelAndView {

    String viewName;
    Map<String, Object> data;

    public ModelAndView(String viewName, Map<String, Object> data) {
        this.viewName = viewName;
        this.data = data;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
// dans le save on va mettre : par exemple save(int nombre,double ...)
// puis on va pouvoir faire
// input type = 'text' name='nom'
// save(String nom,) if 'nom' === 'nom'
// alors on va récuperer la valeur apparement il y a ca dans le invoke
// 7 bis :
// binding object si on a : argument genre : emp exemple : Employe e
// {String nom : "a",String prenom}... 
// donc ca va etre Un objet genre : Employe

// tomcat na wedfly 10 java 8 ihany dia tomcat dia tomcat 9
// connexion base ... ejb misy dossier src/java package
// apj.properties misy lien base 2 donc base 2 ilay izy tsy mandeha
// ant mila anio 
