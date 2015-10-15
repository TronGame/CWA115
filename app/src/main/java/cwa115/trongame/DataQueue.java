package cwa115.trongame;

import android.support.annotation.NonNull;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Bram on 15-10-2015.
 */
public class DataQueue<E> implements Queue<E> {

    private int mSize;
    private AbstractQueue<E> mQueue;

    public DataQueue(int size){
        this.mSize = size;
        this.mQueue = new ArrayBlockingQueue<>(size);
    }

    @Override
    public boolean add(E e) {
        return offer(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return this.mQueue.addAll(collection);
    }

    @Override
    public void clear() {
        this.mQueue.clear();
    }

    @Override
    public boolean contains(Object object) {
        return this.mQueue.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.mQueue.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return this.mQueue.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return this.mQueue.iterator();
    }

    @Override
    public boolean remove(Object object) {
        return this.mQueue.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.mQueue.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.mQueue.retainAll(collection);
    }

    @Override
    public int size() {
        return this.mSize;// Or this.mQueue.size() ??
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return this.mQueue.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        return this.mQueue.toArray(array);
    }

    @Override
    public boolean offer(E o) {
        if(this.size()>=this.mSize)
            this.poll();// Remove head element before adding new
        return this.mQueue.offer(o);
    }

    @Override
    public E remove() {
        return this.mQueue.remove();
    }

    @Override
    public E poll() {
        return this.mQueue.poll();
    }

    @Override
    public E element() {
        return this.mQueue.element();
    }

    @Override
    public E peek() {
        return this.mQueue.peek();
    }
}
