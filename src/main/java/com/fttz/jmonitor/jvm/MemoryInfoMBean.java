package com.fttz.jmonitor.jvm;

/**
 * 内存信息
 * </p>
 *
 * @author fttz8706
 * @since 18/3/19
 */
public interface MemoryInfoMBean {

    String getPrintInfo();

    // Heap
    long getHeapMemoryCommitted();

    long getHeapMemoryInit();

    long getHeapMemoryMax();

    long getHeapMemoryUsed();

    // NonHeap
    long getNonHeapMemoryCommitted();

    long getNonHeapMemoryInit();

    long getNonHeapMemoryMax();

    long getNonHeapMemoryUsed();

    // PermGen
    long getPermGenCommitted();

    long getPermGenInit();

    long getPermGenMax();

    long getPermGenUsed();

    // OldGen
    long getOldGenCommitted();

    long getOldGenInit();

    long getOldGenMax();

    long getOldGenUsed();

    // EdenSpace
    long getEdenSpaceCommitted();

    long getEdenSpaceInit();

    long getEdenSpaceMax();

    long getEdenSpaceUsed();

    // Survivor
    long getSurvivorCommitted();

    long getSurvivorInit();

    long getSurvivorMax();

    long getSurvivorUsed();
}
