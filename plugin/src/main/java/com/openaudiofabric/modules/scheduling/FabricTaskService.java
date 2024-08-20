package com.openaudiofabric.modules.scheduling;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.craftmend.openaudiomc.generic.platform.interfaces.TaskService;
import com.mojang.logging.LogUtils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

public class FabricTaskService implements TaskService {

    private static class Task {
        Runnable runnable;
        int timeToNextTick;
        int interval;
    }

    private static record SimpleMapEntry<K, V>(K key, V value) {

        public static <K, V> SimpleMapEntry<K, V> from(Map.Entry<? extends K, ? extends V> entry) {
            return new SimpleMapEntry<K,V>(entry.getKey(), entry.getValue());
        }
  
    }

    private final Map<Integer, Task> tasks = Collections.synchronizedMap(new HashMap<>());

    private final MinecraftServer server;

    public FabricTaskService(MinecraftServer server) {
        this.server = server;
    }

    public void tickTasks() {
        // Copy to array to avoid concurrent modification exception if a task tries to cancel itself.
        List<SimpleMapEntry<Integer, Task>> entries;
        synchronized(tasks) {
            entries = tasks.entrySet().stream().map(SimpleMapEntry::from).toList();
        }

        for (var entry : entries) {
            Task task = entry.value;
            if (task.timeToNextTick <= 0) {
                try {
                    task.runnable.run();
                } catch (Exception e) {
                    LogUtils.getLogger().error("Error executing scheduled task: ", e);
                } finally {
                    // Requeue if looping
                    if (task.interval > 0) {
                        task.timeToNextTick = task.interval;
                    } else {
                        tasks.remove(entry.key);
                    }
                }
            } else {
                task.timeToNextTick--;
            }
        }
    }

    @Override
    public int scheduleAsyncRepeatingTask(Runnable runnable, int delayUntilFirst, int tickInterval) {
        return scheduleAsyncRepeatingTask(() -> Util.getMainWorkerExecutor().submit(runnable), delayUntilFirst, tickInterval);
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable runnable, int delayUntilFirst, int tickInterval) {
        synchronized(tasks) {
            int id = tasks.size();
            Task task = new Task();
            task.runnable = runnable;
            task.timeToNextTick = delayUntilFirst;
            task.interval = tickInterval;
            return id;
        }
    }

    @Override
    public int schduleSyncDelayedTask(Runnable runnable, int delay) {
        return scheduleAsyncRepeatingTask(runnable, delay, -1);
    }

    @Override
    public void cancelRepeatingTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void runAsync(Runnable runnable) {
        Util.getMainWorkerExecutor().submit(runnable);
    }

    @Override
    public void runSync(Runnable runnable) {
        server.submit(runnable);
    }
    
    public void shutdown() {
        tasks.clear();
    }

    public MinecraftServer getServer() {
        return server;
    }
}
