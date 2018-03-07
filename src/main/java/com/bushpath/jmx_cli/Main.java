package com.bushpath.jmx_cli;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;;
import java.lang.management.MemoryUsage;;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Main {
    public static void main(String[] args) {
        // print off all virtual machines
        for (VirtualMachineDescriptor virtualMachineDesc : VirtualMachine.list()) {
            System.out.println(virtualMachineDesc.id() + ":"
                + virtualMachineDesc.displayName());
        }

        VirtualMachine virtualMachine = null;
        try {
            // connect to virtual machine
            virtualMachine = VirtualMachine.attach(args[0]);

            Properties properties = virtualMachine.getSystemProperties();
            String javaHome = properties.getProperty("java.home");
            virtualMachine.loadAgent(javaHome + "/lib/management-agent.jar");

            String connectorAddress = virtualMachine.getAgentProperties()
                .getProperty("com.sun.management.jmxremote.localConnectorAddress");

            JMXConnector jmxConnector =
                JMXConnectorFactory.connect(new JMXServiceURL(connectorAddress));
            MBeanServerConnection mBeanServerConnection =
                jmxConnector.getMBeanServerConnection();

            // retrieve memory mxbean
            MemoryMXBean memoryMXBean =
                ManagementFactory.newPlatformMXBeanProxy(mBeanServerConnection,
                    ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);

            // print memory usage statistics
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            System.out.println("HEAP_MEM: " + heapMemoryUsage.getUsed());
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            System.out.println("NON_HEAP_MEM: " + nonHeapMemoryUsage.getUsed());
        } catch (AgentInitializationException | AgentLoadException
                | AttachNotSupportedException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}