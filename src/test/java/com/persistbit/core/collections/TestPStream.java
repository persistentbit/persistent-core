package com.persistbit.core.collections;

import com.persistentbit.core.collections.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 09:35
 */
public class TestPStream {
    static private final PStream<Integer> init = PStream.val(4,10,2,8,100,-2,0,1000);

    @Test
    public void testSequences() {
        PStream<Integer> s = PStream.sequence(10,(n)-> n-1);
        assert s.isInfinit();
        assert s.isEmpty() == false;
        Assert.assertEquals((Object)s.limit(10).plist(),PList.val(10,9,8,7,6,5,4,3,2,1));
        expectInfinite(() -> s.size());
        expectInfinite(() -> s.plist());
        expectInfinite(() -> s.pset());
        expectInfinite(() -> s.toArray());
        expectInfinite(() -> s.list());
        expectInfinite(() -> s.dropLast());
        expectInfinite(() -> s.fold(0,(a,b)-> a));
        expectInfinite(() -> s.groupBy(i-> i));
        expectInfinite(() -> s.join((a,b)-> a));
        expectInfinite(() -> s.llist());
        expectInfinite(() -> s.max());
        expectInfinite(() -> s.min());
        expectInfinite(() -> s.plus(1));
        expectInfinite(() -> s.plusAll(Collections.EMPTY_LIST));
        expectInfinite(() -> s.reversed());
        expectInfinite(() -> s.sorted());
        expectInfinite(() -> s.toArray());
        expectInfinite(() -> s.toArray(null));
        expectInfinite(() -> s.toList());
        expectInfinite(() -> s.toString(","));
        expectInfinite(() -> s.with(1,(a,b)-> 1));
        expectInfinite(() -> s.count((i) -> true));
    }

    @Test
    void testPList() {
        PList<Integer> pi = PList.forInt().plusAll(init);
        testStream(pi);
        testStream(pi.lazy());
    }
    @Test
    void testPSet() {
        PSet<Integer> pi = PSet.forInt().plusAll(init);
        testStream(pi);
        testStream(pi.lazy());
    }

    @Test
    void testLList() {
        LList<Integer> pi = LList.<Integer>empty().plusAll(init);
        testStream(pi);
        testStream(pi.lazy());
    }


    /**
     * Expect a stream with following elements: 4,10,2,8,100,-2,0,1000
     * @param s
     */
    void testStream(PStream<Integer> s){
        assert s.size() == 8;
        assert s.isEmpty() == false;
        assert s.clear().size() == 0;
        assert s.clear().isEmpty() == true;
        assert s.contains(1000);
        assert s.contains(4);
        assert s.contains(8);
        assert s.contains(2000) == false;
        assert s.containsAll(PStream.val(2,4,0));
        assert s.containsAll(PStream.val(2,4,0,9)) == false;
        assert s.min().get() == -2;
        assert s.max().get() == 1000;
        assert s.clear().min().isPresent() == false;
        assert s.clear().max().isPresent() == false;
        assert s.limit(0).plist().equals(s.clear().plist());
        expectException(() -> s.limit(-1),Exception.class);
        assert s.limit(100).size() == 8;
        assert s.limit(6).size() == 6;
        assert s.plusAll(PStream.val(3,9)).pset().equals(PStream.val(4,10,2,8,100,-2,0,1000,3,9).pset());
        assert s.plus(3).pset().equals(PStream.val(4,10,2,8,100,-2,0,1000,3).pset());
        assert s.plus(4).distinct().count(i -> i == 4) == 1;
        assert s.plus(5).plus(5).plus(5).distinct().count(i-> i==5) == 1;
        assert s.count(i -> i <=4) == 4;
        assert s.find(i -> i==3).isPresent() == false;
        assert s.find(i -> i==4).get() == 4;
        assert s.fold(10,(a,b)-> a+b) == 1132;
        assert s.clear().fold(11,(a,b) -> a+b) == 11;
        assert s.clear().plus(1).fold(11,(a,b) -> a+b) == 12;
        assert s.clear().plus(1).plus(8).fold(11,(a,b) -> a+b) == 20;
        assert s.sorted().head() == -2;
        assert s.clear().headOpt().isPresent() == false;
        assert s.sorted().tail().equals(PStream.val(0,2,4,8,10,100,1000));
        expectException(() -> s.clear().tail(),Exception.class);
        expectException(() -> s.clear().head(),Exception.class);


/*
        s.list();
        s.dropLast();
        s.filter();
        s.find();
        s.fold();
        s.groupBy();
        s.head();
        s.headOpt();
        s.isEmpty();
        s.isInfinit();
        s.join();
        s.limit();
        s.llist();
        s.map();
        s.max();
        s.min();
        s.plist();
        s.plus();
        s.plusAll();
        s.pset();
        s.reversed();
        s.sorted();
        s.stream();
        s.toArray();
        s.toString();
        s.with();
        s.zipWithIndex();
        s.zip();
*/



    }


    private void expectInfinite(Runnable r){
        expectException(r,InfinitePStreamException.class);
    }
    private void expectException(Runnable r,Class<? extends Exception> exceptionCls){
        try{
            r.run();
        }catch(Exception e){
            if(exceptionCls.isAssignableFrom(e.getClass())){
                return ;
            }
            throw e;
        }
        throw new RuntimeException("Expected" + exceptionCls.getSimpleName() + " exception");
    }


}
