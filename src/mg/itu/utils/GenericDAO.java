package mg.itu.utils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GenericDAO<T> {

    public List<T> findAll(Class<T> clazz, String tableName) throws Exception {
        List<T> results = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object value = rs.getObject(fieldName);
                    field.set(instance, value);
                }

                results.add(instance);
            }
        }

        return results;
    }
}