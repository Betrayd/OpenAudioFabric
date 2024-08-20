package com.craftmend.openaudiomc.generic.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Util;

// TODO: fix memory leak pls
public abstract class OARunnable implements Runnable {

    private static java.util.List<OARunnable> instances = new java.util.ArrayList<>();

    public static void register()
    {
        ServerTickEvents.START_SERVER_TICK.register((x) -> 
        {
            for(OARunnable runnable : instances)
            {
                if(runnable != null)
                {
                    runnable.tick();
                }
            }
        });
    }
    
    boolean async = false;
    int ticks = -1;
    int maxTicks = -1;

    public OARunnable()
    {
        instances.add(this);
    }

    public int getTicks()
    {
        return ticks;
    }

    private void tick()
    {
        if(ticks > -1 && ticks <= 0)
        {
            if(!async) {
                run();
            } else {
                Util.getIoWorkerExecutor().execute(this);
            }
            
            if(maxTicks != -1)
            {
                ticks = maxTicks;
            }
            else
            {
                ticks = -1;
            }
        }
        else
        {
            ticks --;
        }
    }

    @Override
    public abstract void run();
    
    public OARunnable runTaskTimerSync(int delay, int period)
    {
        ticks = delay;
        maxTicks = period;
        async = false;
        return this;
    }

    public OARunnable runTaskTimerAsync(int delay, int period)
    {
        ticks = delay;
        maxTicks = period;
        async = true;
        return this;
    }

    public void cancel()
    {
        ticks = -1;
        maxTicks = -1;
        instances.remove(this);
    }
}
