package com.badlogic.gdx.utils;

import java.util.*;
import java.util.function.Consumer;
import com.badlogic.gdx.utils.Array;

/**
 Java Collections to libGDX collections bridge.
 <p />
 Also implements common push()/pop() stack methods.

 @author vaxquis
 @param <E>
 */
public class GdxArrayList<E> implements List<E>, RandomAccess, Queue<E> {
    private final Array<E> array;

    public GdxArrayList() {
        array = new Array<E>();
    }

    public GdxArrayList( Array<E> array ) {
        this.array = array;
    }

    public GdxArrayList( int capacity ) {
        array = new Array<E>( capacity );
    }

    public GdxArrayList( E[] array ) {
        this.array = new Array<E>( array );
    }

    public Array<E> getArray() {
        return array;
    }

    @Override
    public int size() {
        return array.size;
    }

    @Override
    public boolean isEmpty() {
        return array.size == 0;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean contains( Object o ) {
        return array.contains( (E) o, false );
    }

    public boolean contains( E t, boolean identity ) {
        return array.contains( t, identity );
    }

    @Override
    public Iterator<E> iterator() {
        return array.iterator();
    }

    @Override
    public Object[] toArray() {
        return array.toArray();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T[] toArray( T[] a ) {
        int size = array.size;
        if ( a.length < size ) {
            a = (T[]) java.lang.reflect.Array.newInstance( a.getClass(), size );
        }
        for( int i = 0; i < size; i++ ) {
            a[i] = (T) array.get( i );
        }
        if ( a.length > size ) { // as in Java Collection classes
            a[size] = null;
        }
        return a;
    }

    @Override
    public boolean add( E e ) {
        array.add( e );
        return true;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean remove( Object o ) {
        return array.removeValue( (E) o, false );
    }

    public void removeRange( int start, int end ) {
        array.removeRange( start, end );
    }

    public boolean removeValue( E e, boolean identity ) {
        return array.removeValue( e, identity );
    }

    public boolean removeAll( Array<? extends E> a, boolean identity ) {
        return array.removeAll( a, identity );
    }

    @Override
    public boolean addAll( int index, Collection<? extends E> c ) {
        for( E e : c ) {
            add( index, e );
            index++;
        }
        return !c.isEmpty();
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
        array.clear();
    }

    @Override
    public E get( int index ) {
        return array.get( index );
    }

    public E getFirst() {
        return array.first();
    }

    public E getRandom() {
        return array.peek();
    }

    public void reverse() {
        array.reverse();
    }

    @Override
    public E set( int index, E element ) {
        E e = array.get( index );
        array.set( index, element );
        return e;
    }

    @Override
    public void add( int index, E element ) {
        array.insert( index, element );
    }

    @Override
    public E remove( int index ) {
        return array.removeIndex( index );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public int indexOf( Object o ) {
        return array.indexOf( (E) o, false );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public int lastIndexOf( Object o ) {
        return array.lastIndexOf( (E) o, false );
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator( 0 );
    }

    @Override
    public ListIterator<E> listIterator( int index ) {
        throw new UnsupportedOperationException( "Not implemented. Use iterator() instead." );
    }

    @Override
    public List<E> subList( int fromIndex, int toIndex ) {
        throw new UnsupportedOperationException( "Not implemented." );
    }

    @Override
    public Spliterator<E> spliterator() {
        return array.spliterator();
    }

    @Override
    public void forEach( Consumer<? super E> arg0 ) {
        array.forEach( arg0 );
    }

    @Override
    public void sort( Comparator<? super E> c ) {
        array.sort( c );
    }

    public void sort() {
        array.sort();
    }

    @Override
    public boolean offer( E e ) {
        return add( e );
    }

    @Override
    public E remove() {
        if ( array.size == 0 ) {
            throw new NoSuchElementException();
        }
        return array.pop();
    }

    @Override
    public E poll() {
        if ( array.size == 0 ) {
            return null;
        }
        return array.pop();
    }

    @Override
    public E element() {
        if ( array.size == 0 ) {
            throw new NoSuchElementException();
        }
        return array.peek();
    }

    @Override
    public E peek() {
        if ( array.size == 0 ) {
            return null;
        }
        return array.peek();
    }

    public E push( E e ) {
        add( e );
        return e;
    }

    public E pop() {
        return array.pop();
    }

    @Override
    public String toString() {
        return array.toString();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof GdxArrayList ) {
            obj = ( (GdxArrayList) obj ).array;
        }
        return array.equals( obj );
    }

    @Override
    public int hashCode() {
        return array.hashCode();
    }
}
