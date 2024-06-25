package mg.itu.prom16;

import utils.Utilitaire;
import java.io.IOException;
import java.io.PrintWriter;

import utils.ModelView;
import utils.TypeConverter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import controller.AnnotationController;
import controller.AnnotationReqParam;
import exception.DuplicateUrlException;

import exception.InvalidReturnTypeException;

public class FrontController extends HttpServlet{
   
    private Utilitaire scanner;
    private HashMap<String, Mapping> ListService;

    @Override
    public void init() throws ServletException{
        try {
            scanner = new Utilitaire();
            String packagename = this.getInitParameter("source-package");
            ListService = scanner.getMapping(packagename, AnnotationController.class);
        } catch (DuplicateUrlException e) {
            log("DuplicateGetMappingException occurred: " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String url = scanner.conform_url(request.getRequestURL().toString());
        Mapping mapping = ListService.get(url);
        if (mapping == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "URL not mapped.");
            return;
        }

        try {
            Class<?> clazz = Class.forName(mapping.getClassName());
            // Controller instance:
            Object instance = clazz.getDeclaredConstructor().newInstance();
            // get matching method for url
            Method method = null;
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(mapping.getMethodName())) {
                    method = m;
                    break;
                }
            }

            // get All the parameters name from a form:
            Enumeration<String> parameterNames = request.getParameterNames();
            // Map to hold created objects
            Map<String, Object> objets = new HashMap<String, Object>();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                String[] parts = paramName.split("\\.");
                if (parts.length > 1) {
                    String objectName = parts[0];
                    Class<?> objetclazz = Class.forName(this.getInitParameter("model-package")+"." + objectName);
                    Object mydataholder;
                    if (!objets.containsKey(objectName)) {
                        mydataholder = objetclazz.getDeclaredConstructor().newInstance();
                        objets.put(objectName, mydataholder);
                    } else {
                        mydataholder = objets.get(objectName);
                    }
                    Method datasetter = null;
                    for (Method m : objetclazz.getDeclaredMethods()) {
                        if (m.getName().equals("set" + parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1))) {
                            datasetter = m;
                            break;
                        }
                    }
                    String paramValue = request.getParameter(paramName);
                    datasetter.invoke(mydataholder, TypeConverter.convertParameter(datasetter.getParameterTypes()[0], paramValue));
                } else {
                    String paramValue = request.getParameter(paramName);
                    objets.put(paramName, paramValue);
                }
            }

            // Prepare method arguments:
            List<Object> methodArgs = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                String paramName = parameter.isAnnotationPresent(AnnotationReqParam.class) ? parameter.getAnnotation(AnnotationReqParam.class).value() : parameter.getName();
                methodArgs.add(objets.get(paramName));
            }

            // Invoke the controller method
            Object result = method.invoke(instance, methodArgs.toArray());
            if (result instanceof String) {
                out.println(result);
            } else if (result instanceof ModelView) {
                ModelView mv = (ModelView) result;
                mv.getData().forEach(request::setAttribute);
                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
                dispatcher.forward(request, response);
            } else {
                throw new InvalidReturnTypeException("Return type not handled for: " + url);
            }
        } catch (Exception e) {
            out.println("<h3>Oops!</h3>");
            out.println("<p>An error occurred while processing the request.</p>");
            out.println(e);
        }
    }

  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       processRequest(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       processRequest(req, resp);
    }
}
