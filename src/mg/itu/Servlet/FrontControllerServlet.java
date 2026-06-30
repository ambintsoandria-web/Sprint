package mg.itu.Servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.Controller.Controller;
import mg.itu.annotation.Url.UrlMapping;
import mg.itu.utils.MethodInfo;
import mg.itu.utils.PackageScanner;
import mg.itu.utils.UrlMethod;

public class FrontControllerServlet extends HttpServlet {

    private static Set<String> uris = new HashSet<>();
    private static Map<UrlMethod, MethodInfo> urlMethodMappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        System.out.println("[Framework] Scan des classes...");

        String packageToScan = getServletContext().getInitParameter("controllerPackage");
        if (packageToScan == null || packageToScan.isEmpty()) {
            packageToScan = "controlleur";
        }

        List<String> allClasses = PackageScanner.getClasses(packageToScan);

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

                            if (httpMethod == null || httpMethod.isEmpty()) {
                                httpMethod = "GET";
                            }

                            UrlMethod key = new UrlMethod(url, httpMethod);

                            if (urlMethodMappings.containsKey(key)) {
                                throw new ServletException("URL dupliquee: " + httpMethod + " " + url);
                            }

                            MethodInfo info = new MethodInfo(className, method.getName());
                            urlMethodMappings.put(key, info);

                            System.out.println("[Framework] Mapping: " + httpMethod + " " + url + " -> " + className
                                    + "." + method.getName());
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Framework] Total mappings: " + urlMethodMappings.size());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String chemin = uri.substring(contextPath.length());
        String httpMethod = req.getMethod();

        uris.add(chemin);

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h1>URL: " + chemin + "</h1>");
        out.println("<p>Methode HTTP: " + httpMethod + "</p>");

        UrlMethod key = new UrlMethod(chemin, httpMethod);

        if (urlMethodMappings.containsKey(key)) {
            MethodInfo info = urlMethodMappings.get(key);
            String simpleName = info.className.substring(info.className.lastIndexOf('.') + 1);
            out.println("<p>Classe: " + simpleName + "</p>");
            out.println("<p>Methode: " + info.methodName + "</p>");

            try { 
                Class<?> clazz = Class.forName(info.className);
                Object instance = clazz.getDeclaredConstructor().newInstance();

                Method method = null;
                try {
                    method = clazz.getMethod(info.methodName, HttpServletRequest.class, HttpServletResponse.class);
                    method.invoke(instance, req, resp);
                } catch (NoSuchMethodException e) {
                    method = clazz.getMethod(info.methodName);
                    method.invoke(instance);
                }

            } catch (Exception e) {
                out.println("<p>Erreur: " + e.getMessage() + "</p>");
                e.printStackTrace();
            }
        } else {
            out.println("<p>404 - Aucun mapping pour: " + httpMethod + " " + chemin + "</p>");
            out.println("<h2>Mappings disponibles</h2><ul>");
            for (Map.Entry<UrlMethod, MethodInfo> entry : urlMethodMappings.entrySet()) {
                String simpleName = entry.getValue().className
                        .substring(entry.getValue().className.lastIndexOf('.') + 1);
                out.println(
                        "<li>" + entry.getKey().getMethod() + " " + entry.getKey().getUrl() + " -> " +
                                simpleName + "." + entry.getValue().methodName + "</li>");
            }
            out.println("</ul>");
        }

        out.println("<hr>");
        out.println("<h2>URLs visitees (" + uris.size() + ")</h2><ul>");
        for (String lien : uris) {
            out.println("<li>" + lien + "</li>");
        }
        out.println("</ul>");

        out.println("</body></html>");
    }
}