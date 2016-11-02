package digital.loom.rhizome.aws;

import org.junit.Assert;
import org.junit.Test;

import com.kryptnostic.rhizome.configuration.amazon.AmazonLaunchConfiguration;
import com.kryptnostic.rhizome.configuration.service.ConfigurationService;

public class AwsTests {

    @Test
    public void testLoadAmazonLaunchConfiguration() {
        Assert.assertNotNull( ConfigurationService.StaticLoader.loadConfiguration( AmazonLaunchConfiguration.class ) );
    }
}
