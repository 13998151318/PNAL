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
import bak.pcj.ByteIterator;
import bak.pcj.set.ByteSet;
import bak.pcj.set.AbstractByteSet;
import bak.pcj.adapter.IteratorToByteIteratorAdapter;
import bak.pcj.util.Exceptions;

import java.util.Set;

/**
 *  This class represents adaptions of Java Collections Framework
 *  sets to primitive sets of byte values.
 *  The adapter is implemented as a wrapper around the set. 
 *  Thus, changes to the underlying set are reflected by this
 *  set and vice versa.
 *
 *  <p>
 *  Adapters from JCF collections to primitive collections will
 *  fail if the JCF collection contains <tt>null</tt> values or
 *  values of the wrong class. However, adapters are not fast
 *  failing in the case that the underlying collection should
 *  contain illegal values. To implement fast failure would require
 *  every operation to check every element of the underlying
 *  collection before doing anything. Instead validation methods
 *  are provided. They can be called using the assertion facility
 *  in the client code:
 *  <pre>
 *      SetToByteSetAdapter s;
 *      ...
 *      <b>assert</b> s.validate();
 *  </pre>
 *  or by letting the adapter throw an exception on illegal values:
 *  <pre>
 *      SetToByteSetAdapter s;
 *      ...
 *      s.evalidate();  // Throws an exception on illegal values
 *  </pre>
 *  Either way, validation must be invoked directly by the client
 *  code.
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.2     21-08-2003 19:04
 *  @since      1.0
 */
public class SetToByteSetAdapter extends AbstractByteSet implements ByteSet {

    /** The underlying set. */
    protected Set set;

    /**
     *  Creates a new adaption to a set of byte
     *  values.
     *
     *  @param      set
     *              the underlying set. This set must
     *              consist of values of class
     *              {@link Byte Byte}. Otherwise a
     *              {@link ClassCastException ClassCastException}
     *              will be thrown by some methods.
     *
     *  @throws     NullPointerException
     *              if <tt>set</tt> is <tt>null</tt>.
     */
    public SetToByteSetAdapter(Set set) {
        if (set == null)
            Exceptions.nullArgument("set");
        this.set = set;
    }

    /**
     *  Creates a new adaption to a set of byte
     *  values. The set to adapt is optionally validated.
     *
     *  @param      set
     *              the underlying set. This set must
     *              consist of values of class
     *              {@link Byte Byte}. Otherwise a
     *              {@link ClassCastException ClassCastException}
     *              will be thrown by some methods.
     *
     *  @param      validate
     *              indicates whether <tt>set</tt> should
     *              be checked for illegal values.
     *
     *  @throws     NullPointerException
     *              if <tt>set</tt> is <tt>null</tt>.
     *
     *  @throws     IllegalStateException
     *              if <tt>validate</tt> is <tt>true</tt> and
     *              <tt>set</tt> contains a <tt>null</tt> value
     *              or a value that is not of class
     *              {@link Byte Byte}.
     */
    public SetToByteSetAdapter(Set set, boolean validate) {
        if (set == null)
            Exceptions.nullArgument("set");
        this.set = set;
        if (validate)
            evalidate();
    }

    public boolean add(byte v)
    { return set.add(new Byte(v)); }

    public void clear()
    { set.clear(); }

    public boolean contains(byte v)
    { return set.contains(new Byte(v)); }

    public int hashCode()
    { return set.hashCode(); }

    public ByteIterator iterator()
    { return new IteratorToByteIteratorAdapter(set.iterator()); }

    public boolean remove(byte v)
    { return set.remove(new Byte(v)); }

    public int size()
    { return set.size(); }

    /**
     *  Indicates whether the underlying set is valid for
     *  this adapter. For the underlying set to be valid, it
     *  can only contain {@link Byte Byte} values and no <tt>null</tt>
     *  values.
     *
     *  @return     <tt>true</tt> if the underlying set is
     *              valid; returns <tt>false</tt> otherwise.
     */
    public boolean validate()
    { return Adapter.isByteAdaptable(set); }

    /**
     *  Validates the set underlying this adapter and throws
     *  an exception if it is invalid. For the underlying set
     *  to be valid, it can only contain {@link Byte Byte}
     *  values and no <tt>null</tt> values.
     *
     *  @throws     IllegalStateException
     *              if the underlying set is invalid.
     */
    public void evalidate() {
        if (!validate())
            Exceptions.cannotAdapt("set");
    }

}
