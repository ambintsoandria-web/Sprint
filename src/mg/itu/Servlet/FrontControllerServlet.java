package mg.itu.Servlet;

import mg.itu.annotation.Controller.Controller;
import mg.itu.annotation.Url.UrlMapping;
import mg.itu.utils.PackageScanner;
import mg.itu.utils.MethodInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrontControllerServlet extends HttpServlet {

    private static Set<String> uris = new HashSet<>();
    private static Map<String, MethodInfo> urlMappings = new HashMap<>();

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
                            MethodInfo info = new MethodInfo(className, method.getName(), url);
                            urlMappings.put(url, info);
                            System.out.println(
                                    "[Framework] Mapping: " + url + " -> " + className + "." + method.getName());
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Framework] Total mappings: " + urlMappings.size());
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

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String chemin = uri.substring(contextPath.length());

        uris.add(chemin);

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><body>");
        out.println("<h1>URL: " + chemin + "</h1>");

        if (urlMappings.containsKey(chemin)) {
            MethodInfo info = urlMappings.get(chemin);
            String simpleName = info.className.substring(info.className.lastIndexOf('.') + 1);
            out.println("<p>Classe: " + simpleName + "</p>");
            out.println("<p>Methode: " + info.methodName + "</p>");
        } else {
            out.println("<p>404 - Aucun mapping pour: " + chemin + "</p>");
            out.println("<h2>Mappings disponibles</h2><ul>");
            for (Map.Entry<String, MethodInfo> entry : urlMappings.entrySet()) {
                String simpleName = entry.getValue().className
                        .substring(entry.getValue().className.lastIndexOf('.') + 1);
                out.println(
                        "<li>" + entry.getKey() + " -> " + simpleName + "." + entry.getValue().methodName + "</li>");
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