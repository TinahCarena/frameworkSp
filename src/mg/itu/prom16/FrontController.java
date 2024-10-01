package mg.itu.prom16;

import utils.Personne;
import java.io.IOException;
import java.io.PrintWriter;
import utils.Function;
import utils.ModelView;
import utils.MySession;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import controller.AnnotationController;
import controller.AnnotationParamObject;
import controller.AnnotationReqParam;
import controller.AnnotationResponse;
import com.google.gson.Gson;

public class FrontController extends HttpServlet {
   
    private List<String> controllers;
    private HashMap<String, Mapping> map;

    @Override
public void init() throws ServletException {
    String packageToScan = this.getInitParameter("source-package");
    if (packageToScan == null || packageToScan.isEmpty()) {
        throw new ServletException("Le package à scanner est vide ou non défini.");
    }
    
    try {
        // Récupérer toutes les classes annotées
        this.controllers = new Function().getAllclazzsStringAnnotation(packageToScan, AnnotationController.class);
        // Vérifier si des contrôleurs ont été trouvés
        if (this.controllers == null || this.controllers.isEmpty()) {
            throw new ServletException("Aucun contrôleur annoté trouvé dans le package spécifié.");
        }

        // Scanner les méthodes des contrôleurs
        this.map = new Function().scanControllersMethods(this.controllers);
        if (this.map == null || this.map.isEmpty()) {
            throw new ServletException("Le mapping des contrôleurs est vide.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        throw new ServletException("Erreur lors de l'initialisation du FrontController : " + e.getMessage());
    }
}


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String path = new Function().getURIWithoutContextPath(request);

        // Vérification si path contient un paramètre GET après "?"
        if (path.contains("?")) {
            int index = path.indexOf("?");
            path = path.substring(0, index);
        }

        // Vérification si la map est initialisée avant d'y accéder
        if (map == null) {
            out.println("Erreur: le mapping des contrôleurs n'a pas été initialisé.");
            return;
        }

        // Traitement de la requête si la route existe dans le mapping
        if (map.containsKey(path)) {
            Mapping m = map.get(path);
            try {
                Class<?> clazz = Class.forName(m.getClassName());
                Method[] methods = clazz.getDeclaredMethods();
                Method targetMethod = null;

                for (Method method : methods) {
                    if (method.getName().equals(m.getMethodName())) {
                        targetMethod = method;
                        break;
                    }
                }

                if (targetMethod != null) {
                    Object[] params = Function.getParameterValue(request, targetMethod, AnnotationReqParam.class,
                            AnnotationParamObject.class);
                    Object controllerInstance = clazz.newInstance();

                    // Vérification et initialisation de MySession si nécessaire
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType().equals(MySession.class)) {
                            field.setAccessible(true);
                            field.set(controllerInstance, new MySession(request.getSession()));
                        }
                    }

                    Object result = targetMethod.invoke(controllerInstance, params);

                    // Gestion des annotations et des types de retour
                    if (targetMethod.isAnnotationPresent(AnnotationResponse.class)) {
                        if (result instanceof ModelView) {
                            response.setContentType("application/json");
                            ModelView modelView = (ModelView) result;
                            HashMap<String, Object> data = modelView.getData();
                            Gson gson = new Gson();
                            String jsonModel = gson.toJson(data);
                            out.println(jsonModel);
                        } else {
                            response.setContentType("application/json");
                            Gson gson = new Gson();
                            String jsonResult = gson.toJson(result);
                            out.println(jsonResult);
                        }
                    } else if (result instanceof String) {
                        out.println("Résultat de l'exécution de la méthode " + m.getMethodName() + " est " + result);
                    } else if (result instanceof ModelView) {
                        ModelView modelView = (ModelView) result;
                        String destinationUrl = modelView.getUrl();
                        HashMap<String, Object> data = modelView.getData();
                        for (String key : data.keySet()) {
                            request.setAttribute(key, data.get(key));
                        }
                        RequestDispatcher dispatcher = request.getRequestDispatcher(destinationUrl);
                        dispatcher.forward(request, response);
                    } else {
                        out.println("Le type de retour n'est ni un String ni un ModelView.");
                    }
                } else {
                    out.println("Méthode non trouvée : " + m.getMethodName());
                }
            } catch (Exception e) {
                out.println("Erreur lors de l'exécution de la méthode : " + e.getMessage());
            }
        } else {
            out.println("404 NOT FOUND");
        }
    }
}
