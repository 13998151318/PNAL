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
package bak.pcj.set;

import bak.pcj.AbstractShortCollection;
import bak.pcj.ShortIterator;
import bak.pcj.hash.DefaultShortHashFunction;

/**
 *  This class represents an abstract base for implementing
 *  sets of short values. All operations that can be implemented
 *  using iterators and the <tt>get()</tt> and <tt>set()</tt> methods
 *  are implemented as such. In most cases, this is
 *  hardly an efficient solution, and at least some of those
 *  methods should be overridden by sub-classes.
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.1     2003/1/10
 *  @since      1.0
 */
public abstract class AbstractShortSet extends AbstractShortCollection implements ShortSet {

    /** Default constructor to be invoked by sub-classes. */
    protected AbstractShortSet() { }

    public boolean equals(Object obj) {
        if (!(obj instanceof ShortSet))
            return false;
        ShortSet s = (ShortSet)obj;
        if (s.size() != size())
            return false;
        return containsAll(s);
    }

    public int hashCode() {
        int h = 0;
        ShortIterator i = iterator();
        while (i.hasNext())
            h += DefaultShortHashFunction.INSTANCE.hash(i.next());
        return h;
    }

}
