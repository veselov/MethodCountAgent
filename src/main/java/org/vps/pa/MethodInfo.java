package org.vps.pa;

import java.util.concurrent.atomic.AtomicLong;

public class MethodInfo {

    public final String name;
    public final boolean isConstructor;
    public final AtomicLong counter = new AtomicLong();

    public MethodInfo(String name, boolean constructor) {
        this.name = name;
        isConstructor = constructor;
    }
}
