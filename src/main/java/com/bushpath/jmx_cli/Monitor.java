package com.bushpath.jmx_cli;

import javax.management.MBeanServerConnection;

public interface Monitor {
    public String getHeader();
    public String getStatistics(MBeanServerConnection mBeanServerConnection)
        throws Exception;
}
