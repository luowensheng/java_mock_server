```java

package com.company.controllers;

import com.company.annotations.*;


@Controller
public class SimpleController {

    @GetMapping(value="/index")
    public String index(){

        return "Hello Index";
    }

    @GetMapping(value="/home/users")
    public String home(@RequestParam String car){

        return "Hello Home and ? "+car;
    }
}


```


```java

public class MockServer {

    static String packageName = "com.company";

    public static void run(int port){
        System.out.println("Initializing server");
        String url = "http://localhost:"+port;
   
        var mappings = getAllMapping();
        for (var map : mappings.keySet()) {
            for (var relative_url : mappings.get(map).keySet()) {
                  
              var function = mappings.get(map).get(relative_url);
              System.out.println("[name:"+map+", path:"+url+ relative_url +", result:"+function.invoke()+"]"); 
            }
        }
        System.out.println("Running server at "+url);
    }

    private static HashMap<String, HashMap<String, RequestFunction>> getAllMapping(){
        
        var mapping =  new HashMap<String, HashMap<String, RequestFunction>>();

        var classes = findAllClassesUsingClassLoader(packageName+".controllers");

        for (int i =0; i<classes.size(); i++){

              var classObj = classes.get(i);

              var temp = InstanciateClassWithoutParameters(classObj);

              if (temp.isEmpty()) continue;

              var instance = temp.get();

              var annot = classObj.getAnnotation(Controller.class);

              mapping.put(annot.toString(), new HashMap<>());

              for (var el : classObj.getMethods()) {

                if (el.isAnnotationPresent(GetMapping.class)){
        
                  var annotation = el.getAnnotation(GetMapping.class);

                  var c = el.getParameterCount();
                  var ps = el.getParameters();
                  Object[] params = new Object[c];

                  for (int j = 0; j < c ; j++) {
                       var p = ps[i];
                       var item = p.getAnnotation(RequestParam.class);
                        params[i] = (item!=null)?item.item(): "null";
                  }
        
                  mapping.get(annot.toString()).put(annotation.value(), ()->{
                    var result = CallMethodWithParameters(el, instance, params);
                    if (result.isEmpty()) return "Error";
                    return result.get().toString();
                  });
                }
                
              }
        }
        
        System.out.println("Done loading!");
        return mapping;
    }

    public static Optional<Object> CallMethodWithoutParameters(Method method, Object instance){

      try {
          return  Optional.of(method.invoke(instance, (Object[])null));
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
          return Optional.empty();
      }         
    }

    public static Optional<Object> CallMethodWithParameters(Method method, Object instance, Object[] params){

      try {
          return  Optional.of(method.invoke(instance, params));
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
          return Optional.empty();
      }         
    }


    public static <T> Optional<Object> InstanciateClassWithoutParameters(Class<T> classObj){
      try {
         Object instance = classObj.getConstructor().newInstance();
         return Optional.of(instance);
        
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        System.out.println(classObj.getName());
        return Optional.empty();
      }
    }



    public static <T> List<Class<T>> findAllClassesUsingClassLoader(String packageName) {

      InputStream stream = ClassLoader.getSystemClassLoader()
        .getResourceAsStream(packageName.replaceAll("[.]", "/"));

      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

      return  reader.lines()
                   .filter(line ->line.endsWith(".class"))
                   .map(className -> {
                      String fullClassName = packageName + "." + className.replace(".class", ""); 
                      return getClassIfAvailable(fullClassName) ;
                   })
                   .filter(obj -> obj.isPresent())
                   .map(obj ->obj.get())
                   .filter(obj -> obj.isAnnotationPresent(Controller.class))
                   .collect(Collectors.toList())
                   ;
    }

    public static Optional<Class> getClassIfAvailable(String className){
         System.out.println(className);
          try {
            return Optional.of(Class.forName(className));
          } catch (ClassNotFoundException e) {
            return Optional.empty();
          }   
    }


}

```