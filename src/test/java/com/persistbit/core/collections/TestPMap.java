package com.persistbit.core.collections;

import com.persistentbit.core.Tuple2;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.collections.PStream;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 13:35
 */
public class TestPMap {
    @Test
    void testAddRemove(){
        Map<Integer,String> refmap = new HashMap<>();
        PMap<Integer,String> pmap = new PMap<>();
        Random r = new Random(System.currentTimeMillis());

        for(int t=0; t<100000;t++){
            int key = r.nextInt();
            String val = ""  +r.nextGaussian();
            refmap.put(key,val);
            pmap = pmap.put(key,val);
        }
        refmap.put(null,"1234");
        pmap = pmap.put(null,"1234");
        Set<Integer> refKeys = refmap.keySet();
        PStream<Integer> pstreamKeys = pmap.keys();
        //System.out.println(pstreamKeys);
        PSet<Integer> psetKeys = pstreamKeys.pset();
        //System.out.println(psetKeys);
        Set<Integer> pkeys = psetKeys.toSet();
        assert refmap.size() == pmap.size();


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
