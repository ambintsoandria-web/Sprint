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
import jakarta.servlet.http.HttpSession;
import mg.itu.annotation.Security.RequireLogin;
import mg.itu.utils.JsonConverter;
import mg.itu.utils.MethodInfo;
import mg.itu.utils.ModelAndView;
import mg.itu.utils.Session;
import mg.itu.utils.UrlMethod;

public class FrontControllerServlet extends HttpServlet {

    private static Set<String> uris = new HashSet<>();

    @Override
    public void init() throws ServletException {
        String viewPrefix = getServletContext().getInitParameter("viewPrefix");
        if (viewPrefix == null)
            viewPrefix = "/";

        String viewSuffix = getServletContext().getInitParameter("viewSuffix");
        if (viewSuffix == null)
            viewSuffix = ".jsp";

        AppListener.viewPrefix = viewPrefix;
        AppListener.viewSuffix = viewSuffix;
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

        if (AppListener.initError != null)
            throw new ServletException(AppListener.initError);

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

                // Verif sur la methode courante
                if (targetMethod.isAnnotationPresent(RequireLogin.class)) {
                    RequireLogin ann = targetMethod.getAnnotation(RequireLogin.class);

                    HttpSession hs = req.getSession(false);
                    Object user = (hs != null) ? hs.getAttribute(AppListener.sessionUserAttribute) : null;

                    if (user == null) {
                        forbidden(resp, "Vous devez etre connecte.");
                        return;
                    }

                    if (!ann.role().isEmpty() && !MethodInfo.hasRole(user, ann.role())) {
                        forbidden(resp, "Role insuffisant.");
                        return;
                    }
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
                    if (result == null)
                        out.print("null");
                    else if (result instanceof String)
                        out.print((String) result);
                    else
                        out.print(JsonConverter.toJson(result));
                    out.flush();
                    return;
                }

                if (result != null && result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;

                    if (!checkView(mv.getViewName(), req, resp))
                        return;

                    if (mv.getData() != null) {
                        for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                            req.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    String viewPath = AppListener.viewPrefix + mv.getViewName() + AppListener.viewSuffix;
                    req.getRequestDispatcher(viewPath).forward(req, resp);
                    return;
                }

                if (result != null && result instanceof String) {
                    String viewName = result.toString();

                    if (!checkView(viewName, req, resp))
                        return;

                    String viewPath = AppListener.viewPrefix + viewName + AppListener.viewSuffix;
                    req.getRequestDispatcher(viewPath).forward(req, resp);
                    return;
                }

                resp.setContentType("text/html");
                PrintWriter out = resp.getWriter();
                out.println("<html><body><h1>URL: " + chemin + "</h1></body></html>");
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body><h1>404: " + httpMethod + " " + chemin + "</h1></body></html>");
        }
    }

    private boolean checkView(String viewName, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (!AppListener.protectedViews.containsKey(viewName))
            return true;

        MethodInfo pv = AppListener.protectedViews.get(viewName);

        HttpSession hs = req.getSession(false);
        Object user = (hs != null) ? hs.getAttribute(AppListener.sessionUserAttribute) : null;

        if (user == null) {
            forbidden(resp, "Vous devez etre connecte.");
            return false;
        }

        if (!pv.requiredRole.isEmpty() && !MethodInfo.hasRole(user, pv.requiredRole)) {
            forbidden(resp, "Role insuffisant.");
            return false;
        }

        return true;
    }

    private void forbidden(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body><h1>403 - Acces refuse</h1><p>" + message + "</p></body></html>");
    }
}