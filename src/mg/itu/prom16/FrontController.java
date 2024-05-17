package mg.itu.prom16;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ModuleLayer.Controller;
import java.net.MalformedURLException;
import java.util.Vector;
import utils.Utilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import controller.AnnotationController;

public class FrontController extends HttpServlet{
   String packagePath;
   boolean isChecked;
   Vector<String> controllers;

   public void init() {
      try {
         controllers = new Utilitaire().getListControllers(this.getInitParameter("source-package"), AnnotationController.class);
         isChecked = true;
      } catch (Exception e) {
          throw new RuntimeException();
      }
   }

   protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, ClassNotFoundException {
      PrintWriter out = resp.getWriter();
      if (!isChecked) {
         init();
      } else{
         for(int i=0; i < controllers.size(); i++) {
            out.println(controllers.get(i));
         }
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
