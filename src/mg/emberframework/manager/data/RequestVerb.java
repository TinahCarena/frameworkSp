package mg.emberframework.manager.data;

import java.lang.reflect.Method;

import mg.emberframework.annotation.request.AnnotationPost;

public class RequestVerb {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";
    public static final String PUT = "PUT";

    public static String getMethodVerb(Method method) {
        String verb = RequestVerb.GET;
        if (method.isAnnotationPresent(AnnotationPost.class)) {
            verb = RequestVerb.POST;
        }
        return verb;
    }
}