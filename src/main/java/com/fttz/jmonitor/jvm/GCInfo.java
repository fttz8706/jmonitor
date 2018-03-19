package com.fttz.jmonitor.jvm;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ListIterator;

/**
 * 类JVMGC.java的实现描述：获取gc的信息 <br>
 * youngGenCollectorNames = [ <br>
 * // Oracle (Sun) HotSpot <br>
 * // -XX:+UseSerialGC <br>
 * 'Copy', <br>
 * // -XX:+UseParNewGC <br>
 * 'ParNew', <br>
 * // -XX:+UseParallelGC <br>
 * 'PS Scavenge', <br>
 * <br>
 * // Oracle (BEA) JRockit <br>
 * // -XgcPrio:pausetime <br>
 * 'Garbage collection optimized for short pausetimes Young Collector', <br>
 * // -XgcPrio:throughput <br>
 * 'Garbage collection optimized for throughput Young Collector', <br>
 * // -XgcPrio:deterministic <br>
 * 'Garbage collection optimized for deterministic pausetimes Young Collector' <br>
 * ] <br>
 * <br>
 * oldGenCollectorNames = [ <br>
 * // Oracle (Sun) HotSpot <br>
 * // -XX:+UseSerialGC <br>
 * 'MarkSweepCompact', <br>
 * // -XX:+UseParallelGC and (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting) <br>
 * 'PS MarkSweep', <br>
 * // -XX:+UseConcMarkSweepGC <br>
 * 'ConcurrentMarkSweep', <br>
 * <br>
 * // Oracle (BEA) JRockit <br>
 * // -XgcPrio:pausetime <br>
 * 'Garbage collection optimized for short pausetimes Old Collector', <br>
 * // -XgcPrio:throughput <br>
 * 'Garbage collection optimized for throughput Old Collector', <br>
 * // -XgcPrio:deterministic <br>
 * 'Garbage collection optimized for deterministic pausetimes Old Collector' <br>
 * ]
 *
 * </p>
 *
 * @author fttz8706
 * @since 18/3/19
 */
public class GCInfo implements GCInfoMBean {

    private GarbageCollectorMXBean fullGC;
    private GarbageCollectorMXBean youngGC;
    private RuntimeMXBean runtime;


    private long lastYoungGCCollectionCount = -1;
    private long lastYoungGCCollectionTime = -1;
    private long lastFullGCCollectionCount = -1;
    private long lastFullGCCollectionTime = -1;
    private LastGcInfo ygc;
    private LastGcInfo fgc;
    // private GCProvider gcProvider = null;

    public GCInfo() {
        runtime = ManagementFactory.getRuntimeMXBean();

        // gcProvider = GCProvider.createGCProvider();
        for (ListIterator<GarbageCollectorMXBean> iter = ManagementFactory.getGarbageCollectorMXBeans().listIterator(); iter.hasNext(); ) {
            GarbageCollectorMXBean item = iter.next();
            if ("ConcurrentMarkSweep".equals(item.getName()) //
                    || "MarkSweepCompact".equals(item.getName()) //
                    || "PS MarkSweep".equals(item.getName()) //
                    || "G1 Old Generation".equals(item.getName()) //
                    || "Garbage collection optimized for short pausetimes Old Collector".equals(item.getName()) //
                    || "Garbage collection optimized for throughput Old Collector".equals(item.getName()) //
                    || "Garbage collection optimized for deterministic pausetimes Old Collector".equals(item.getName()) //
                    ) {
                fullGC = item;
            } else if ("ParNew".equals(item.getName()) //
                    || "Copy".equals(item.getName()) //
                    || "PS Scavenge".equals(item.getName()) //
                    || "G1 Young Generation".equals(item.getName()) //
                    || "Garbage collection optimized for short pausetimes Young Collector".equals(item.getName()) //
                    || "Garbage collection optimized for throughput Young Collector".equals(item.getName()) //
                    || "Garbage collection optimized for deterministic pausetimes Young Collector".equals(item.getName()) //
                    ) {
                youngGC = item;
            }
        }
        ygc = new LastGcInfo(0, 0);
        fgc = new LastGcInfo(0, 0);
    }

    class LastGcInfo {
        long count;
        long time;

        public LastGcInfo(long count, long time) {
            this.count = count;
            this.time = time;
        }

        String gc(long count) {
            long up = runtime.getStartTime();
            long tmp = time;
            if (count != this.count) {
                this.time = up;
                this.count = count;
            }
            return count + " " + ((tmp == 0) ? 0 : (up - tmp) / 1000);
        }
    }

    @Override
    public String getPrintInfo() {
        StringBuilder sb = new StringBuilder("gc ");
        //ygc总次数
        sb.append(ygc.gc(getYoungGCCollectionCount())).append(" ");
        //平均ygc耗时
        sb.append(getAvgYgct()).append(" ");
        //fgc总次数
        sb.append(fgc.gc(getFullGCCollectionCount())).append(" ");
        //平均每次fgc耗时
        sb.append(getAvgFgct()).append(" ");
        return sb.toString();
    }

    private long getAvgYgct() {
        if (getYoungGCCollectionCount() == 0) {
            return 0;
        }
        return getYoungGCCollectionTime() / getYoungGCCollectionCount();
    }

    private long getAvgFgct() {
        if (getFullGCCollectionCount() == 0) {
            return 0;
        }
        return getFullGCCollectionTime() / getFullGCCollectionCount();
    }


    public long getYoungGCCollectionCount() {
        if (youngGC == null) {
            return 0;
        }
        return youngGC.getCollectionCount();
    }

    public long getYoungGCCollectionTime() {
        if (youngGC == null) {
            return 0;
        }
        return youngGC.getCollectionTime();
    }

    public long getFullGCCollectionCount() {
        if (fullGC == null) {
            return 0;
        }
        return fullGC.getCollectionCount();
    }

    public long getFullGCCollectionTime() {
        if (fullGC == null) {
            return 0;
        }
        return fullGC.getCollectionTime();
    }

    public long getSpanYoungGCCollectionCount() {
        long current = getYoungGCCollectionCount();
        if (lastYoungGCCollectionCount == -1) {
            lastYoungGCCollectionCount = current;
            return 0;
        } else {
            long reslut = current - lastYoungGCCollectionCount;
            lastYoungGCCollectionCount = current;
            return reslut;
        }
    }

    public long getSpanYoungGCCollectionTime() {
        long current = getYoungGCCollectionTime();
        if (lastYoungGCCollectionTime == -1) {
            lastYoungGCCollectionTime = current;
            return 0;
        } else {
            long reslut = current - lastYoungGCCollectionTime;
            lastYoungGCCollectionTime = current;
            return reslut;
        }
    }

    public long getSpanFullGCCollectionCount() {
        long current = getFullGCCollectionCount();
        if (lastFullGCCollectionCount == -1) {
            lastFullGCCollectionCount = current;
            return 0;
        } else {
            long reslut = current - lastFullGCCollectionCount;
            lastFullGCCollectionCount = current;
            return reslut;
        }
    }

    public long getSpanFullGCCollectionTime() {
        long current = getFullGCCollectionTime();
        if (lastFullGCCollectionTime == -1) {
            lastFullGCCollectionTime = current;
            return 0;
        } else {
            long reslut = current - lastFullGCCollectionTime;
            lastFullGCCollectionTime = current;
            return reslut;
        }
    }
}
