package com.persistbit.core.collections;

import com.persistentbit.core.collections.*;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Peter Muys
 * @since 13/07/2016
 */
public class TestSets {
    @Test
    public  void testPSet(){
        doAddRemove(PSet.empty());
    }
    @Test
    public  void testPOrderedSet(){
        doAddRemove(POrderedSet.empty());
    }
    private void doAddRemove(IPSet<Integer> empty){
        LinkedHashSet<Integer> refmap = new LinkedHashSet<>();
        IPSet<Integer> pmap = empty;
        Random r = new Random(System.currentTimeMillis());

        for(int t=0; t<100000;t++){
            int key = r.nextInt();
            refmap.add(key);
            pmap = pmap.plus(key);
        }
        refmap.add(null);
        pmap = pmap.plus(null);
        Set<Integer> refKeys = refmap;
        PStream<Integer> pstreamKeys = pmap;
        //System.out.println(pstreamKeys);
        PSet<Integer> psetKeys = pstreamKeys.pset();
        //System.out.println(psetKeys);
        Set<Integer> pkeys = psetKeys.toSet();
        assert refmap.size() == pmap.size();

        if(pmap instanceof POrderedSet){
            //Lets check if the order is ok...
            Iterator<Integer> refIter = refmap.iterator();
            Iterator<Integer> pIter = pmap.iterator();
            while(refIter.hasNext() && pIter.hasNext()){
                assert Objects.equals(refIter.next(),pIter.next());

            }
            assert refIter.hasNext() == pIter.hasNext();
        }

        if(refKeys.equals(pkeys) == false){
            throw new RuntimeException();
        }

        for(Integer entry : refmap){
            if(pmap.contains(entry) == false){
                throw new RuntimeException(entry.toString());
            }
        }



    }
}
