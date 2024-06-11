package mg.itu.prom16;

import utils.Utilitaire;
import java.io.IOException;
import java.io.PrintWriter;

import utils.ModelView;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import controller.AnnotationController;
import exception.InvalidControllerProviderException;
import exception.InvalidReturnTypeException;
import exception.UrlNotFoundException;

public class FrontController extends HttpServlet{
   
    HashMap<String, Mapping> urlMappings;

    public HashMap<String, Mapping> getUrlMappings() {
        return urlMappings;
    }

    public void setUrlMappings(HashMap<String, Mapping> urlMappings) {
        this.urlMappings = urlMappings;
    }
  

   public void init() throws ServletException {

      try {
         String packageName = this.getInitParameter("source-package");
        
         if (packageName == null || packageName.isEmpty()) {
            throw new InvalidControllerProviderException("Invalid controller provider");
         }
         Vector<String> controllers = Utilitaire.getListControllers(packageName, AnnotationController.class);
         HashMap<String, Mapping> temp =  Utilitaire.getMapping(controllers);
         setUrlMappings(temp);
      } catch (Exception e) {
         throw new ServletException(e);
      }
   
   }


    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ClassNotFoundException {
      PrintWriter out = resp.getWriter();
      try {
         String url = req.getRequestURI().substring(req.getContextPath().length());
         Mapping mapping = urlMappings.get(url);
        out.println(mapping);
         if (mapping != null) {
             Class<?> clazz = Class.forName(mapping.getClassName());
             Method method = clazz.getMethod(mapping.getMethodName());
             Object result = method.invoke(clazz.getConstructor().newInstance());
            if (result instanceof String) {
                out.println(result);
            }
            else if (result instanceof ModelView) {
                ModelView modelv = (ModelView)result;
                HashMap<String, Object> data = modelv.getData();
                for (String key : data.keySet()) {
                    req.setAttribute(key, data.get(key));
                }
                req.getRequestDispatcher(modelv.getUrl()).forward(req, resp);
            } 
            else {
                throw new InvalidReturnTypeException("Invalid return type");
            }
         } else {
            throw new UrlNotFoundException("Url not found");
         }

      } catch (UrlNotFoundException e) {
         resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
      }
      catch (Exception e) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
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
