package com.geekbeast.rhizome.configuration;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public interface Configuration extends Serializable {
    ConfigurationKey getKey();
}
