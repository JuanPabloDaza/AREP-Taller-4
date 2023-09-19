package edu.escuelaing.arep.ASE.app;

import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.Test;

public class APIConnectionTest {
    
    @Test
    public void CorrectInfoRequest() throws IOException{
        String info = APIConnection.getMovieInfo("Spider-man");
        assertNotEquals("FAILED", info);
    }
}
