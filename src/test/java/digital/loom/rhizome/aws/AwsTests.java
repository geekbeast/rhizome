package digital.loom.rhizome.aws;

import com.kryptnostic.rhizome.configuration.amazon.AmazonConfiguration;
import com.kryptnostic.rhizome.configuration.amazon.AwsLaunchConfiguration;
import com.openlattice.ResourceConfigurationLoader;
import org.junit.Assert;
import org.junit.Test;

import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

public class AwsTests {

    @Test
    public void testLoadAmazonLaunchConfiguration() {
        Assert.assertNotNull( ResourceConfigurationLoader.loadConfigurationFromResource( "aws.yaml", AwsLaunchConfiguration.class ) );
    }
}
