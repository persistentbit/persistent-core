package com.persistbit.core.collections;

import com.persistentbit.core.collections.*;
import org.testng.annotations.Test;

import java.util.*;
import java.util.logging.Logger;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 13:35
 */
public class TestMaps {
    static private final Logger log = Logger.getLogger(TestMaps.class.getName());
    @Test
    public  void testPMap(){
        doAddRemove(PMap.empty());
    }
    @Test
    public  void testPOrderedMap(){
        doAddRemove(POrderedMap.empty());
    }
    private void doAddRemove(IPMap<Integer,String> empty){

        LinkedHashMap<Integer,String> refmap = new LinkedHashMap<>();
        IPMap<Integer,String> pmap = empty;
        Random r = new Random(System.currentTimeMillis());
        int count = 100000;
        log.info("Adding " + count + " elements to a PMap and a java Map");
        for(int t=0; t<count;t++){
            int key = r.nextInt();
            String val = ""  +r.nextGaussian();
            refmap.put(key,val);
            pmap = pmap.put(key,val);
        }
        log.info("Adding " + count + " elements to a PMap and a java Map...done");
        refmap.put(null,"1234");
        pmap = pmap.put(null,"1234");
        Set<Integer> refKeys = refmap.keySet();
        PStream<Integer> pstreamKeys = pmap.keys();
        //System.out.println(pstreamKeys);
        PSet<Integer> psetKeys = pstreamKeys.pset();
        //System.out.println(psetKeys);
        Set<Integer> pkeys = psetKeys.toSet();
        assert refmap.size() == pmap.size();

        if(pmap instanceof POrderedMap){
            //Lets check if the order is ok...
            Iterator<Integer> refIter = refmap.keySet().iterator();
            Iterator<Integer> pIter = pmap.keys().iterator();
            while(refIter.hasNext() && pIter.hasNext()){
                assert Objects.equals(refIter.next(),pIter.next());

            }
            assert refIter.hasNext() == pIter.hasNext();
        }

        if(refKeys.equals(pkeys) == false){
            throw new RuntimeException();
        }

        for(Map.Entry<Integer,String> entry : refmap.entrySet()){
            if(pmap.get(entry.getKey()).equals(entry.getValue()) == false){
                throw new RuntimeException(entry.toString());
            }
        }
        for(Map.Entry<Integer,String> entry : pmap.map().entrySet()){
            if(pmap.get(entry.getKey()).equals(entry.getValue()) == false){
                throw new RuntimeException(entry.toString());
            }
        }


    }
}
