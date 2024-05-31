package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import utils.Utilitaire;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import controller.AnnotationController;
import controller.AnnotationGet;

public class FrontController extends HttpServlet{
   HashMap<String, Mapping> urlMappings;
  

   public void init() {

      try {
         String packageName = this.getInitParameter("source-package");
         Vector<String> controllers = Utilitaire.getListControllers(packageName, AnnotationController.class);

         HashMap<String, Mapping> temp = new HashMap<String, Mapping>();

         for (String controller : controllers) {
             Class<?> clazz = Class.forName(controller);
             List<Method> classMethods = Utilitaire.getClassMethodsWithAnnotation(clazz, AnnotationGet.class);
             for (Method method : classMethods) {
                 String annotationValue = method.getAnnotation(AnnotationGet.class).value();
                 temp.put(annotationValue, new Mapping(controller, method.getName()));
             }
         }
      } catch (Exception e) {
         
      }
      
     
   }

   protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ClassNotFoundException {
      PrintWriter out = resp.getWriter();
      try {
         String url = req.getRequestURI().substring(req.getContextPath().length());
         Mapping mapping = urlMappings.get(url);

         if (mapping != null) {
             Class<?> clazz = Class.forName(mapping.getClassName());
             Method method = clazz.getMethod(mapping.getMethodName());

             String result = method.invoke(clazz.getConstructor().newInstance()).toString();

             out.println(result);
         } else {
             out.println("Url not found");
         }

     } catch (Exception e) {
         e.printStackTrace(out);
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
