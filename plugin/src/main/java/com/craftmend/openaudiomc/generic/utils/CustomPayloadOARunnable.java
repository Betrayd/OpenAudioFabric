package com.craftmend.openaudiomc.generic.utils;

public class CustomPayloadOARunnable extends OARunnable {

    Runnable function;

    public CustomPayloadOARunnable(Runnable function)
    {
        super();
        this.function = function;
    }

    @Override
    public void run() {
        function.run();
    }
    
}
