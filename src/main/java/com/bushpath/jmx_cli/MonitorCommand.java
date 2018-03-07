package com.bushpath.jmx_cli;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

@Command(name = "monitor", description = "monitor a given java virtual machine")
public class MonitorCommand implements Callable<Boolean> {
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help messsage")
    private boolean usageHelpRequested = false;

    @Option(names = {"-a", "--average"}, description = "number of measurements to average")
    private int averageCount = 1;

    @Option(names = {"-i", "--interval"}, description = "measurment interval (ms)")
    private int interval = 1000;

    @Parameters(paramLabel = "VM_ID", description = "list of virtual machine id")
    private String[] virtualMachineIds;

    @Override
    public Boolean call() throws Exception {
        List<Monitor> monitors = new ArrayList();
        monitors.add(new MemoryMonitor(this.averageCount));

        // print header
        StringBuilder header = new StringBuilder("timestamp");
        for (Monitor monitor : monitors) {
            header.append("," + monitor.getHeader());
        }
        System.out.println(header);

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

                        StringBuilder line =
                            new StringBuilder(Long.toString(timestamp));
                        for (Monitor monitor : monitors) {
                            line.append("," + monitor.getStatistics(mBeanServerConnection));
                        }
                        System.out.println(line);
                    } catch (Exception e) {
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
