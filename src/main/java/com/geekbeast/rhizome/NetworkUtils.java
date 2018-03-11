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

package com.geekbeast.rhizome;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */
public class NetworkUtils {
    private static final Logger logger = LoggerFactory.getLogger( NetworkUtils.class );

    public static final boolean isRunningOnHost( String host ) {
        try {
            InetAddress[] acceptableHosts = InetAddress.getAllByName( host );
            Set<String> acceptableAddresses = new HashSet<>( acceptableHosts.length );
            for ( InetAddress acceptableAddress : acceptableHosts ) {
                acceptableAddresses.add( acceptableAddress.getHostAddress() );
            }
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while ( networkInterfaces.hasMoreElements() ) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> networkAddresses = networkInterface.getInetAddresses();
                while ( networkAddresses.hasMoreElements() ) {
                    if ( acceptableAddresses.contains( networkAddresses.nextElement().getHostAddress() ) ) {
                        return true;
                    }
                }
            }
        } catch ( SocketException | UnknownHostException e ) {
            logger.error( "Unable to determine host we are running on.", e );
        }
        return false;

    }
}
