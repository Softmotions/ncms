package com.softmotions.commons.cont;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public abstract class AbstractIndexedCollection<K, V> implements Collection<V> {

    private final Collection<V> wrappedCollection;

    private final Map<K, V> index;

    protected AbstractIndexedCollection() {
        this.wrappedCollection = new ArrayList<>();
        this.index = new HashMap<>(wrappedCollection.size());
    }

    protected AbstractIndexedCollection(Collection<V> wrappedCollection) {
        this.wrappedCollection = wrappedCollection;
        this.index = new HashMap<>(wrappedCollection.size());
    }

    public Map<K, V> getIndex() {
        return index;
    }

    public int size() {
        return wrappedCollection.size();
    }

    public boolean isEmpty() {
        return wrappedCollection.isEmpty();
    }

    public boolean contains(Object o) {
        return wrappedCollection.contains(o);
    }

    public Iterator<V> iterator() {
        return wrappedCollection.iterator();
    }

    public Object[] toArray() {
        return wrappedCollection.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return wrappedCollection.toArray(a);
    }

    public boolean add(V v) {
        K k = getElementKey(v);
        if (k != null) {
            index.put(k, v);
        }
        return wrappedCollection.add(v);
    }

    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        V to = (V) o;
        K k = getElementKey(to);
        if (k != null) {
            index.remove(k);
        }
        return wrappedCollection.remove(to);
    }

    public boolean containsAll(Collection<?> c) {
        return wrappedCollection.containsAll(c);
    }

    public boolean addAll(Collection<? extends V> c) {
        for (V v : c) {
            K k = getElementKey(v);
            if (k != null) {
                index.put(k, v);
            }
        }
        return wrappedCollection.addAll(c);
    }

    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            K k = getElementKey((V) o);
            if (k != null) {
                index.remove(k);
            }
        }
        return wrappedCollection.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        boolean res = wrappedCollection.retainAll(c);
        index.clear();
        for (V v : wrappedCollection) {
            K k = getElementKey(v);
            if (k != null) {
                index.put(k, v);
            }
        }
        return res;
    }

    public void clear() {
        wrappedCollection.clear();
        index.clear();
    }

    public String toString() {
        return wrappedCollection.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractIndexedCollection that = (AbstractIndexedCollection) o;
        if (!wrappedCollection.equals(that.wrappedCollection)) return false;
        return true;
    }

    public int hashCode() {
        return wrappedCollection.hashCode();
    }

    protected abstract K getElementKey(V el);
}
