package mg.itu.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
            Class<?> type = p.getType();

            if (type == HttpServletRequest.class) {
                args[i] = req;
            } else if (type == HttpServletResponse.class) {
                args[i] = resp;
            } else if (Map.class.isAssignableFrom(type)) {
                args[i] = bindMap(p, req);
            } else if (Collection.class.isAssignableFrom(type)) {
                args[i] = bindCollection(p, req);
            } else if (isBindableObject(type)) {
                args[i] = bindObject(type, req);
            } else {
                args[i] = convertSimpleValue(req.getParameter(p.getName()), type);
            }
        }

        return args;
    }

    // Employe.nom, Employe.prenom, ...
    private static Object bindObject(Class<?> type, HttpServletRequest req) {
        try {
            Object obj = type.getDeclaredConstructor().newInstance();
            fillObject(obj, type.getSimpleName(), req);
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Bind failed: " + type.getName(), e);
        }
    }

    // Employe[0].nom, Employe[1].nom, ...
    private static Object bindCollection(Parameter p, HttpServletRequest req) {
        try {
            ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
            Class<?> itemType = (Class<?>) pt.getActualTypeArguments()[0];
            String prefix = itemType.getSimpleName();

            Collection<Object> col = Set.class.isAssignableFrom(p.getType())
                    ? new LinkedHashSet<>()
                    : new ArrayList<>();

            int i = 0;
            while (hasFields(itemType, prefix, i, req)) {
                Object item = itemType.getDeclaredConstructor().newInstance();
                fillObject(item, prefix + "[" + i + "]", req);
                col.add(item);
                i++;
            }
            return col;
        } catch (Exception e) {
            throw new RuntimeException("Bind collection failed: " + p.getName(), e);
        }
    }

    // data[cle1]=val1, data[cle2]=val2, ...
    private static Object bindMap(Parameter p, HttpServletRequest req) {
        try {
            ParameterizedType pt = (ParameterizedType) p.getParameterizedType();
            Class<?> keyType = (Class<?>) pt.getActualTypeArguments()[0];
            Class<?> valType = (Class<?>) pt.getActualTypeArguments()[1];

            Map<Object, Object> map = new HashMap<>();
            String prefix = p.getName(); // necessite -parameters

            Enumeration<String> names = req.getParameterNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                if (!name.startsWith(prefix + "[") || !name.endsWith("]"))
                    continue;

                String key = name.substring(prefix.length() + 1, name.length() - 1);
                String val = req.getParameter(name);
                if (val == null || val.isEmpty())
                    continue;

                map.put(convertSimpleValue(key, keyType), convertSimpleValue(val, valType));
            }
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Bind map failed: " + p.getName(), e);
        }
    }

    // Remplit un objet via ses setters
    private static void fillObject(Object obj, String prefix, HttpServletRequest req) {
        for (Method setter : obj.getClass().getMethods()) {
            String name = setter.getName();
            if (!name.startsWith("set") || setter.getParameterCount() != 1)
                continue;

            String field = name.substring(3, 4).toLowerCase() + name.substring(4);
            String val = req.getParameter(prefix + "." + field);
            if (val == null || val.isEmpty())
                continue;

            try {
                setter.invoke(obj, convertSimpleValue(val, setter.getParameterTypes()[0]));
            } catch (Exception e) {
                // ignorer
            }
        }
    }

    // Verifie s'il existe au moins un champ pour cet index
    private static boolean hasFields(Class<?> type, String prefix, int i, HttpServletRequest req) {
        for (Method setter : type.getMethods()) {
            String name = setter.getName();
            if (!name.startsWith("set") || setter.getParameterCount() != 1)
                continue;

            String field = name.substring(3, 4).toLowerCase() + name.substring(4);
            if (req.getParameter(prefix + "[" + i + "]." + field) != null)
                return true;
        }
        return false;
    }

    // Un POJO = ni primitif, ni String/Number/Boolean, ni Date, ni Servlet
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

    // Convertit une String vers un type simple
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
                return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value);
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
            throw new RuntimeException("Conversion failed: '" + value + "' -> " + type.getSimpleName(), e);
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