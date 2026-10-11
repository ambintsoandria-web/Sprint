package mg.itu.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MethodInfo {
    public String className;
    public String methodName;
    public boolean isJson;

    public MethodInfo(String className, String methodName, boolean isJson) {
        this.className = className;
        this.methodName = methodName;
        this.isJson = isJson;
    }

    // Sprint 7 : seulement les types simples (pas de binding POJO)
    public static Object[] buildArgs(Method method, HttpServletRequest req, HttpServletResponse resp) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            Class<?> paramType = p.getType();

            // Injection speciale
            if (paramType == HttpServletRequest.class) {
                args[i] = req;
                continue;
            }
            if (paramType == HttpServletResponse.class) {
                args[i] = resp;
                continue;
            }

            // Parametre de formulaire simple
            String value = req.getParameter(p.getName());
            args[i] = convertSimpleValue(value, paramType);
        }

        return args;
    }

    private static Object convertSimpleValue(String value, Class<?> type) {
        if (value == null || value.isEmpty())
            return null;

        try {
            if (type == String.class)
                return value;
            if (type == int.class || type == Integer.class)
                return Integer.parseInt(value);
            if (type == double.class || type == Double.class)
                return Double.parseDouble(value);
            if (type == long.class || type == Long.class)
                return Long.parseLong(value);
            if (type == boolean.class || type == Boolean.class)
                return Boolean.parseBoolean(value);
            if (type == float.class || type == Float.class)
                return Float.parseFloat(value);
            if (type == short.class || type == Short.class)
                return Short.parseShort(value);
            if (type == byte.class || type == Byte.class)
                return Byte.parseByte(value);
            if (type == char.class || type == Character.class)
                return value.charAt(0);
            if (type == java.util.Date.class) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                return sdf.parse(value);
            }
            if (type == java.sql.Date.class)
                return java.sql.Date.valueOf(value);
            if (type == java.time.LocalDate.class)
                return java.time.LocalDate.parse(value);
            if (type == java.time.LocalDateTime.class)
                return java.time.LocalDateTime.parse(value);
            if (type == java.time.LocalTime.class)
                return java.time.LocalTime.parse(value);

            return value;
        } catch (Exception e) {
            throw new RuntimeException("Impossible de convertir '" + value + "' en " + type.getSimpleName(), e);
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean IsJson() {
        return isJson;
    }

    public void setIsJson(boolean isJson) {
        this.isJson = isJson;
    }
}