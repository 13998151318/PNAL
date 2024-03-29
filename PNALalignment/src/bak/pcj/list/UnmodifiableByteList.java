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
package bak.pcj.list;

import bak.pcj.util.Exceptions;

import bak.pcj.ByteCollection;
import bak.pcj.UnmodifiableByteCollection;

/**
 *  This class represents unmodifiable lists of byte values.
 *
 *  @see        java.util.Collections#unmodifiableList(java.util.List)
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.1     24-08-2003 20:52
 *  @since      1.0
 */
public class UnmodifiableByteList extends UnmodifiableByteCollection implements ByteList {

    /**
     *  Creates a new unmodifiable list on an existing
     *  list. The result is a list whose elements and
     *  behaviour is the same as the existing list's except
     *  that the new list cannot be modified.
     *
     *  @param      l
     *              the existing list to make unmodifiable.
     *
     *  @throws     NullPointerException
     *              if <tt>l</tt> is <tt>null</tt>.
     */
    public UnmodifiableByteList(ByteList l) {
        super(l);
    }

    private ByteList list()
    { return (ByteList)collection; }

    /**
     *  Throws <tt>UnsupportedOperationException</tt>.
     *
     *  @throws     UnsupportedOperationException
     *              unconditionally.
     */
    public void add(int index, byte v)
    { Exceptions.unmodifiable("list"); }

    /**
     *  Throws <tt>UnsupportedOperationException</tt>.
     *
     *  @throws     UnsupportedOperationException
     *              unconditionally.
     */
    public boolean addAll(int index, ByteCollection c)
    { Exceptions.unmodifiable("list"); return false; }

    public byte get(int index)
    { return list().get(index); }

    public int indexOf(byte c)
    { return list().indexOf(c); }

    /**
     *  @since      1.2
     */
    public int indexOf(int index, byte c)
    { return list().indexOf(index, c); }

    public int lastIndexOf(byte c)
    { return list().lastIndexOf(c); }

    /**
     *  @since      1.2
     */
    public int lastIndexOf(int index, byte c)
    { return list().lastIndexOf(index, c); }

    public ByteListIterator listIterator()
    { return listIterator(0); }

    public ByteListIterator listIterator(int index) {
        final ByteListIterator i = list().listIterator(index);
        return new ByteListIterator() {
            public boolean hasNext()
            { return i.hasNext(); }

            public byte next()
            { return i.next(); }

            //  It is necessary to override remove() since we have
            //  no way of knowing how iterators are implemented
            //  in the underlying class.
            public void remove()
            { Exceptions.unmodifiable("list"); }
            
            public void add(byte v)
            { Exceptions.unmodifiable("list"); }

            public boolean hasPrevious()
            { return i.hasPrevious(); }

            public int nextIndex()
            { return i.nextIndex(); }

            public byte previous()
            { return i.previous(); }
            
            public int previousIndex()
            { return i.previousIndex(); }

            public void set(byte v)
            { Exceptions.unmodifiable("list"); }
        };
    }

    /**
     *  Throws <tt>UnsupportedOperationException</tt>.
     *
     *  @throws     UnsupportedOperationException
     *              unconditionally.
     */
    public byte removeElementAt(int index)
    { Exceptions.unmodifiable("list"); throw new RuntimeException(); }

    /**
     *  Throws <tt>UnsupportedOperationException</tt>.
     *
     *  @throws     UnsupportedOperationException
     *              unconditionally.
     */
    public byte set(int index, byte v)
    { Exceptions.unmodifiable("list"); throw new RuntimeException(); }

}
