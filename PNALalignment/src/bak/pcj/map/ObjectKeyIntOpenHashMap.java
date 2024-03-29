/*
 *  Primitive Collections for Java.
 *  Copyright (C) 2002, 2003  S&oslash;ren Bak
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package bak.pcj.map;

import bak.pcj.IntCollection;
import bak.pcj.AbstractIntCollection;
import bak.pcj.IntIterator;
import bak.pcj.hash.IntHashFunction;
import bak.pcj.hash.DefaultIntHashFunction;
import bak.pcj.util.Exceptions;
import java.util.Set;
import java.util.AbstractSet;
import java.util.Iterator;

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *  This class represents open addressing hash table based maps from
 *  object values to int values.
 *
 *  @see        ObjectKeyIntChainedHashMap
 *  @see        java.util.Map
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.2     21-08-2003 19:41
 *  @since      1.1
 */
public class ObjectKeyIntOpenHashMap extends AbstractObjectKeyIntMap implements ObjectKeyIntMap, Cloneable, Serializable {

    /** Constant indicating relative growth policy. */
    private static final int    GROWTH_POLICY_RELATIVE      = 0;

    /** Constant indicating absolute growth policy. */
    private static final int    GROWTH_POLICY_ABSOLUTE      = 1;

    /**
     *  The default growth policy of this map.
     *  @see    #GROWTH_POLICY_RELATIVE
     *  @see    #GROWTH_POLICY_ABSOLUTE
     */
    private static final int    DEFAULT_GROWTH_POLICY       = GROWTH_POLICY_RELATIVE;

    /** The default factor with which to increase the capacity of this map. */
    public static final double DEFAULT_GROWTH_FACTOR        = 1.0;

    /** The default chunk size with which to increase the capacity of this map. */
    public static final int    DEFAULT_GROWTH_CHUNK         = 10;

    /** The default capacity of this map. */
    public static final int    DEFAULT_CAPACITY             = 11;

    /** The default load factor of this map. */
    public static final double DEFAULT_LOAD_FACTOR          = 0.75;

    /**
     *  The size of this map.
     *  @serial
     */
    private int size;

    /**
     *  The keys of this map. Contains key values directly.
     *  Due to the use of a secondary hash function, the length of this
     *  array must be a prime.
     */
    private transient Object[] keys;

    /**
     *  The values of this map. Contains values directly.
     *  Due to the use of a secondary hash function, the length of this
     *  array must be a prime.
     */
    private transient int[] values;

    /** The states of each cell in the keys[] and values[]. */
    private transient byte[] states;

    private static final byte EMPTY = 0;
    private static final byte OCCUPIED = 1;
    private static final byte REMOVED = 2;

    /** The number of entries in use (removed or occupied). */
    private transient int used;

    /**
     *  The growth policy of this map (0 is relative growth, 1 is absolute growth).
     *  @serial
     */
    private int growthPolicy;

    /**
     *  The growth factor of this map, if the growth policy is
     *  relative.
     *  @serial
     */
    private double growthFactor;

    /**
     *  The growth chunk size of this map, if the growth policy is
     *  absolute.
     *  @serial
     */
    private int growthChunk;

    /**
     *  The load factor of this map.
     *  @serial
     */
    private double loadFactor;

    /**
     *  The next size at which to expand the data[].
     *  @serial
     */
    private int expandAt;

    /** A set view of the keys of this map. */
    private transient Set ckeys;

    /** A collection view of the values of this map. */
    private transient IntCollection cvalues;

    /** Indicates whether last call to containsKey() had a corresponding value. */
    private transient boolean hasLastValue;

    /** Value corresponding to to the key of the last call of containsKey(). */
    private transient int lastValue;

    private ObjectKeyIntOpenHashMap(int capacity, int growthPolicy, double growthFactor, int growthChunk, double loadFactor) {
        if (capacity < 0)
            Exceptions.negativeArgument("capacity", String.valueOf(capacity));
        if (growthFactor <= 0.0)
            Exceptions.negativeOrZeroArgument("growthFactor", String.valueOf(growthFactor));
        if (growthChunk <= 0)
            Exceptions.negativeOrZeroArgument("growthChunk", String.valueOf(growthChunk));
        if (loadFactor <= 0.0)
            Exceptions.negativeOrZeroArgument("loadFactor", String.valueOf(loadFactor));
        capacity = bak.pcj.hash.Primes.nextPrime(capacity);
        keys = new Object[capacity];
        values = new int[capacity];
        this.states = new byte[capacity];
        size = 0;
        expandAt = (int)Math.round(loadFactor*capacity);
        this.used = 0;
        this.growthPolicy = growthPolicy;
        this.growthFactor = growthFactor;
        this.growthChunk = growthChunk;
        this.loadFactor = loadFactor;
        hasLastValue = false;
    }

    /**
     *  Creates a new hash map with capacity 11, a relative
     *  growth factor of 1.0, and a load factor of 75%.
     */
    public ObjectKeyIntOpenHashMap() {
        this(DEFAULT_CAPACITY);
    }

    /**
     *  Creates a new hash map with the same mappings as a specified map.
     *
     *  @param      map
     *              the map whose mappings to put into the new map.
     *
     *  @throws     NullPointerException
     *              if <tt>map</tt> is <tt>null</tt>.
     */
    public ObjectKeyIntOpenHashMap(ObjectKeyIntMap map) {
        this();
        putAll(map);
    }

    /**
     *  Creates a new hash map with a specified capacity, a relative
     *  growth factor of 1.0, and a load factor of 75%.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative.
     */
    public ObjectKeyIntOpenHashMap(int capacity) {
        this(capacity, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, DEFAULT_LOAD_FACTOR);
    }

    /**
     *  Creates a new hash map with a capacity of 11, a relative
     *  growth factor of 1.0, and a specified load factor.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive.
     */
    public ObjectKeyIntOpenHashMap(double loadFactor) {
        this(DEFAULT_CAPACITY, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity and
     *  load factor, and a relative growth factor of 1.0.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive.
     */
    public ObjectKeyIntOpenHashMap(int capacity, double loadFactor) {
        this(capacity, DEFAULT_GROWTH_POLICY, DEFAULT_GROWTH_FACTOR, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity,
     *  load factor, and relative growth factor.
     *
     *  <p>The map capacity increases to <tt>capacity()*(1+growthFactor)</tt>.
     *  This strategy is good for avoiding many capacity increases, but
     *  the amount of wasted memory is approximately the size of the map.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @param      growthFactor
     *              the relative amount with which to increase the
     *              the capacity when a capacity increase is needed.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive;
     *              if <tt>growthFactor</tt> is not positive.
     */
    public ObjectKeyIntOpenHashMap(int capacity, double loadFactor, double growthFactor) {
        this(capacity, GROWTH_POLICY_RELATIVE, growthFactor, DEFAULT_GROWTH_CHUNK, loadFactor);
    }

    /**
     *  Creates a new hash map with a specified capacity,
     *  load factor, and absolute growth factor.
     *
     *  <p>The map capacity increases to <tt>capacity()+growthChunk</tt>.
     *  This strategy is good for avoiding wasting memory. However, an
     *  overhead is potentially introduced by frequent capacity increases.
     *
     *  @param      capacity
     *              the initial capacity of the map.
     *
     *  @param      loadFactor
     *              the load factor of the map.
     *
     *  @param      growthChunk
     *              the absolute amount with which to increase the
     *              the capacity when a capacity increase is needed.
     *
     *  @throws     IllegalArgumentException
     *              if <tt>capacity</tt> is negative;
     *              if <tt>loadFactor</tt> is not positive;
     *              if <tt>growthChunk</tt> is not positive.
     */
    public ObjectKeyIntOpenHashMap(int capacity, double loadFactor, int growthChunk) {
        this(capacity, GROWTH_POLICY_ABSOLUTE, DEFAULT_GROWTH_FACTOR, growthChunk, loadFactor);
    }

    // ---------------------------------------------------------------
    //      Hash table management
    // ---------------------------------------------------------------

    private static int hash(Object key) 
    { return key == null ? 0 : key.hashCode(); }

    private static boolean keyeq(Object k1, Object k2)
    { return k1 == null ? k2 == null : k1.equals(k2); }

    private void ensureCapacity(int elements) {
        if (elements >= expandAt) {
            int newcapacity;
            if (growthPolicy == GROWTH_POLICY_RELATIVE)
                newcapacity = (int)(keys.length * (1.0 + growthFactor));
            else
                newcapacity = keys.length + growthChunk;
            if (newcapacity*loadFactor < elements)
                newcapacity = (int)Math.round(((double)elements/loadFactor));
            newcapacity = bak.pcj.hash.Primes.nextPrime(newcapacity);
            expandAt = (int)Math.round(loadFactor*newcapacity);

            Object[] newkeys = new Object[newcapacity];
            int[] newvalues = new int[newcapacity];
            byte[] newstates = new byte[newcapacity];

            used = 0;
            //  re-hash
            for (int i = 0; i < keys.length; i++) {
                if (states[i] == OCCUPIED) {
                    used++;
                    Object k = keys[i];
                    int v = values[i];
                    //  first hash
                    int h = Math.abs(hash(k));
                    int n = h % newcapacity;
                    if (newstates[n] == OCCUPIED) {
                        //  second hash
                        int c = 1 + (h % (newcapacity - 2));
                        for (;;) {
                            n -= c;
                            if (n < 0)
                                n += newcapacity;
                            if (newstates[n] == EMPTY)
                                break;
                        }
                    }
                    newstates[n] = OCCUPIED;
                    newvalues[n] = v;
                    newkeys[n] = k;
                }
            }

            keys = newkeys;
            values = newvalues;
            states = newstates;
        }
    }

    // ---------------------------------------------------------------
    //      Operations not supported by abstract implementation
    // ---------------------------------------------------------------

    public Set keySet() {
        if (ckeys == null)
            ckeys = new KeySet();
        return ckeys;
    }

    public int lget() {
        if (!hasLastValue)
            Exceptions.noLastElement();
        return lastValue;
    }

    public int put(Object key, int value) {
        int result;

        //  first hash
        int h = Math.abs(hash(key));
        int i = h % keys.length;
        if (states[i] == OCCUPIED) {
            if (keyeq(keys[i], key)) {
                int oldValue = values[i];
                values[i] = value;
                return oldValue;
            }
            //  second hash
            int c = 1 + (h % (keys.length - 2));
            for (;;) {
                i -= c;
                if (i < 0)
                    i += keys.length;
                //  Empty entries are re-used
                if (states[i] == EMPTY || states[i] == REMOVED)
                    break;
                if (states[i] == OCCUPIED && keyeq(keys[i], key)) {
                    int oldValue = values[i];
                    values[i] = value;
                    return oldValue;
                }
            }
        }
        if (states[i] == EMPTY)
            used++;
        states[i] = OCCUPIED;
        keys[i] = key;
        values[i] = value;
        size++;
        ensureCapacity(used);
        return MapDefaults.defaultInt();
    }

    public IntCollection values() {
        if (cvalues == null)
            cvalues = new ValueCollection();
        return cvalues;
    }

    /**
     *  Returns a clone of this hash map.
     *
     *  @return     a clone of this hash map.
     *
     *  @since      1.1
     */
    public Object clone() {
        try {
            ObjectKeyIntOpenHashMap c = (ObjectKeyIntOpenHashMap)super.clone();
            c.keys = new Object[keys.length];
            System.arraycopy(keys, 0, c.keys, 0, keys.length);
            c.values = new int[values.length];
            System.arraycopy(values, 0, c.values, 0, values.length);
            c.states = new byte[states.length];
            System.arraycopy(states, 0, c.states, 0, states.length);
            //  The views should not refer to this map's views
            c.cvalues = null;
            c.ckeys = null;
            return c;
        } catch (CloneNotSupportedException e) {
            Exceptions.cloning(); return null;
        }
    }

    public ObjectKeyIntMapIterator entries() {
        return new ObjectKeyIntMapIterator() {
            int nextEntry = nextEntry(0);
            int lastEntry = -1;

            int nextEntry(int index) {
                while (index < keys.length && states[index] != OCCUPIED)
                    index++;
                return index;
            }

            public boolean hasNext() {
                return nextEntry < keys.length;
            }

            public void next() {
                if (!hasNext())
                    Exceptions.endOfIterator();
                lastEntry = nextEntry;
                nextEntry = nextEntry(nextEntry+1);
            }

            public void remove() {
                if (lastEntry == -1)
                    Exceptions.noElementToRemove();
                states[lastEntry] = REMOVED;
                size--;
                lastEntry = -1;
            }

            public Object getKey() {
                if (lastEntry == -1)
                    Exceptions.noElementToGet();
                return keys[lastEntry];
            }

            public int getValue() {
                if (lastEntry == -1)
                    Exceptions.noElementToGet();
                return values[lastEntry];
            }
        };
    }

    private class KeySet extends AbstractSet {

        public void clear()
        { ObjectKeyIntOpenHashMap.this.clear(); }

        public boolean contains(Object v) {
            return containsKey(v);
        }

        public Iterator iterator() {
            return new Iterator() {
                int nextEntry = nextEntry(0);
                int lastEntry = -1;

                int nextEntry(int index) {
                    while (index < keys.length && states[index] != OCCUPIED)
                        index++;
                    return index;
                }

                public boolean hasNext() {
                    return nextEntry < keys.length;
                }

                public Object next() {
                    if (!hasNext())
                        Exceptions.endOfIterator();
                    lastEntry = nextEntry;
                    nextEntry = nextEntry(nextEntry+1);
                    return keys[lastEntry];
                }

                public void remove() {
                    if (lastEntry == -1)
                        Exceptions.noElementToRemove();
                    states[lastEntry] = REMOVED;
                    keys[lastEntry] = null; // GC
                    size--;
                    lastEntry = -1;
                }
            };
        }

        public boolean remove(Object v) {
            boolean result = containsKey(v);
            if (result)
                ObjectKeyIntOpenHashMap.this.remove(v);
            return result;
        }

        public int size()
        { return size; }

    }


    private class ValueCollection extends AbstractIntCollection {

        public void clear()
        { ObjectKeyIntOpenHashMap.this.clear(); }

        public boolean contains(int v) {
            return containsValue(v);
        }

        public IntIterator iterator() {
            return new IntIterator() {
                int nextEntry = nextEntry(0);
                int lastEntry = -1;

                int nextEntry(int index) {
                    while (index < keys.length && states[index] != OCCUPIED)
                        index++;
                    return index;
                }

                public boolean hasNext() {
                    return nextEntry < keys.length;
                }

                public int next() {
                    if (!hasNext())
                        Exceptions.endOfIterator();
                    lastEntry = nextEntry;
                    nextEntry = nextEntry(nextEntry+1);
                    return values[lastEntry];
                }

                public void remove() {
                    if (lastEntry == -1)
                        Exceptions.noElementToRemove();
                    states[lastEntry] = REMOVED;
                    size--;
                    lastEntry = -1;
                }
            };
        }

        public int size()
        { return size; }

    }

    // ---------------------------------------------------------------
    //      Operations overwritten for efficiency
    // ---------------------------------------------------------------

    public void clear() {
        java.util.Arrays.fill(states, EMPTY);
        java.util.Arrays.fill(keys, null); // GC
        size = 0;
        used = 0;
    }

    public boolean containsKey(Object key) {
        int h = Math.abs(hash(key));
        int i = h % keys.length;
        if (states[i] != EMPTY) {
            if (states[i] == OCCUPIED && keyeq(keys[i], key)) {
                hasLastValue = true;
                lastValue = values[i];
                return true;
            }
            //  second hash

            int c = 1 + (h % (keys.length - 2));
            for (;;) {
                i -= c;
                if (i < 0)
                    i += keys.length;
                if (states[i] == EMPTY) {
                    hasLastValue = false;
                    return false;
                }
                if (states[i] == OCCUPIED && keyeq(keys[i], key)) {
                    hasLastValue = true;
                    lastValue = values[i];
                    return true;
                }
            }
        }
        hasLastValue = false;
        return false;
    }

    public boolean containsValue(int value) {
        for (int i = 0; i < states.length; i++)
            if (states[i] == OCCUPIED && values[i] == value)
                return true;
        return false;
    }

    public int get(Object key) {
        int h = Math.abs(hash(key));
        int i = h % keys.length;
        if (states[i] != EMPTY) {
            if (states[i] == OCCUPIED && keyeq(keys[i], key))
                return values[i];
            //  second hash

            int c = 1 + (h % (keys.length - 2));
            for (;;) {
                i -= c;
                if (i < 0)
                    i += keys.length;
                if (states[i] == EMPTY)
                    return MapDefaults.defaultInt();
                if (states[i] == OCCUPIED && keyeq(keys[i], key))
                    return values[i];
            }
        }
        return MapDefaults.defaultInt();
    }

    public boolean isEmpty()
    { return size == 0; }

    public int remove(Object key) {
        int h = Math.abs(hash(key));
        int i = h % keys.length;
        if (states[i] != EMPTY) {
            if (states[i] == OCCUPIED && keyeq(keys[i], key)) {
                int oldValue = values[i];
                states[i] = REMOVED;
                keys[i] = null; // GC
                size--;
                return oldValue;
            }
            //  second hash
            int c = 1 + (h % (keys.length - 2));
            for (;;) {
                i -= c;
                if (i < 0)
                    i += keys.length;
                if (states[i] == EMPTY) {
                    return MapDefaults.defaultInt();
                }
                if (states[i] == OCCUPIED && keyeq(keys[i], key)) {
                    int oldValue = values[i];
                    states[i] = REMOVED;
                    keys[i] = null; // GC
                    size--;
                    return oldValue;
                }
            }
        }
        return MapDefaults.defaultInt();
    }

    public int size()
    { return size; }

    public int tget(Object key) {
        int h = Math.abs(hash(key));
        int i = h % keys.length;
        if (states[i] != EMPTY) {
            if (states[i] == OCCUPIED && keyeq(keys[i], key))
                return values[i];
            //  second hash

            int c = 1 + (h % (keys.length - 2));
            for (;;) {
                i -= c;
                if (i < 0)
                    i += keys.length;
                if (states[i] == EMPTY)
                    Exceptions.noSuchMapping(key);
                if (states[i] == OCCUPIED && keyeq(keys[i], key))
                    return values[i];
            }
        }
        Exceptions.noSuchMapping(key); throw new RuntimeException();
    }

    // ---------------------------------------------------------------
    //      Serialization
    // ---------------------------------------------------------------

    /**
     *  @serialData     Default fields; the capacity of the
     *                  map (<tt>int</tt>); the maps's entries
     *                  (<tt>int</tt>, <tt><S></tt>).
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(keys.length);
        ObjectKeyIntMapIterator i = entries();
        while (i.hasNext()) {
            i.next();
            s.writeObject(i.getKey());
            s.writeInt(i.getValue());
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        keys = new Object[s.readInt()];
        states = new byte[keys.length];
        values = new int[keys.length];
        used = size;

        for (int n = 0; n < size; n++) {
            Object key = s.readObject();
            int value = s.readInt();

            //  first hash
            int h = Math.abs(hash(key));
            int i = h % keys.length;
            if (states[i] != EMPTY) {
                //  second hash
                int c = 1 + (h % (keys.length - 2));
                for (;;) {
                    i -= c;
                    if (i < 0)
                        i += keys.length;
                    if (states[i] == EMPTY)
                        break;
                }
            }
            states[i] = OCCUPIED;
            keys[i] = key;
            values[i] = value;
        }
    }

}
