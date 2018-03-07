package com.bushpath.jmx_cli;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list", description = "list running java virtual machines")
public class ListVM implements Runnable {
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help messsage")
    private boolean usageHelpRequested = false;

    @Override
    public void run() {
        // print off all virtual machines
        for (VirtualMachineDescriptor virtualMachineDesc : VirtualMachine.list()) {
            System.out.println(virtualMachineDesc.id() + ":"
                + virtualMachineDesc.displayName());
        }
    }
}
