package com.fttz.jmonitor.jvm;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * </p>
 *
 * @author fttz8706
 * @since 18/3/19
 */
public class ThreadInfo implements ThreadInfoMBean {


    private volatile long lastCPUTime;
    private volatile long lastCPUUpTime;
    private OperatingSystemMXBean operatingSystem;
    private RuntimeMXBean runtime;

    private static ThreadInfo instance = new ThreadInfo();

    public static ThreadInfo getInstance() {
        return instance;
    }

    private ThreadMXBean threadMXBean;

    public ThreadInfo() {
        threadMXBean = ManagementFactory.getThreadMXBean();
        operatingSystem = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        runtime = ManagementFactory.getRuntimeMXBean();

        try {
            lastCPUTime = operatingSystem.getProcessCpuTime();
        } catch (Exception e) {
//            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public String getPrintInfo() {
        StringBuilder sb = new StringBuilder("thread ");
        sb.append(getDaemonThreadCount()).append(" ");
        sb.append(getThreadCount()).append(" ");
        sb.append(getTotalStartedThreadCount()).append(" ");
        sb.append(getDeadLockedThreadCount()).append(" ");
        return sb.toString();
    }

    public BigDecimal getProcessCpuTimeRate() {
        long cpuTime = operatingSystem.getProcessCpuTime();
        long upTime = runtime.getUptime();

        long elapsedCpu = cpuTime - lastCPUTime;
        long elapsedTime = upTime - lastCPUUpTime;

        lastCPUTime = cpuTime;
        lastCPUUpTime = upTime;

        BigDecimal cpuRate;
        if (elapsedTime <= 0) {
            return new BigDecimal(0);
        }

        float cpuUsage = elapsedCpu / (elapsedTime * 10000F);
        cpuRate = new BigDecimal(cpuUsage, new MathContext(4));

        return cpuRate;
    }

    public int getDaemonThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }

    public int getThreadCount() {
        return threadMXBean.getThreadCount();
    }

    public long getTotalStartedThreadCount() {
        return threadMXBean.getTotalStartedThreadCount();
    }

    public int getDeadLockedThreadCount() {
        try {
            long[] deadLockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadLockedThreadIds == null) {
                return 0;
            }
            return deadLockedThreadIds.length;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
