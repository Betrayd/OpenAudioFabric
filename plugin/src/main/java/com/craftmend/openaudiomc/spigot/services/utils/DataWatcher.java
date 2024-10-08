package com.craftmend.openaudiomc.spigot.services.utils;

import com.craftmend.openaudiomc.generic.utils.CustomPayloadOARunnable;
import com.craftmend.openaudiomc.generic.utils.OARunnable;
import com.craftmend.openaudiomc.spigot.services.utils.interfaces.Feeder;
import lombok.Getter;

import java.util.function.Consumer;

public class DataWatcher<T> {

    private T value;
    private final OARunnable task;
    private Feeder<T> dataFeeder;
    @Getter private Consumer<T> callback;
    private boolean isRunning;
    private boolean forced = false;

    public DataWatcher(boolean sync, int delayTicks) {
        Runnable executor = () -> {
            if (this.dataFeeder == null) return;
            T newValue = dataFeeder.feed();
            if (forced || (this.value != null && !newValue.equals(this.value))) this.callback.accept(newValue);
            this.value = newValue;
            forced = false;
        };

        if (sync) {
            this.task = new CustomPayloadOARunnable(executor).runTaskTimerSync(delayTicks, delayTicks);
        } else {
            this.task = new CustomPayloadOARunnable(executor).runTaskTimerAsync(delayTicks, delayTicks);
        }

        isRunning = true;
    }

    public DataWatcher<T> setFeeder(Feeder<T> feeder) {
        this.dataFeeder = feeder;
        return this;
    }

    public DataWatcher<T> setTask(Consumer<T> task) {
        this.callback = task;
        return this;
    }

    public void forceTicK() {
        this.forced = true;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void stop() {
        this.task.cancel();
        this.isRunning = false;
    }

}
