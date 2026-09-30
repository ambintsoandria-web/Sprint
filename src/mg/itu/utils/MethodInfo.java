package mg.itu.utils;

public class MethodInfo {
    public String className;
    public String methodName;
    public boolean isJson;

    public MethodInfo(String className, String methodName, boolean isJson) {
        this.className = className;
        this.methodName = methodName;
        this.isJson = isJson;
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