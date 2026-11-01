# sprint 0
mise en place de la capture d'uri avec : 
String uri = req.getRequestURI();
String contextPath = req.getContextPath();
String chemin = uri.substring(contextPath.length());

// Fichiers statiques   
```
if (chemin.startsWith("/upload/")
|| chemin.startsWith("/css/")
|| chemin.startsWith("/js/")
|| chemin.startsWith("/img/")) {
    getServletContext().getNamedDispatcher("default").forward(req, resp);
    return;
}
*
uris.add(chemin);
```
Objectifs : on affiche toutes les url qui sont capturées à partir de l'application de test

# sprint 1
Mise en place de l'annotation Controller et de PackageScanner pour voir toutes les classes annotées @Controller dans le framework
```
package mg.itu.annotation.Controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
```

```
if (clazz.isAnnotationPresent(Controller.class)) {
    // === SINGLETON : instance unique par controller ===
    Object instance = clazz.getDeclaredConstructor().newInstance();
    controllerInstances.put(className, instance);
    // controllerInstances.put("mg.itu.controller.UserController"
    // eto le izy maka anle hoe exemple {key : "User","User"} sprint 1
```

# sprint 2
Implementation de l'annotation UrlMapping("/emp/saveAll...")
``` 
package mg.itu.annotation.Url;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {
    String value() default "";
}
```
```
if (method.isAnnotationPresent(UrlMapping.class)) {
    UrlMapping rm = method.getAnnotation(UrlMapping.class);
    String url = rm.value();
    String httpMethod = rm.method();
    if (httpMethod == null || httpMethod.isEmpty())
        httpMethod = "GET";
```