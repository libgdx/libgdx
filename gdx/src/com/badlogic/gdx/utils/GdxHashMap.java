package com.badlogic.gdx.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 Java Collections to libGDX collections bridge.

 @author vaxquis
 @param <K>
 @param <V>
 */
public class GdxHashMap<K, V> implements Map<K, V> {
    /**
     disables keySet(), values() & entrySet(), as their implementation is currently not standard compliant.
     */
    public static boolean disableBrokenImplementation = true;

    private final ObjectMap<K, V> objectMap;

    public GdxHashMap() {
        objectMap = new ObjectMap<K, V>();
    }

    public GdxHashMap( int initialCapacity ) {
        objectMap = new ObjectMap<K, V>( initialCapacity );
    }

    public GdxHashMap( int initialCapacity, float loadFactor ) {
        objectMap = new ObjectMap<K, V>( initialCapacity, loadFactor );
    }

    public GdxHashMap( ObjectMap<K, V> objectMap ) {
        this.objectMap = objectMap;
    }

    public ObjectMap<K, V> getObjectMap() {
        return objectMap;
    }

    @Override
    public int size() {
        return objectMap.size;
    }

    @Override
    public boolean isEmpty() {
        return objectMap.size == 0;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean containsKey( Object key ) {
        return objectMap.containsKey( (K) key );
    }

    @Override
    public boolean containsValue( Object value ) {
        return objectMap.containsValue( value, false );
    }

    public boolean containsValue( Object value, boolean identity ) {
        return objectMap.containsValue( value, identity );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public V get( Object key ) {
        return objectMap.get( (K) key );
    }

    @Override
    public V put( K key, V value ) {
        return objectMap.put( key, value );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public V remove( Object key ) {
        return objectMap.remove( (K) key );
    }

    @Override
    public void putAll( Map<? extends K, ? extends V> m ) {
        if ( m instanceof ObjectMap ) {
            objectMap.putAll( objectMap );
        } else {
            for( Entry<? extends K, ? extends V> entry : m.entrySet() ) {
                objectMap.put( entry.getKey(), entry.getValue() );
            }
        }
    }

    @Override
    public void clear() {
        objectMap.clear();
    }

    private void checkBrokenImplementation( String replacementMethod ) {
        if ( disableBrokenImplementation ) {
            throw new UnsupportedOperationException( "please use " + replacementMethod
                    + " instead (set GdxHashMap.disableBrokenImplementation to false to disable throwing this exception)" );
        }

    }

    /**
     Note: this implementation is not fulfilling supertype contract; namely, it returns a copy, not a set view
     of the keys in this map.
     <p />
     Use keys() to iterate over them directly with read-write access to this Map.

     @return java.util.Set-type copy of the objectMap.entries()
     @throws UnsupportedOperationException if disableBrokenImplementation is set
     */
    @Override
    public Set<K> keySet() {
        checkBrokenImplementation( "keys()" );
        Set<K> retSet = new GdxHashSet<K>();
        for( K k : objectMap.keys() ) {
            retSet.add( k );
        }
        return retSet;
    }

    public ObjectMap.Keys<K> keys() {
        return objectMap.keys();
    }

    /**
     Note: this implementation is not fulfilling supertype contract; namely, it returns a copy, not a collection view
     of the values in this map.
     <p />
     Use gdxValues() to iterate over them directly with read-write access to this Map.

     @return java.util.ArrayList-type copy of the objectMap.entries()
     @throws UnsupportedOperationException if disableBrokenImplementation is set
     */
    @Override
    public Collection<V> values() {
        checkBrokenImplementation( "gdxValues()" );
        GdxArrayList<V> gal = new GdxArrayList<V>();
        for( V v : objectMap.values() ) {
            gal.add( v );
        }
        return gal;
    }

    public ObjectMap.Values<V> gdxValues() {
        return objectMap.values();
    }

    public class EntryGHM implements Entry<K, V> {
        private final K key;
        private V value;

        public EntryGHM( K key, V value ) {
            this.key = key;
            this.value = value;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V setValue( V value ) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

    }

    /**
     Note: this implementation is not fulfilling supertype contract; namely, it returns a copy, not a set view of the entries in this map.
     <p />
     Use entries() to iterate over them directly with read-write access to this Map.

     @return java.util.Set-type copy of the objectMap.entries()
     @throws UnsupportedOperationException if disableBrokenImplementation is set
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        checkBrokenImplementation( "entries()" );
        Set<Entry<K, V>> retSet = new GdxHashSet<Entry<K, V>>();
        for( com.badlogic.gdx.utils.ObjectMap.Entry<K, V> entry : objectMap.entries() ) {
            retSet.add( new EntryGHM( entry.key, entry.value ) );
        }
        return retSet;
    }

    public ObjectMap.Entries<K, V> entries() {
        return objectMap.entries();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public V getOrDefault( Object key, V defaultValue ) {
        return objectMap.get( (K) key, defaultValue );
    }

    @Override
    public String toString() {
        return objectMap.toString();
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof GdxHashMap ) {
            obj = ( (GdxHashMap) obj ).objectMap;
        }
        return objectMap.equals( obj );
    }

    @Override
    public int hashCode() {
        return objectMap.hashCode();
    }

    /*
     @Override
     public void forEach( BiConsumer<? super K, ? super V> action ) {
     objectMap.forEach( action );
     }
     */
}
