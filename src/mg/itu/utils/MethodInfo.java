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

    public static Object[] buildArgs(Method method, HttpServletRequest req, HttpServletResponse resp) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            Class<?> paramType = p.getType();

            System.out.println("[buildArgs] param[" + i + "] name=" + p.getName() + " type=" + paramType.getName());

            if (paramType == HttpServletRequest.class) {
                args[i] = req;
                continue;
            }
            if (paramType == HttpServletResponse.class) {
                args[i] = resp;
                continue;
            }

            if (isBindableObject(paramType)) {
                System.out.println("[buildArgs] -> bindObject " + paramType.getName());
                args[i] = bindObject(paramType, req);
                continue;
            }

            String value = req.getParameter(p.getName());
            System.out.println("[buildArgs] -> simple param " + p.getName() + " = " + value);
            args[i] = convertSimpleValue(value, paramType);
        }

        return args;
    }

    private static boolean isBindableObject(Class<?> type) {
        if (type.isPrimitive())
            return false;
        if (type == String.class)
            return false;
        if (Number.class.isAssignableFrom(type))
            return false;
        if (type == Boolean.class || type == Character.class)
            return false;
        if (java.util.Date.class.isAssignableFrom(type))
            return false;
        if (java.time.temporal.Temporal.class.isAssignableFrom(type))
            return false;
        if (type.isArray() || type.isEnum())
            return false;
        if (jakarta.servlet.ServletRequest.class.isAssignableFrom(type))
            return false;
        if (jakarta.servlet.ServletResponse.class.isAssignableFrom(type))
            return false;
        return true;
    }

    private static Object bindObject(Class<?> type, HttpServletRequest req) {
        try {
            System.out.println("[bindObject] START type=" + type.getName());
            Object instance = type.getDeclaredConstructor().newInstance();

            for (Method setter : type.getMethods()) {
                String name = setter.getName();

                if (!name.startsWith("set") || setter.getParameterCount() != 1)
                    continue;

                String fieldName = name.substring(3, 4).toLowerCase() + name.substring(4);
                String value = req.getParameter(fieldName);

                System.out.println("[bindObject] setter=" + name + " field=" + fieldName + " value=" + value);

                if (value == null || value.isEmpty())
                    continue;

                Class<?> setterType = setter.getParameterTypes()[0];

                try {
                    setter.invoke(instance, convertSimpleValue(value, setterType));
                } catch (Exception e) {
                    System.out.println("[bindObject] FAIL on " + name + " : " + e.getMessage());
                }
            }
            
            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Impossible de binder " + type.getName(), e);
        }
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