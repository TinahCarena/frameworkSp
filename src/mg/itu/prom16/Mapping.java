package mg.itu.prom16;

import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import utils.UtilServlet;

public class Mapping {
    String className;
    String methodName;

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

    public Mapping(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "Mapping{className='" + className + "', methodName='" + methodName + "'}";
    }

    public Object invoke(HttpServletRequest request , Class<?> clazz , Method method ) throws ServletException {
        try {
            Object obj = clazz.getDeclaredConstructor().newInstance();
            Map<String, String> params = UtilServlet.extractParameter(request);
            Object[] args = UtilServlet.getMethodArguments(method, params);

            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new ServletException("Error invoking method", e);
        }
    }
}
