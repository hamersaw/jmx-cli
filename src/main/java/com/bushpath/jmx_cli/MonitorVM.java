package com.bushpath.jmx_cli;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;;
import java.lang.management.MemoryUsage;;
import java.util.List;
import java.util.Properties;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

@Command(name = "monitor", description = "monitor a given java virtual machine")
public class MonitorVM implements Callable<Boolean> {
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help messsage")
    private boolean usageHelpRequested = false;

    @Option(names = {"-i", "--interval"}, description = "measurment interval (ms)")
    private int interval = 1000;

    @Parameters(paramLabel = "VM_ID", description = "list of virtual machine id")
    private String[] virtualMachineIds;

    @Override
    public Boolean call() throws Exception {
        System.out.println("timestamp"
                + ",heap_mem,heap_total_mem,heap_max_mem"
                + ",non_heap_mem,non_heap_total_mem,non_heap_max_mem");

        VirtualMachine virtualMachine = null;
        try {
            // connect to virtual machine
            virtualMachine = VirtualMachine.attach(this.virtualMachineIds[0]);

            Properties properties = virtualMachine.getSystemProperties();
            String javaHome = properties.getProperty("java.home");
            virtualMachine.loadAgent(javaHome + "/lib/management-agent.jar");

            String connectorAddress = virtualMachine.getAgentProperties()
                .getProperty("com.sun.management.jmxremote.localConnectorAddress");

            JMXConnector jmxConnector =
                JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));
            MBeanServerConnection mBeanServerConnection =
                jmxConnector.getMBeanServerConnection();

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        long timestamp = System.currentTimeMillis() / 1000;

                        // retrieve memory mxbean
                        MemoryMXBean memoryMXBean = 
                            ManagementFactory.newPlatformMXBeanProxy(
                                mBeanServerConnection,
                                ManagementFactory.MEMORY_MXBEAN_NAME,
                                MemoryMXBean.class
                            );

                        // print memory usage statistics
                        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
                        MemoryUsage nonHeapMemoryUsage =
                            memoryMXBean.getNonHeapMemoryUsage();

                        System.out.println(timestamp
                            + "," + heapMemoryUsage.getUsed()
                            + "," + heapMemoryUsage.getCommitted()
                            + "," + heapMemoryUsage.getMax()
                            + "," + nonHeapMemoryUsage.getUsed()
                            + "," + nonHeapMemoryUsage.getCommitted()
                            + "," + nonHeapMemoryUsage.getMax());

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }, 0, this.interval);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
