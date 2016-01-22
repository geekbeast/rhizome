package com.kryptnostic.rhizome.configuration;

import java.io.Serializable;

public interface Configuration extends Serializable {
    ConfigurationKey getKey();
}
