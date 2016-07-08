package com.persistentbit.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * User: petermuys
 * Date: 7/07/16
 * Time: 18:06
 */
public class PListList<T> implements List<T> {

    private PList<T> master;

    public PListList(PList<T> master){
        this.master = master;
    }

    @Override
    public int size() {
        return master.size();
    }

    @Override
    public boolean isEmpty() {
        return master.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return master.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return master.iterator();
    }

    @Override
    public Object[] toArray() {
        return master.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return master.toArray(a);
    }

    private void changeError(){
        throw new UnsupportedOperationException("Immutable List (PList pretending to be an immutable List)");
    }

    @Override
    public boolean add(T t) {
        changeError();
        return false;
    }

    @Override
    public boolean remove(Object o) {
        changeError();
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        changeError();
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        changeError();
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        changeError();
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        changeError();
        return false;
    }

    @Override
    public void clear() {
        changeError();

    }

    @Override
    public T get(int index) {
        return master.get(index);
    }

    @Override
    public T set(int index, T element) {
        changeError();
        return null;
    }

    @Override
    public void add(int index, T element) {
        changeError();
    }

    @Override
    public T remove(int index) {
        changeError();
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return master.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return master.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return master.listIterator(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return master.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return master.subList(fromIndex,toIndex).list();
    }

    @Override
    public String toString() {
        return master.toString();
    }
}