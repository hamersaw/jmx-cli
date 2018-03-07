package com.bushpath.jmx_cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.RunLast;

import java.util.List;

@Command(name = "main", version = "jmx-cli 0.1", subcommands = {ListCommand.class, MonitorCommand.class})
public class Main {
    @Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
    private boolean versionInfoRequested = false;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help messsage")
    private boolean usageHelpRequested = false;

    public static void main(String[] args) {
        Main main = new Main();
        CommandLine commandLine = new CommandLine(main);
        List<Object> result =
            commandLine.parseWithHandler(new RunLast(), System.err, args);
    }
}
