package com.fttz.jmonitor;

import com.fttz.jmonitor.jvm.GCInfo;
import com.fttz.jmonitor.jvm.MemoryInfo;
import com.fttz.jmonitor.jvm.ThreadInfo;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * </p>
 *
 * @author fttz8706
 * @since 18/3/19
 */
public class MonitorUtil {

    static volatile AtomicBoolean inited = new AtomicBoolean(false);
    static final String NAME_PREFIX = "com.fttz.monitor:type=";
    static MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    static List<ObjectName> OBJECT_NAMES = new ArrayList<ObjectName>();
    static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private static Logger LOGGER;

    static {
        ILoggerFactory loggerFactory = new LoggerFactoryBuilder().resource("logback.xml").build();
        LOGGER = loggerFactory.getLogger("jmonitor-logger");
    }

    //log服务器地址和端口
    static final String LOG_SERVER = "127.0.0.1";
    static final int LOG_SERVER_TCP_PORT = 7070;
    static Socket client = null;

    public static void register(Class<?> clazz) throws Exception {
        ObjectName name = new ObjectName(NAME_PREFIX + clazz.getName());
        clazz.newInstance();
        mBeanServer.registerMBean(clazz.newInstance(), name);
        OBJECT_NAMES.add(name);
    }


    public static void init() throws Exception {
        if (!inited.compareAndSet(false, true)) {
            return;
        }
        register(GCInfo.class);
        register(MemoryInfo.class);
        register(ThreadInfo.class);
        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    print();
                } finally {
                    service.schedule(this, 1, TimeUnit.MINUTES);
                }
            }
        });
    }

    public synchronized static void unregister() throws Exception {
        for (ObjectName name : OBJECT_NAMES) {
            mBeanServer.unregisterMBean(name);
        }
        OBJECT_NAMES.clear();
        inited.compareAndSet(true, false);
    }

    private static void print() {
        for (ObjectName name : OBJECT_NAMES) {
            try {
                Object object = mBeanServer.getAttribute(name, "PrintInfo");
                String printStr = (String) object;
                LOGGER.info(printStr);
                sendStrArray(formatLogStr(printStr), LOG_SERVER, LOG_SERVER_TCP_PORT);
            } catch (Exception e) {
                LOGGER.error("print error,", e);
            }
        }
    }

    private static String[] formatLogStr(String orignalStr) {
        String str[] = orignalStr.split(" ");
        String strRet[] = new String[str.length - 1];

        for (int i = 0; i < strRet.length; i++) {
            strRet[i] = getVlogKeyName(str[0], i + 1) + "\r\n" + str[i + 1] + "\r\n";
        }
        return strRet;
    }

    private static String getVlogKeyName(String name, int order) {
        final String gcName = "gc";
        final String memoryName = "memory";
        final String threadName = "thread";

        String keyName = "";

        if (gcName.equalsIgnoreCase(name)) {
            switch (order) {
                case 1:
                    keyName = name + "." + "ygc_count";
                    break;
                case 2:
                    keyName = name + "." + "ygc_time";
                    break;
                case 3:
                    keyName = name + "." + "ygc_avg_time";
                    break;
                case 4:
                    keyName = name + "." + "fgc_count";
                    break;
                case 5:
                    keyName = name + "." + "fgc_time";
                    break;
                case 6:
                    keyName = name + "." + "fgc_avg_time";
                    break;
                default:
                    keyName = name + "." + "gc";
                    break;

            }

        } else if (memoryName.equalsIgnoreCase(name)) {
            switch (order) {
                case 1:
                    keyName = name + "." + "heap";
                    break;
                case 2:
                    keyName = name + "." + "non_heap";
                    break;
                case 3:
                    keyName = name + "." + "code_cache";
                    break;
                default:
                    keyName = name + "." + "memory";
                    break;

            }
        } else if (threadName.equalsIgnoreCase(name)) {
            switch (order) {
                case 1:
                    keyName = name + "." + "daemon_count";
                    break;
                case 2:
                    keyName = name + "." + "count";
                    break;
                case 3:
                    keyName = name + "." + "total_started_count";
                    break;
                case 4:
                    keyName = name + "." + "dead_locked_count";
                    break;
                default:
                    keyName = name + "." + "thread";
                    break;

            }
        }

        return keyName;

    }

    private static void sendStrArray(String[] strArray, final String serverAddr, final int port) {
        for (int i = 0; i < strArray.length; i++) {
            try {
                sentDataToVlogServer(strArray[i]);
            } catch (IOException e) {
                LOGGER.error("send error,", e);
            }
        }

    }

    private static Socket getSinglonSocket() {
        if (null == client || client.isClosed()) {
            try {
                client = new Socket(LOG_SERVER, LOG_SERVER_TCP_PORT);
            } catch (UnknownHostException e) {
                LOGGER.error("socket error,", e);
            } catch (IOException e) {
                LOGGER.error("socket error,", e);
            }
        }
        return client;
    }

    private static void sentDataToVlogServer(final String sendStr) throws IOException {
        Socket client = getSinglonSocket();
        if (null != client && client.isConnected()) {
            OutputStream os = client.getOutputStream();
            os.write(sendStr.getBytes());
            os.close();
        }
    }
}
