package mg.itu.Servlet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import mg.itu.annotation.Controller.Controller;
import mg.itu.annotation.Security.RequireLogin;
import mg.itu.annotation.Url.UrlMapping;
import mg.itu.utils.Database;
import mg.itu.utils.MethodInfo;
import mg.itu.utils.PackageScanner;
import mg.itu.utils.Session;
import mg.itu.utils.UrlMethod;

@WebListener
public class AppListener implements ServletContextListener {

    public static Map<UrlMethod, MethodInfo> urlMethodMappings = new HashMap<>();
    public static Map<String, MethodInfo> protectedViews = new HashMap<>();
    public static String viewPrefix = "";
    public static String viewSuffix = "";
    public static RuntimeException initError = null;
    public static String sessionUserAttribute = "user";
    public static String roleGetter = "getRole";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Framework] Demarrage...");

        ServletContext context = sce.getServletContext();
        Database.init(context);

        viewPrefix = context.getInitParameter("viewPrefix");
        if (viewPrefix == null)
            viewPrefix = "/";

        viewSuffix = context.getInitParameter("viewSuffix");
        if (viewSuffix == null)
            viewSuffix = ".jsp";

        String userAttr = context.getInitParameter("sessionUserAttribute");
        if (userAttr != null && !userAttr.isEmpty())
            sessionUserAttribute = userAttr;

        String rg = context.getInitParameter("roleGetter");
        if (rg != null && !rg.isEmpty())
            roleGetter = rg;

        Map<String, Object> sessionMap = new HashMap<>();
        context.setAttribute("session", sessionMap);
        Session.init(context, sessionMap);

        String packageToScan = context.getInitParameter("controllerPackage");
        if (packageToScan == null || packageToScan.isEmpty())
            packageToScan = "controlleur";

        List<String> allClasses = PackageScanner.getClasses(packageToScan);

        try {
            for (String className : allClasses) {
                try {
                    Class<?> clazz = Class.forName(className);

                    if (clazz.isAnnotationPresent(Controller.class)) {
                        System.out.println("[Framework] Controller: " + className);

                        for (Method method : clazz.getMethods()) {
                            if (method.isAnnotationPresent(UrlMapping.class)) {
                                UrlMapping rm = method.getAnnotation(UrlMapping.class);
                                String url = rm.value();
                                String httpMethod = rm.method();
                                if (httpMethod == null || httpMethod.isEmpty())
                                    httpMethod = "GET";

                                UrlMethod key = new UrlMethod(url, httpMethod);

                                if (urlMethodMappings.containsKey(key)) {
                                    throw new RuntimeException("URL dupliquee: " + httpMethod + " " + url);
                                }

                                boolean isJson = method.isAnnotationPresent(
                                        mg.itu.annotation.Json.Json.class);

                                boolean requireLogin = method.isAnnotationPresent(RequireLogin.class);
                                String requiredRole = "";
                                if (requireLogin) {
                                    requiredRole = method.getAnnotation(RequireLogin.class).role();
                                }

                                MethodInfo info = new MethodInfo(className, method.getName(), isJson,
                                        requireLogin, requiredRole);
                                urlMethodMappings.put(key, info);

                                // Enregistrer la vue protegee (goToXxx -> xxx)
                                if (requireLogin) {
                                    String m = method.getName();
                                    if (m.startsWith("goTo") && m.length() > 4) {
                                        String vue = m.substring(4);
                                        vue = Character.toLowerCase(vue.charAt(0)) + vue.substring(1);
                                        protectedViews.put(vue, info);
                                        System.out.println("[Framework] Vue protegee: '" + vue
                                                + "' role='" + requiredRole + "'");
                                    }
                                }

                                System.out.println("[Framework] Mapping: " + httpMethod + " " + url
                                        + " -> " + className + "." + method.getName()
                                        + (isJson ? " [JSON]" : "")
                                        + (requireLogin ? " [LOGIN]" : ""));
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            initError = e;
            e.printStackTrace();
        }

        System.out.println("[Framework] Total mappings: " + urlMethodMappings.size());
        System.out.println("[Framework] Vues protegees: " + protectedViews.size());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[Framework] Arret.");
    }
}