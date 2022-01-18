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

package com.geekbeast.rhizome.configuration.hazelcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration class for Hazelcast's scheduled executor service.
 */
public class ScheduledExecutorConfiguration {
    private static final String NAME        = "name";
    private static final String CAPACITY    = "capacity";
    private static final String DURABILITY  = "durability";
    private static final String POOL_SIZE   = "pool-size";
    private static final String QUOROM_NAME = "quorum-name";

    private final String name;
    private final int    poolSize;
    private final int    capacity;
    private final int    durability;
    private final String splitBrainProtectionName;

    public ScheduledExecutorConfiguration(
            @JsonProperty( NAME ) String name,
            @JsonProperty( POOL_SIZE ) int poolSize,
            @JsonProperty( CAPACITY ) int capacity,
            @JsonProperty( DURABILITY ) int durability,
            @JsonProperty( QUOROM_NAME ) Optional<String> splitBrainProtectionName ) {
        this.name = name;
        this.poolSize = poolSize;
        this.capacity = capacity;
        this.durability = durability;
        this.splitBrainProtectionName = splitBrainProtectionName.orElse( null );
    }

    @JsonProperty( NAME )
    public String getName() {
        return name;
    }

    @JsonProperty( POOL_SIZE )
    public int getPoolSize() {
        return poolSize;
    }

    @JsonProperty( CAPACITY )
    public int getCapacity() {
        return capacity;
    }

    @JsonProperty( DURABILITY )
    public int getDurability() {
        return durability;
    }

    @JsonProperty( QUOROM_NAME )
    public String getSplitBrainProtectionName() {
        return splitBrainProtectionName;
    }

    @Override public boolean equals( Object o ) {
        if ( this == o ) { return true; }
        if ( !( o instanceof ScheduledExecutorConfiguration ) ) { return false; }
        ScheduledExecutorConfiguration that = (ScheduledExecutorConfiguration) o;
        return poolSize == that.poolSize &&
                capacity == that.capacity &&
                durability == that.durability &&
                Objects.equals( name, that.name ) &&
                Objects.equals( splitBrainProtectionName, that.splitBrainProtectionName );
    }

    @Override public int hashCode() {
        return Objects.hash( name, poolSize, capacity, durability, splitBrainProtectionName );
    }

    @Override public String toString() {
        return "ScheduledExecutorConfiguration{" +
                "name='" + name + '\'' +
                ", poolSize=" + poolSize +
                ", capacity=" + capacity +
                ", durability=" + durability +
                ", quorumName='" + splitBrainProtectionName + '\'' +
                '}';
    }
}
