/*
 * Copyright (C) 2018. OpenLattice, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * You can contact the owner of the copyright at support@openlattice.com
 *
 *
 */

package com.openlattice.hazelcast.serializer;

import com.kryptnostic.rhizome.hazelcast.serializers.AbstractStreamSerializerTest;
import com.openlattice.hazelcast.serializers.Jdk8StreamSerializers.AbstractLocalDateStreamSerializer;
import java.time.LocalDate;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class AbstractLocalDateStreamSerializerTest extends AbstractStreamSerializerTest<AbstractLocalDateStreamSerializer, LocalDate> {
    @Override protected AbstractLocalDateStreamSerializer createSerializer() {
        return new AbstractLocalDateStreamSerializer() {
            @Override public int getTypeId() {
                return 0;
            }
        };
    }

    @Override protected LocalDate createInput() {
        return LocalDate.now();
    }
}
