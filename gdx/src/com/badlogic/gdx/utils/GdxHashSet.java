package com.badlogic.gdx.utils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

/**
 Java Collections to libGDX collections bridge.

 @author vaxquis
 @param <E>
 */
public class GdxHashSet<E> implements Set<E> {
    private final ObjectSet<E> objectSet;

    public GdxHashSet() {
        objectSet = new ObjectSet<E>();
    }

    public GdxHashSet( int initialCapacity ) {
        objectSet = new ObjectSet<E>( initialCapacity );
    }

    public GdxHashSet( int initialCapacity, float loadFactor ) {
        objectSet = new ObjectSet<E>( initialCapacity, loadFactor );
    }

    public GdxHashSet( ObjectSet<E> objectSet ) {
        this.objectSet = objectSet;
    }

    public ObjectSet<E> getObjectSet() {
        return objectSet;
    }

    @Override
    public int size() {
        return objectSet.size;
    }

    @Override
    public boolean isEmpty() {
        return objectSet.size == 0;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean contains( Object o ) {
        return objectSet.contains( (E) o );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean add( E e ) {
        return objectSet.add( e );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean remove( Object o ) {
        return objectSet.remove( (E) o );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean containsAll( Collection<?> c ) {
        for( Object o : c ) {
            if ( !contains( (E) o ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll( Collection<? extends E> c ) {
        boolean changed = false;
        for( E e : c ) {
            if ( add( e ) ) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        boolean modified = false;
        Iterator<E> e = iterator();
        while( e.hasNext() ) {
            if ( !c.contains( e.next() ) ) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean removeAll( Collection<?> c ) {
        boolean changed = false;
        for( Object o : c ) {
            if ( remove( (E) o ) ) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        objectSet.clear();
    }

    @Override
    public Iterator<E> iterator() {
        return objectSet.iterator();
    }

    @Override
    public Object[] toArray() {
        int size = objectSet.size;
        Object[] arr = new Object[size];
        Iterator<E> it = objectSet.iterator();
        for( int i = 0; i < size; i++ ) {
            arr[i] = it.next();
        }
        return arr;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T[] toArray( T[] a ) {
        int size = objectSet.size;
        if ( a.length < size ) {
            a = (T[]) Array.newInstance( a.getClass(), size );
        }
        Iterator<E> it = objectSet.iterator();
        for( int i = 0; i < size; i++ ) {
            a[i] = (T) it.next();
        }
        if ( a.length > size ) { // as in Java Collection classes
            a[size] = null;
        }
        return a;
    }

    @Override
    public void forEach( Consumer<? super E> arg0 ) {
        objectSet.forEach( arg0 );
    }

    @Override
    public Spliterator<E> spliterator() {
        return objectSet.spliterator();
    }

    @Override
    public String toString() {
        return objectSet.toString();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof GdxHashSet ) {
            obj = ( (GdxHashSet) obj ).objectSet;
        }
        return objectSet.equals( obj );
    }

    @Override
    public int hashCode() {
        return objectSet.hashCode();
    }

}
