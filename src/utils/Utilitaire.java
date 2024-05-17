package utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.util.Vector;


public class Utilitaire {

    public String modifClassPath(String classPath) {
        classPath = classPath.substring(1);
        classPath = classPath.replace("%20", " ");
        return classPath;
    }

    public Vector<String> getListControllers(String packageName, Class<? extends Annotation>  annotation) throws MalformedURLException, ClassNotFoundException {
        Vector<String> controllers = new Vector<String>();
        String path = packageName.replace(".", "/");
        try {
            
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        java.net.URL source = classLoader.getResource(packageName);

        String realPath = this.modifClassPath(source.getFile());

        File classPathDirectory = new File(realPath);
  
        if(classPathDirectory.isDirectory()) {
            packageName = packageName.replace("/", ".");
            for(String fileName: classPathDirectory.list()) {
                fileName = fileName.substring(0, fileName.length()-6);
                String className = packageName +"."+ fileName;
                Class<?> classes = Class.forName(className);
                if (classes.isAnnotationPresent(annotation)) {
                    controllers.add(className);
                } 
            }
        }
        
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return controllers;
    }
}
