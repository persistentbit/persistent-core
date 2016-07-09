package com.persistentbit.core.collections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 10:48
 */
public abstract class AbstractPSeq<T,IMPL extends PStream<T>> extends PStreamDirect<T,IMPL> implements PSeq<T> {
    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T v : this) {
            hashCode = 31 * hashCode + (v == null ? 0 : v.hashCode());
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj instanceof PSeq == false){
            return false;
        }
        PSeq p = (PSeq)obj;

        Iterator<T> i1 = iterator();
        Iterator<T> i2 = p.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            T o1 = i1.next();
            Object o2 = i2.next();
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(i1.hasNext() || i2.hasNext());
    }


}
