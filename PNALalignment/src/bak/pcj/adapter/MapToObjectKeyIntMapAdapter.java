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
import bak.pcj.IntCollection;
import bak.pcj.map.ObjectKeyIntMap;
import bak.pcj.map.AbstractObjectKeyIntMap;
import bak.pcj.map.ObjectKeyIntMapIterator;
import bak.pcj.map.MapDefaults;
import bak.pcj.map.NoSuchMappingException;
import bak.pcj.set.IntSet;
import bak.pcj.util.Exceptions;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 *  This class represents adaptions of Java Collections Framework
 *  maps to primitive maps from object values to int values.
 *  The adapter is implemented as a wrapper around the map. 
 *  Thus, changes to the underlying map are reflected by this
 *  map and vice versa.
 *
 *  <p>
 *  Adapters from JCF maps to primitive map will
 *  fail if the JCF collection contains <tt>null</tt> values or
 *  values of the wrong class. However, adapters are not fast
 *  failing in the case that the underlying map should
 *  contain illegal keys or values. To implement fast failure would require
 *  every operation to check every key and value of the underlying
 *  map before doing anything. Instead validation methods
 *  are provided. They can be called using the assertion facility
 *  in the client code:
 *  <pre>
 *      MapToObjectKeyIntMapAdapter s;
 *      ...
 *      <b>assert</b> s.validate();
 *  </pre>
 *  or by letting the adapter throw an exception on illegal values:
 *  <pre>
 *      MapToObjectKeyIntMapAdapter s;
 *      ...
 *      s.evalidate();  // Throws an exception on illegal values
 *  </pre>
 *  Either way, validation must be invoked directly by the client
 *  code.
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.1     21-08-2003 19:12
 *  @since      1.1
 */
public class MapToObjectKeyIntMapAdapter extends AbstractObjectKeyIntMap implements ObjectKeyIntMap {

    /** The underlying map. */
    protected Map map;

    /** The value corresponding to the last key found by containsKey(). */
    protected Integer lastValue;

    /**
     *  Creates a new adaption to a map from object
     *  values to int values.
     *
     *  @param      map
     *              the underlying map. This map must
     *              consist of
     *              values of class
     *              {@link Integer Integer}. Otherwise a
     *              {@link ClassCastException ClassCastException}
     *              will be thrown by some methods.
     *
     *  @throws     NullPointerException
     *              if <tt>map</tt> is <tt>null</tt>.
     */
    public MapToObjectKeyIntMapAdapter(Map map) {
        if (map == null)
            Exceptions.nullArgument("map");
        this.map = map;
        lastValue = null;
    }

    /**
     *  Creates a new adaption to a map from object
     *  values to int values. The map to adapt is optionally validated.
     *
     *  @param      map
     *              the underlying map. This map must
     *              consist of 
     *              values of class
     *              {@link Integer Integer}. Otherwise a
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
     *              <tt>map</tt> contains a <tt>null</tt> value,
     *              or a value that is not of class
     *              {@link Integer Integer}.
     */
    public MapToObjectKeyIntMapAdapter(Map map, boolean validate) {
        if (map == null)
            Exceptions.nullArgument("map");
        this.map = map;
        lastValue = null;
        if (validate)
            evalidate();
    }

    public void clear()
    { map.clear(); }

    public boolean containsKey(Object key) {
        lastValue = (Integer)map.get(key);
        return lastValue != null;
    }

    public boolean containsValue(int value)
    { return map.containsValue(new Integer(value)); }

    public ObjectKeyIntMapIterator entries() {
        return new ObjectKeyIntMapIterator() {
            Iterator i = map.entrySet().iterator();
            Map.Entry lastEntry = null;

            public boolean hasNext()
            { return i.hasNext(); }

            public void next()
            { lastEntry = (Map.Entry)i.next(); }

            public Object getKey() {
                if (lastEntry == null)
                    Exceptions.noElementToGet();
                return lastEntry.getKey();
            }

            public int getValue() {
                if (lastEntry == null)
                    Exceptions.noElementToGet();
                return ((Integer)lastEntry.getValue()).intValue();
            }

            public void remove() {
                i.remove();
                lastEntry = null;
            }
        };
    }

    public int get(Object key) {
        Integer value = (Integer)map.get(key);
        return value == null ? MapDefaults.defaultInt() : value.intValue();
    }

    public Set keySet()
    { return map.keySet(); }

    public int lget() {
        if (lastValue == null)
            Exceptions.noLastElement();
        return lastValue.intValue();
    }

    public int put(Object key, int value) {
        Integer oldValue = (Integer)map.put(key, new Integer(value));
        return oldValue == null ? MapDefaults.defaultInt() : oldValue.intValue();
    }

    public int remove(Object key) {
        Integer value = (Integer)map.remove(key);
        return value == null ? MapDefaults.defaultInt() : value.intValue();
    }

    public int size()
    { return map.size(); }

    public IntCollection values()
    { return new CollectionToIntCollectionAdapter(map.values()); }

    public int tget(Object key) {
        Integer value = (Integer)map.get(key);
        if (value == null)
            Exceptions.noSuchMapping(key);
        return value.intValue();
    }

    /**
     *  Indicates whether the underlying map is valid for
     *  this adapter. For the underlying map to be valid it
     *  can contain no <tt>null</tt>
     *  values and only {@link Integer Integer} values.
     *
     *  @return     <tt>true</tt> if the underlying map is
     *              valid; returns <tt>false</tt> otherwise.
     */
    public boolean validate()
    { return Adapter.isObjectKeyIntAdaptable(map); }

    /**
     *  Validates the map underlying this adapter and throws
     *  an exception if it is invalid. For the underlying map to be valid it
     *  can contain no <tt>null</tt>
     *  values and only {@link Integer Integer} values.
     *
     *  @throws     IllegalStateException
     *              if the underlying map is invalid.
     */
    public void evalidate() {
        if (!validate())
            Exceptions.cannotAdapt("map");
    }

}
