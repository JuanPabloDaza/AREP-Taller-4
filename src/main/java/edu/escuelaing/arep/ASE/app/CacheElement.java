package edu.escuelaing.arep.ASE.app;

public class CacheElement {
    //Esta clase es para el funcionamiento del cache LRU (Least Recently Used):
    String key;
    String value;
    CacheElement next;
    CacheElement prev;

    public CacheElement(String key, String value){
        this.key = key;
        this.value = value;
    }
}
