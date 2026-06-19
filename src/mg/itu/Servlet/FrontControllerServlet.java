package mg.itu.Servlet;

import mg.itu.annotation.Controller.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FrontControllerServlet extends HttpServlet {

    private static Set<String> uris = new HashSet<>();
    private static List<String> controllerClasses = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        System.out.println("[Framework] Scan des classes...");
        String packageToScan = getServletContext().getInitParameter("controllerPackage");
        if (packageToScan == null || packageToScan.isEmpty()) {
            packageToScan = "controller";
        }

        List<String> allClasses = PackageScanner.getClasses(packageToScan);

        for (String className : allClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    controllerClasses.add(className);
                    System.out.println("[Framework] ✅ Controller: " + className);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("[Framework] Total: " + controllerClasses.size() + " controller(s)");
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

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Framework - URLs</title></head><body>");
        out.println("<h1>URL demandée : " + chemin + "</h1>");

        out.println("<h2>📋 Classes avec @Controller :</h2><ul>");
        if (!controllerClasses.isEmpty()) {
            for (String c : controllerClasses) {
                out.println("<li>" + c + "</li>");
            }
        } else {
            out.println("<li>Aucune classe avec @Controller</li>");
        }
        out.println("</ul>");

        out.println("<h2>URLs capturées (" + uris.size() + "):</h2><ul>");
        for (String lien : uris) {
            out.println("<li>" + lien + "</li>");
        }
        out.println("</ul>");

        out.println("</body></html>");
    }
}