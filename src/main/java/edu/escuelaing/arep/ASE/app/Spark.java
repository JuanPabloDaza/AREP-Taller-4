package edu.escuelaing.arep.ASE.app;

public class Spark {
    public static void main(String[] args) {
        try {
            HttpServer.getInstance();
            HttpServer.start(args);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
