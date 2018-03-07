package com.bushpath.jmx_cli;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "list", description = "list running java virtual machines")
public class ListVM implements Callable<Boolean> {
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help messsage")
    private boolean usageHelpRequested = false;

    @Override
    public Boolean call() throws Exception {
        // print off all virtual machines
        for (VirtualMachineDescriptor virtualMachineDesc : VirtualMachine.list()) {
            System.out.println(virtualMachineDesc.id() + " : "
                + virtualMachineDesc.displayName());
        }

        return true;
    }
}
