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
package bak.pcj.adapter;

import bak.pcj.Adapter;
import bak.pcj.IntIterator;
import bak.pcj.FloatCollection;
import bak.pcj.map.IntKeyFloatMap;
import bak.pcj.map.AbstractIntKeyFloatMap;
import bak.pcj.map.IntKeyFloatMapIterator;
import bak.pcj.map.MapDefaults;
import bak.pcj.map.NoSuchMappingException;
import bak.pcj.set.IntSet;
import bak.pcj.util.Exceptions;

import java.util.Map;
import java.util.Iterator;

/**
 *  This class represents adaptions of Java Collections Framework
 *  maps to primitive maps from int values to float values.
 *  The adapter is implemented as a wrapper around the map. 
 *  Thus, changes to the underlying map are reflected by this
 *  map and vice versa.
 *
 *  <p>
 *  Adapters from JCF maps to primitive map will
 *  fail if the JCF collection contains <tt>null</tt> keys/values or
 *  keys/values of the wrong class. However, adapters are not fast
 *  failing in the case that the underlying map should
 *  contain illegal keys or values. To implement fast failure would require
 *  every operation to check every key and value of the underlying
 *  map before doing anything. Instead validation methods
 *  are provided. They can be called using the assertion facility
 *  in the client code:
 *  <pre>
 *      MapToIntKeyFloatMapAdapter s;
 *      ...
 *      <b>assert</b> s.validate();
 *  </pre>
 *  or by letting the adapter throw an exception on illegal values:
 *  <pre>
 *      MapToIntKeyFloatMapAdapter s;
 *      ...
 *      s.evalidate();  // Throws an exception on illegal values
 *  </pre>
 *  Either way, validation must be invoked directly by the client
 *  code.
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.3     21-08-2003 19:09
 *  @since      1.0
 */
public class MapToIntKeyFloatMapAdapter extends AbstractIntKeyFloatMap implements IntKeyFloatMap {

    /** The underlying map. */
    protected Map map;

    /** The value corresponding to the last key found by containsKey(). */
    protected Float lastValue;

    /**
     *  Creates a new adaption to a map from int
     *  values to float values.
     *
     *  @param      map
     *              the underlying map. This map must
     *              consist of keys of class
     *              {@link Integer Integer}.
     *              values of class
     *              {@link Float Float}. Otherwise a
     *              {@link ClassCastException ClassCastException}
     *              will be thrown by some methods.
     *
     *  @throws     NullPointerException
     *              if <tt>map</tt> is <tt>null</tt>.
     */
    public MapToIntKeyFloatMapAdapter(Map map) {
        if (map == null)
            Exceptions.nullArgument("map");
        this.map = map;
        lastValue = null;
    }

    /**
     *  Creates a new adaption to a map from int
     *  values to float values. The map to adapt is optionally validated.
     *
     *  @param      map
     *              the underlying map. This map must
     *              consist of keys of class
     *              {@link Integer Integer}.
     *              values of class
     *              {@link Float Float}. Otherwise a
     *              {@link ClassCastException ClassCastException}
     *              will be thrown by some methods.
     *
     *  @param      validate
     *              indicates whether <tt>map</tt> should
     *              be checked for illegal values.
     *
     *  @throws     NullPointerException
     *              if <tt>map</tt> is <tt>null</tt>.
     *
     *  @throws     IllegalStateException
     *              if <tt>validate</tt> is <tt>true</tt> and
     *              <tt>map</tt> contains a <tt>null</tt> key/value,
     *              a key that is not of class
     *              {@link Integer Integer},
     *              or a value that is not of class
     *              {@link Float Float}.
     */
    public MapToIntKeyFloatMapAdapter(Map map, boolean validate) {
        if (map == null)
            Exceptions.nullArgument("map");
        this.map = map;
        lastValue = null;
        if (validate)
            evalidate();
    }

    public void clear()
    { map.clear(); }

    public boolean containsKey(int key) {
        lastValue = (Float)map.get(new Integer(key));
        return lastValue != null;
    }

    public boolean containsValue(float value)
    { return map.containsValue(new Float(value)); }

    public IntKeyFloatMapIterator entries() {
        return new IntKeyFloatMapIterator() {
            Iterator i = map.entrySet().iterator();
            Map.Entry lastEntry = null;

            public boolean hasNext()
            { return i.hasNext(); }

            public void next()
            { lastEntry = (Map.Entry)i.next(); }

            public int getKey() {
                if (lastEntry == null)
                    Exceptions.noElementToGet();
                return ((Integer)lastEntry.getKey()).intValue();
            }

            public float getValue() {
                if (lastEntry == null)
                    Exceptions.noElementToGet();
                return ((Float)lastEntry.getValue()).floatValue();
            }

            public void remove() {
                i.remove();
                lastEntry = null;
            }
        };
    }

    public float get(int key) {
        Float value = (Float)map.get(new Integer(key));
        return value == null ? MapDefaults.defaultFloat() : value.floatValue();
    }

    public IntSet keySet()
    { return new SetToIntSetAdapter(map.keySet()); }

    public float lget() {
        if (lastValue == null)
            Exceptions.noLastElement();
        return lastValue.floatValue();
    }

    public float put(int key, float value) {
        Float oldValue = (Float)map.put(new Integer(key), new Float(value));
        return oldValue == null ? MapDefaults.defaultFloat() : oldValue.floatValue();
    }

    public float remove(int key) {
        Float value = (Float)map.remove(new Integer(key));
        return value == null ? MapDefaults.defaultFloat() : value.floatValue();
    }

    public int size()
    { return map.size(); }

    public FloatCollection values()
    { return new CollectionToFloatCollectionAdapter(map.values()); }

    public float tget(int key) {
        Float value = (Float)map.get(new Integer(key));
        if (value == null)
            Exceptions.noSuchMapping(String.valueOf(key));
        return value.floatValue();
    }

    /**
     *  Indicates whether the underlying map is valid for
     *  this adapter. For the underlying map to be valid, it
     *  can only contain {@link Integer Integer} keys, no <tt>null</tt>
     *  keys/values, and only {@link Float Float} values.
     *
     *  @return     <tt>true</tt> if the underlying map is
     *              valid; returns <tt>false</tt> otherwise.
     */
    public boolean validate()
    { return Adapter.isIntKeyFloatAdaptable(map); }

    /**
     *  Validates the map underlying this adapter and throws
     *  an exception if it is invalid. For the underlying map 
     *  to be valid, it
     *  can only contain {@link Integer Integer} keys, no <tt>null</tt>
     *  keys/values, and only {@link Float Float} values.
     *
     *  @throws     IllegalStateException
     *              if the underlying map is invalid.
     */
    public void evalidate() {
        if (!validate())
            Exceptions.cannotAdapt("map");
    }

}
