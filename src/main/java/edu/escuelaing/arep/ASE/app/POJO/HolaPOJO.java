package edu.escuelaing.arep.ASE.app.POJO;

import edu.escuelaing.arep.ASE.app.Annotation.Component;
import edu.escuelaing.arep.ASE.app.Annotation.RequestMapping;

@Component
public class HolaPOJO {

    public HolaPOJO(){
    }

    @RequestMapping("/HolaPOJO")
    public String holaPojo(){
        return "Hola, esta es la implementacion de POJO";
    }

    @RequestMapping("/Hello")
    public String hello(){
        return "Hola Juan Pablo";
    }

}
