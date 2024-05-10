package mg.itu.prom16;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet{
   protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try {
         PrintWriter print = resp.getWriter();
         print.println("test kely");
      } catch (Exception e) {
         e.getMessage();
      }
      
   }
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      processRequest(req, resp);
   }

   
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      processRequest(req, resp);
   }
}
