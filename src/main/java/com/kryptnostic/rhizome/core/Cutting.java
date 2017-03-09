/*
 * Copyright 2017 Kryptnostic, Inc. (dba Loom)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.kryptnostic.rhizome.core;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Matthew Tamayo-Rios &lt;matthew@kryptnostic.com&gt;
 */
public class Cutting implements Serializable {
    private static final long                     serialVersionUID           = -8431251486986674006L;
    private static final RhizomeApplicationServer RHIZOME_APPLICATION_SERVER = new RhizomeApplicationServer();
    private static final CountDownLatch           latch                      = new CountDownLatch( 1 );
    private static final Lock                     lock                       = new ReentrantLock();
    private final String[]                        activeProfiles;
    private final Class<?>[]                      additionalPods;

    public Cutting( String[] activeProfiles, Class<?>... additionalPods ) {
        this.activeProfiles = activeProfiles;
        this.additionalPods = additionalPods;
    }

    public <T> T getBean( Class<T> clazz ) throws InterruptedException {
        ensureInitialized();
        return RHIZOME_APPLICATION_SERVER.getContext().getBean( clazz );
    }

    public <T> Map<String, T> getBeansOfType( Class<T> clazz ) throws InterruptedException {
        ensureInitialized();
        return RHIZOME_APPLICATION_SERVER.getContext().getBeansOfType( clazz );
    }

    public void ensureInitialized() throws InterruptedException {
        if ( !RHIZOME_APPLICATION_SERVER.getContext().isActive() && lock.tryLock() ) {
            if( additionalPods.length > 0 ) {
                RHIZOME_APPLICATION_SERVER.intercrop( additionalPods );
            }
            
            RHIZOME_APPLICATION_SERVER.sprout( activeProfiles );
            
            latch.countDown();
        } else {
            latch.await();
        }
    }
}
