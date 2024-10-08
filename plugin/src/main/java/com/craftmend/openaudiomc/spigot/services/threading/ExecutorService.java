package com.craftmend.openaudiomc.spigot.services.threading;

import com.craftmend.openaudiomc.generic.service.Inject;
import com.craftmend.openaudiomc.generic.service.Service;
import com.craftmend.openaudiomc.generic.utils.CustomPayloadOARunnable;
import com.craftmend.openaudiomc.spigot.OpenAudioMcSpigot;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

// TODO: Implement the show system to use _this_ instead of regular java timers.

@NoArgsConstructor
public class ExecutorService extends Service {

    @Inject
    private OpenAudioMcSpigot plugin;

    @Getter private Executor executor;
    @Getter private final Queue<Runnable> tickRunnables = new ConcurrentLinkedQueue<>();
    @Getter private final Queue<Runnable> secondRunnables = new ConcurrentLinkedQueue<>();
    @Getter private Queue<Runnable> runNextTick = new ConcurrentLinkedQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    @Getter private final Queue<Runnable> nextTickFallback = new ConcurrentLinkedQueue<>();
    private int tick = 0;
    @Getter private Instant lastPing = Instant.now();

    @Override
    public void onEnable() {
        boot();

        new CustomPayloadOARunnable(() -> {executor.tickSync();}).runTaskTimerSync(1,1);
        new CustomPayloadOARunnable(() -> {
            if (Duration.between(lastPing, Instant.now()).toMillis() > 10000) {
                executor.stop();
                executor = null;
                boot();
            }
        }).runTaskTimerSync(80, 80);
    }

    private void boot() {
        executor = new Executor(0, 50, () -> {
            lock.lock();
            for (Runnable runnable : tickRunnables) {
                runnable.run();
            }

            tick++;
            if (tick >= 50) {
                for (Runnable secondRunnable : secondRunnables) {
                    secondRunnable.run();
                }
                tick = 0;
            }

            for (Runnable runnable : runNextTick) {
                runnable.run();
            }

            runNextTick.clear();
            lastPing = Instant.now();

            lock.unlock();
            runNextTick = nextTickFallback;
            nextTickFallback.clear();
        });

        executor.start();
    }

}
