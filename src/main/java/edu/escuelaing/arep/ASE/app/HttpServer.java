package edu.escuelaing.arep.ASE.app;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.escuelaing.arep.ASE.app.Annotation.RequestMapping;


public class HttpServer {
    private static Cache cache;
    private static Map<String, ServiciosStr> servicios = new HashMap<>();
    public static HttpServer _instance = new HttpServer();
    public static List<File> components = new ArrayList<File>();

    public static HttpServer getInstance(){
        return _instance;
    }

    public static void start(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        cache = new Cache(20); //Aqui se define el tamano del cache que se quiera usar.
        initilizeServices();
        initilizePOJO();
        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean firstLine = true;
            String uriString = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if (firstLine){
                    firstLine = false;
                    // POST /hellopost
                    uriString = inputLine.split(" ")[1];
                    System.out.println("URI: " + uriString);
                }
                if (!in.ready()) {
                    break;
                }
            }
            if (uriString.startsWith("/moviesearch")){
                outputLine = getMovie(uriString);
            }else{
                outputLine = getIndexPage(uriString);
            }
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
    
    public static String getMovie(String uri) throws IOException{
        String response = "";
        String titulo = uri.split("=")[1];
        String info = cache.get(titulo);
        if(info == "NF"){ //Si no esta dentro del cache entonces es necesario que haga la peticion de la informacion a la API externa.
            info = APIConnection.getMovieInfo(titulo);
            response = constructResponse(info);
            cache.put(titulo, response);
            System.out.println("Not Found in cache");
        }else{  //Si esta dentro del cache entonces retorna el valor que ya esta almacenado.
            response = info;
            System.out.println("Found in cache");
        }
        return response;
    }

    public static String constructResponse(String info){ //Para construir este metodo recibi ayuda de Juan Sebastian Rodruiguez Peña
        HashMap<String, String> separatedInfo = new HashMap<String, String>();
        JSONArray traducedInfo = new JSONArray(info);

        for(int i = 0; i < traducedInfo.length(); i++){
            // Se contruye un HashMap para luego poder mostrar la informacion solicitada de la pelicula. 
            JSONObject row = traducedInfo.getJSONObject(i);
            for(String key : row.keySet()){
                separatedInfo.put(key.toString(), row.get(key).toString());
            }
        }
        //Se construye el formato para dar respuesata a la peticion:
        String response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + "<table border=\" 1 \"> \n" 
                    + "<tr> \n";
        for (String key : separatedInfo.keySet()){
            //Se mueve por cada llave del HashMap para poner la informacion en una tabla:
            String movieInfo = separatedInfo.get(key);
            response += "<tr> \n"
                     + "<td>" + key + "</td> \n"
                     + "<td>" + movieInfo + "</td> \n"
                     + "<tr> \n";
        }
        response += "</table>";
        return response;
    }

    public static String getIndexPage(String uriString) {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\n");
        response.append("Content-Type: text/html\r\n");
        response.append("\r\n");
        String path;
        boolean service = false;
        if(uriString.equals("/") || uriString.equals("/favicon.ico")){
            path = "resources/index.html";
        }else{
            path = "";
            try {
                if(uriString.contains("?=")){
                    path = "resources" + uriString;
                    path = path.split("resources")[1];
                    String serviceName = path.split("\\?=")[0];
                    String serviceValue = path.split("\\?=")[1];
                    response.append(findService(serviceName, serviceValue));
                    service = true;
                }else{
                    try {
                        response.append(findService(uriString, ""));
                        service = true;
                    } catch (Exception e) {
                    }
                    
                }
            }catch(Exception e){
                System.out.println(e);
            }
        }
        if(!service){
            if(path.equals("")){
                path = "resources/" + uriString;
            }
            try(BufferedReader reader = new BufferedReader(new FileReader(path))){
                String line;
                while((line = reader.readLine()) != null){
                    if(line.contains("<img")){
                        line = line.trim();
                        String imagePath = line.split(" ")[1];
                        imagePath = imagePath.split("=")[1];
                        imagePath = imagePath.substring(1, imagePath.length()-1);
                        System.out.println(imagePath);
                        System.out.println(imagePath.split("\\.")[0]);
                        System.out.println(imagePath.split("\\.")[1]);

                        response.append("<img src=\"data:image/" + imagePath.split("\\.")[1] + ";base64,").append(getImageBits(imagePath)).append("\"/>\n");
                    }else{
                        response.append(line).append("\n");
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return response.toString(); 
    }

    public static String getImageBits(String imagePath){
        try{
            byte[] imageBytes = Files.readAllBytes(Paths.get("resources/" + imagePath));
            String image = Base64.getEncoder().encodeToString(imageBytes);
            return image;
        }catch(IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static void registrar(String llave, ServiciosStr servicio){
        servicios.put(llave, servicio);
    }

    public static ServiciosStr buscar(String llave){
        return servicios.get(llave);
    }

    public static void initilizeServices(){
        registrar("/hello", str -> "Hello " + str);
        registrar("/cuadrado", str -> str + " al cuadrado es: " + Math.pow(Integer.parseInt(str), 2));
        registrar("/coseno", new ServiciosStr() {
            @Override
            public String handle(String str){
                return "" + Math.cos(Double.parseDouble(str));
            }
        });
        registrar("/seno", str -> {
            Double var = Double.parseDouble(str.split("val=")[1]);
            return "" + Math.sin(var);
        });
    }

    public static void initilizePOJO(){
        File directory = new File("src/main/java/edu/escuelaing/arep/ASE/app/POJO");
        BuscadorComponentes buscador = new BuscadorComponentes();
        buscador.buscarArchivosConAnotacion(directory, "@Component", components);
        for(File file : components){
            try {
                String className = "edu.escuelaing.arep.ASE.app.POJO." + file.getName().split("\\.")[0];
                Class c = Class.forName(className);;
                Method[] methods = c.getDeclaredMethods();
                for(Method method : methods){
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        if(annotation != null){
                            String path = annotation.value();
                            Object instance = c.getDeclaredConstructor().newInstance();
                            String result = (String) method.invoke(instance);
                            registrar(path, new ServiciosStr() {
                                @Override
                                public String handle(String str){
                                    try {
                                        return "Servicio " + path + ": " + result;
                                    } catch (Exception e) {
                                        return "Error al registrar el metodo: " + path;
                                    }
                                    
                                }
                            });
                            System.out.println("Metodo Registrado");
                        }
                    }
                }
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Metodo no Registrado");
            }
        }
    }
    

    public static String findService(String serviceName, String serviceValue){
        return servicios.get(serviceName).handle(serviceValue);
    }

    public static String getIndexResponse(){
        String response = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<head>"
                        + "<title>Form Example</title>"
                        + "<meta charset=\"UTF-8\">"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                        + "</head>"
                        + "<body>"
                        + "<h1>Buscador de Peliculas</h1>"
                        + "<form action=\"/moviesearch\">"
                        + "<label for=\"postmovie\">Titulo:</label><br>"
                        + "<input type=\"text\" id=\"postmovie\" name=\"Titulo\"><br><br>"
                        + "<input type=\"button\" value=\"Submit\" onclick=\"loadPostMsg(postmovie)\">"
                        + "</form>"
        
                        + "<div id=\"postrespmsg\"></div>"
        
                        + "<script>"
                        + "function loadPostMsg(name){"
                        + "let url = \"/moviesearch?name=\" + name.value;"

                        + "fetch (url, {method: 'POST'})"
                        + ".then(x => x.text())"
                        + ".then(y => document.getElementById(\"postrespmsg\").innerHTML = y);"
                        + "}"
                        + "</script>"
                        + "</body>"
                        + "</html>";
        return response;
    }
}
