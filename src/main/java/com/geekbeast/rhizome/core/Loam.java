package com.geekbeast.rhizome.core;

import org.springframework.beans.BeansException;

public interface Loam {
    public abstract void start() throws Exception;
    public abstract void stop() throws Exception;
    public abstract void join() throws BeansException, InterruptedException;
}
