package com.bushpath.jmx_cli;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;;
import java.lang.management.MemoryUsage;;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServerConnection;

public class MemoryMonitor implements Monitor {
    protected final static int HEAP_USED = 0,
                               HEAP_COMMITTED = 1,
                               HEAP_MAX = 2,
                               NON_HEAP_USED = 3,
                               NON_HEAP_COMMITTED = 4,
                               NON_HEAP_MAX = 5;

    protected int averageCount;
    protected Map<Integer, List<Long>> map;

    public MemoryMonitor(int averageCount) {
        this.averageCount = averageCount;
        this.map = new HashMap<>();

        this.map.put(HEAP_USED, new ArrayList<>());
        this.map.put(HEAP_COMMITTED, new ArrayList<>());
        this.map.put(HEAP_MAX, new ArrayList<>());
        this.map.put(NON_HEAP_USED, new ArrayList<>());
        this.map.put(NON_HEAP_COMMITTED, new ArrayList<>());
        this.map.put(NON_HEAP_MAX, new ArrayList<>());
    }

    @Override
    public String getHeader() {
        return "heap_memory_used,heap_memory_committed,heap_memory_max"
            + ",non_heap_memory_used,non_heap_memory_committed,non_heap_memory_max";
    }

    @Override
    public String getStatistics(MBeanServerConnection mBeanServerConnection)
            throws Exception {
        // retrieve memory mxbean
        MemoryMXBean memoryMXBean = 
            ManagementFactory.newPlatformMXBeanProxy(
                mBeanServerConnection,
                ManagementFactory.MEMORY_MXBEAN_NAME,
                MemoryMXBean.class
            );

        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

        // update values
        this.updateMap(HEAP_USED, heapMemory.getUsed());
        this.updateMap(HEAP_COMMITTED, heapMemory.getCommitted());
        this.updateMap(HEAP_MAX, heapMemory.getMax());
        this.updateMap(NON_HEAP_USED, heapMemory.getUsed());
        this.updateMap(NON_HEAP_COMMITTED, heapMemory.getCommitted());
        this.updateMap(NON_HEAP_MAX, heapMemory.getMax());

        // return averages
        return this.getAverage(HEAP_USED)
            + "," + this.getAverage(HEAP_COMMITTED)
            + "," + this.getAverage(HEAP_MAX)
            + "," + this.getAverage(NON_HEAP_USED)
            + "," + this.getAverage(NON_HEAP_COMMITTED)
            + "," + this.getAverage(NON_HEAP_MAX);
    }

    protected void updateMap(int key, long value) {
        List<Long> values = this.map.get(key);
        values.add(value);
        
        while (values.size() > this.averageCount) {
            values.remove(0);
        }
    }

    protected long getAverage(int key) {
        List<Long> values = this.map.get(key);
        long average = 0;
        for (Long value : values) {
            average += value;
        }

        return average / values.size();
    }
}
