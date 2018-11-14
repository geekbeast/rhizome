package com.kryptnostic.rhizome.configuration;

public final class ConfigurationConstants {
    private ConfigurationConstants() {
    }

    public static final class Profiles {
        public static final String AWS_CONFIGURATION_PROFILE   = "aws";
        public static final String AWS_TESTING_PROFILE         = "awstest";
        public static final String LOCAL_CONFIGURATION_PROFILE = "local";

        private Profiles() {
        }
    }

    public static final class HZ {
        private HZ() {
        }

        public static final class MAPS {
            public static final String CONFIGURATION = "configurations";

            private MAPS() {
            }
        }
    }

    public static final class EDM_SYNC {
        private EDM_SYNC() {
        }

        public static final String EDM_SYNC_CONFIGURATION = "sync";
    }
}
