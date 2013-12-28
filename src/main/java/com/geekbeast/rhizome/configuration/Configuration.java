package com.geekbeast.rhizome.configuration;

import java.io.Serializable;

public interface Configuration extends Serializable {
    public abstract ConfigurationKey getKey();
    
}
