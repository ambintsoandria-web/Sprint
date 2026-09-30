# passage par reference 
eviter les doubles boucles ... 

# sprint3 
@Controlleur = ("/Test","GET") 
@Controlleur = ("/Test","POST") 
Faire une classe et ... 
equals annotation 2 eme argument et tout le reste ... 
faire une classe UrlMethod : classe methode hoe get na post de aveo 

<!-- prototype --> 
# sprint3 bis 
/test GET puis on execute par défaut c'est GET 
on va mettre un SYstem.out.println dans la console de tomcat 
le 3 bis juste mi executer anle izy 

# sprint 4
Implémentation de AppListener qui va hériter de ServletContextListener
on va donc implémenter AppListener et y mettre la logique d'initialisation au lieu de le mettre dans void dans FrontControllerServlet

# sprint 5
prefixe et suffixe 
invoker la methode in recup la valeur de retour
model and View on prend l'instance et on prend l'url et la view avec le dispatcher servlet je sais pas ....
dans request on fait setAttribute et on boucle ...String object
web.xml on va mettre prefixe et suffixe
setUrl

model and view View et la Map donnee 
on concate dans le web.xml 
    
On utilise le dispatcher 

on fait le model.addAttribute mais ici on fait : 

Atao anaty base de donnée
genre hoe tsy atao version hoe ...


on doit avoir un spring container on doit avoir un listener déjà intégré dans spring comment faiere pour demmarer le conteneur spring au démarrage de l'app
on ne creep as de listenener on declare dans le web.xml de l'app de test
fichier de configutaion config de spring blabla.xml 
getBean


Equivalent de autowired 


raha String donc c'est direct
si non c'est toJson voilà !!! 

En gros dans ce sprint 6 on va faire du json genre du toJson y aura une fonction qui va permettre de transformer en json mais ne retourne plus ModelAndView 
Donc dans le controller de l'application test on va mettre @Jon exactement comme tu l'avait dit 
et apparement dans la requete il faut dire si c'est json ou pas si c'est du string alors on y a va directement on convertit pas mais c'est du string rien à convertir en json 
si non on passe par toJson 