package com.kryptnostic.rhizome.configuration;

public final class ConfigurationConstants {
    private ConfigurationConstants() {}

    public static final class Profiles {
        private Profiles() {}

        public static final String AWS_CONFIGURATION_PROFILE = "aws";
        public static final String LOCAL_CONFIGURATION_PROFILE = "local";
    }

    public static final class HZ {
        private HZ() {}

        public static final class MAPS {
            private MAPS() {}

            public static final String CONFIGURATION = "configurations";
        }
    }
}
