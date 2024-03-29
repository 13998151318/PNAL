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
package bak.pcj.benchmark;

import bak.pcj.map.IntKeyMap;
import bak.pcj.Adapter;
import java.util.HashMap;


/**
 *  This class represents benchmarks for
 *  {@link HashMap HashMap}s adapted to primitive maps.
 *
 *  @author     S&oslash;ren Bak
 *  @version    1.0     2003/8/1
 *  @since      1.0
 */
public class IntKeyAdaptedHashMapBenchmark extends IntKeyMapBenchmark {

    public IntKeyAdaptedHashMapBenchmark() {
        super(
            new IntKeyMapFactory() {
                public IntKeyMap create(int[] keys, Integer[] values) {
                    HashMap m = new HashMap();
                    for (int i = 0; i < keys.length; i++)
                        m.put(new Integer(keys[i]), values[i]);
                    return Adapter.asIntKeys(m);
                }
            }
        );
    }

}
