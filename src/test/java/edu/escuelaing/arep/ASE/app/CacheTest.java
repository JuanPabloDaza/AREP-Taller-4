package edu.escuelaing.arep.ASE.app;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CacheTest {
    private Cache cache = new Cache(3);

    @Test
    public void CacheCorrectInfoSave(){
        cache.put("1", "Hola");
        cache.put("2", "Buenos Dias");
        cache.put("3","Buenas tardes");
        String value = cache.get("1");
        assertEquals("Hola", value);
    }
    @Test
    public void CacheCorrectRemovement(){
        cache.put("1", "Hola");
        cache.put("2", "Buenos Dias");
        cache.put("3","Buenas tardes");
        cache.put("4", "Buenas Noches"); //Se sobrepasa el tamano del cache por lo tanto el dato menos reciente debe ser eliminado.
        String value = cache.get("1");
        assertEquals("NF", value); //La informacion ha sido borrada y por lo tanto no se encuentra (NF).
    }
}
