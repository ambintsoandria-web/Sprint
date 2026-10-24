package mg.itu.Servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.utils.JsonConverter;
import mg.itu.utils.MethodInfo;
import mg.itu.utils.ModelAndView;
import mg.itu.utils.Session;
import mg.itu.utils.UrlMethod;

public class FrontControllerServlet extends HttpServlet {

    private static Set<String> uris = new HashSet<>();

    @Override
    public void init() throws ServletException {
        System.out.println("[Framework] FrontControllerServlet initialise");

        String viewPrefix = getServletContext().getInitParameter("viewPrefix");
        if (viewPrefix == null)
            viewPrefix = "/";

        String viewSuffix = getServletContext().getInitParameter("viewSuffix");
        if (viewSuffix == null)
            viewSuffix = ".jsp";

        AppListener.viewPrefix = viewPrefix;
        AppListener.viewSuffix = viewSuffix;

        System.out.println("[Framework] viewPrefix = " + viewPrefix);
        System.out.println("[Framework] viewSuffix = " + viewSuffix);

        if (AppListener.urlMethodMappings.isEmpty()) {
            System.out.println("[Framework] Aucun mapping charge par AppListener");
        } else {
            System.out.println("[Framework] " + AppListener.urlMethodMappings.size() + " mappings disponibles");
        }
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

        if (AppListener.initError != null) {
            throw new ServletException(AppListener.initError);
        }

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String chemin = uri.substring(contextPath.length());
        String httpMethod = req.getMethod();

        if (chemin.startsWith("/upload/")
                || chemin.startsWith("/css/")
                || chemin.startsWith("/js/")
                || chemin.startsWith("/img/")) {
            getServletContext().getNamedDispatcher("default").forward(req, resp);
            return;
        }

        uris.add(chemin);

        UrlMethod key = new UrlMethod(chemin, httpMethod);

        if (AppListener.urlMethodMappings.containsKey(key)) {
            MethodInfo info = AppListener.urlMethodMappings.get(key);
            String simpleName = info.className.substring(info.className.lastIndexOf('.') + 1);

            try {
                Class<?> clazz = Class.forName(info.className);
                Object instance = clazz.getDeclaredConstructor().newInstance();

                Object result = null;
                Object[] args = null;

                Method targetMethod = null;
                for (Method m : clazz.getMethods()) {
                    if (m.getName().equals(info.methodName)) {
                        targetMethod = m;
                        break;
                    }
                }

                if (targetMethod == null) {
                    throw new ServletException("Methode introuvable: " + info.methodName);
                }

                Class<?>[] paramTypes = targetMethod.getParameterTypes();

                if (paramTypes.length == 2
                        && paramTypes[0] == HttpServletRequest.class
                        && paramTypes[1] == HttpServletResponse.class) {

                    result = targetMethod.invoke(instance, req, resp);

                } else {
                    args = MethodInfo.buildArgs(targetMethod, req, resp);
                    result = targetMethod.invoke(instance, args);
                }

                // Commit des Sessions
                if (args != null) {
                    for (Object arg : args) {
                        if (arg instanceof Session) {
                            ((Session) arg).commit(req.getSession());
                        }
                    }
                }

                if (info.IsJson()) {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    PrintWriter out = resp.getWriter();

                    if (result == null) {
                        out.print("null");
                    } else if (result instanceof String) {
                        out.print((String) result);
                    } else {
                        out.print(JsonConverter.toJson(result));
                    }
                    out.flush();
                    return;
                }

                if (result != null && result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;

                    if (mv.getData() != null) {
                        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    String viewPath = AppListener.viewPrefix + mv.getViewName() + AppListener.viewSuffix;
                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                    dispatcher.forward(req, resp);
                    return;
                }

                if (result != null && result instanceof String) {
                    String viewPath = AppListener.viewPrefix + result.toString() + AppListener.viewSuffix;
                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                    dispatcher.forward(req, resp);
                    return;
                }

                resp.setContentType("text/html");
                PrintWriter out = resp.getWriter();
                out.println("<html><body>");
                out.println("<h1>URL: " + chemin + "</h1>");
                out.println("<p>Methode HTTP: " + httpMethod + "</p>");
                out.println("<p>Classe: " + simpleName + "</p>");
                out.println("<p>Methode: " + info.methodName + "</p>");
                if (result != null) {
                    out.println("<p>Retour: " + result.toString() + "</p>");
                }
                out.println("</body></html>");
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body>");
            out.println("<h1>404 - Aucun mapping pour: " + httpMethod + " " + chemin + "</h1>");
            out.println("<h2>Mappings disponibles</h2><ul>");
            for (Map.Entry<UrlMethod, MethodInfo> entry : AppListener.urlMethodMappings.entrySet()) {
                String simpleName = entry.getValue().className
                        .substring(entry.getValue().className.lastIndexOf('.') + 1);
                out.println(
                        "<li>" + entry.getKey().getMethod() + " " + entry.getKey().getUrl() + " -> " +
                                simpleName + "." + entry.getValue().methodName + "</li>");
            }
            out.println("</ul>");
            out.println("</body></html>");
        }
    }
}