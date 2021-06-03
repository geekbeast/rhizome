package com.geekbeast.rhizome.hazelcast.pods

import com.geekbeast.rhizome.hazelcast.serializers.TestSharedStreamSerializers
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Component

/**
 *
 * @author Matthew Tamayo-Rios &lt;matthew@openlattice.com&gt;
 */

@Configuration
@ComponentScan(
        basePackageClasses = [TestSharedStreamSerializers::class],
        includeFilters = [ComponentScan.Filter(
                value = [Component::class],
                type = FilterType.ANNOTATION
        )]
)
class TestSharedStreamSerializersPod {}

