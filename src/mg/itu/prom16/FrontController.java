package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import utils.Utilitaire;
import java.lang.reflect.Method;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import controller.AnnotationController;
import controller.AnnotationGet;

public class FrontController extends HttpServlet{
   HashMap<String, Mapping> urlMappings;
   Vector<String> controllers;

   public void init() {
      urlMappings = new HashMap<>();
      try {
         controllers = new Utilitaire().getListControllers(this.getInitParameter("source-package"), AnnotationController.class);
      } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      try {
         for (String controller : controllers) {
            Class<?> clazz = Class.forName(controller);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AnnotationGet.class)) {
                    AnnotationGet getAnnotation = method.getAnnotation(AnnotationGet.class);
                    String url = getAnnotation.value();
                    urlMappings.put(url, new Mapping(controller, method.getName()));
                }
            }
        }
      } catch (Exception e) {
          throw new RuntimeException();
      }
   }

   protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ClassNotFoundException {
      PrintWriter out = resp.getWriter();
      String path = req.getRequestURI().substring(req.getContextPath().length());

      Mapping mapping = urlMappings.get(path);
      if (mapping != null) {
         out.println("URL: " + path);
         out.println("Mapping: " + mapping);
      } else{
         out.println("Pas de methode associe a ce chemin: " + path);
      }
        
    }
  
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
           processRequest(req, resp);
        } catch (ClassNotFoundException | ServletException | IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
           processRequest(req, resp);
        } catch (ClassNotFoundException | ServletException | IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
        }
    }
}
