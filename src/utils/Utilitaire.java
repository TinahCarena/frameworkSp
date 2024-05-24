package utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Vector;

public class Utilitaire {

    public String modifClassPath(String classPath) {
        classPath = classPath.substring(1);
        classPath = classPath.replace("%20", " ");
        return classPath;
    }

    public Vector<String> getListControllers(String packageName, Class<? extends Annotation> annotation) throws ClassNotFoundException {
        Vector<String> controllers = new Vector<>();
        String path = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            throw new ClassNotFoundException("Package " + packageName + " not found");
        }

        String realPath = this.modifClassPath(resource.getFile());
        File classPathDirectory = new File(realPath);

        if (!classPathDirectory.exists() || !classPathDirectory.isDirectory()) {
            throw new ClassNotFoundException("Invalid directory: " + realPath);
        }

        for (String fileName : classPathDirectory.list()) {
            if (fileName.endsWith(".class")) {
                String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(annotation)) {
                    controllers.add(className);
                }
            }
        }

        return controllers;
    }
}
