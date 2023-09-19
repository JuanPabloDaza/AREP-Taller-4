package edu.escuelaing.arep.ASE.app;
import java.util.HashMap;

public class Cache {
    // Esta clase de cache utiliza el modo LRU (Least Recently Used)
    CacheElement head;
    CacheElement tail;
    HashMap<String, CacheElement> entrys = null;
    int size = 10;

    public Cache (int size){
        this.size = size;
        entrys = new HashMap<>();
    }

    public String get(String key){
        //Si la llave esta en entrys, entonces se mueve a la primera posicion, si no, retorna "NF" (Not Found).

        if(entrys.containsKey(key)){
            CacheElement element = entrys.get(key);
            removeElement(element);
            putOnTop(element);
            return element.value;
        }
        return "NF";
    }

    public void put(String key, String value){

        if(entrys.containsKey(key)){
            //Si la llave ya esta, entonces solo se actualiza la posicion.
            CacheElement element = entrys.get(key);
            element.value=value;
            removeElement(element);
            putOnTop(element);
        }else{
            //Si el cache ya esta lleno, entonces se elimina el elemento que este en la ultima posicion.
            if(entrys.size()>=size){
                entrys.remove(tail.key);
                removeElement(tail);
            }
            CacheElement element = new CacheElement(key, value);
            entrys.put(key, element);
            putOnTop(element);

        }
    }

    private void removeElement(CacheElement element){
        CacheElement prevCacheElement = element.prev;
        CacheElement nexCacheElement = element.next;

        if(prevCacheElement != null){
            prevCacheElement.next = element.next;
        }else{
            head = nexCacheElement;
        }

        if(nexCacheElement != null){
            nexCacheElement.prev = prevCacheElement;
        }else{
            tail = prevCacheElement;
        }
    }

    private void putOnTop(CacheElement element){
        element.next = head;
        element.prev = null;
        if(head != null){
            head.prev = element;
        }
        head = element;
        if(tail == null){
            tail = element;
        }
    }
}
