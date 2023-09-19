package edu.escuelaing.arep.ASE.app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class BuscadorComponentes {

    public static void buscarArchivosConAnotacion(File directorio, String anotacionBuscada, List<File> resultado) {
        if (directorio.isDirectory()) {
            File[] archivos = directorio.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    buscarArchivosConAnotacion(archivo, anotacionBuscada, resultado);
                }
            }
        } else if (directorio.isFile() && directorio.getName().endsWith(".java")) {
            try {
                List<String> lineas = Files.readAllLines(directorio.toPath());
                for (String linea : lineas) {
                    if (linea.contains(anotacionBuscada)) {
                        resultado.add(directorio);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
