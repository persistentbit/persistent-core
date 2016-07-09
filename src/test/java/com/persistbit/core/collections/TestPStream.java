package com.persistbit.core.collections;

import com.persistentbit.core.collections.InfinitePStreamException;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 09:35
 */
public class TestPStream {


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
