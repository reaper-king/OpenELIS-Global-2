package org.openelisglobal.common.servlet.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.plugin.PluginLoader;
import org.openelisglobal.scheduler.IndependentThreadStarter;
import org.openelisglobal.scheduler.LateStartScheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@WebListener
public final class StartStopListener implements ServletContextListener {

    private ServletContext context = null;

    @Autowired
    private IndependentThreadStarter threadStarter;
    @Autowired
    private LateStartScheduler lateStartScheduler;

    public StartStopListener() {

    }

    // This method is invoked when the Web Application
    // has been removed and is no longer able to accept
    // requests

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        context = null;
        if (threadStarter != null) {
            threadStarter.stopThreads();
        }
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        try {
            lateStartScheduler.shutdown();
        } catch (SchedulerException e) {
            LogEvent.logDebug(e);
            // rethrow as a runtime exception
            throw new IllegalStateException(e);
        }

        LogEvent.logInfo(this.getClass().getName(), "method unkown", "\nShutting down context\n");
    }

    // This method is invoked when the Web Application
    // is ready to service requests

    @Override
    public void contextInitialized(ServletContextEvent event) {
        context = event.getServletContext();

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        try {
            lateStartScheduler.checkAndStartScheduler();
        } catch (RuntimeException e) {
            // rethrow as a runtime exception
            throw new IllegalStateException(e);
        }

        LogEvent.logInfo(this.getClass().getName(), "method unkown", "Scheduler started");

        threadStarter.startThreads();

        LogEvent.logInfo(this.getClass().getName(), "method unkown", "Threads started");
        new PluginLoader(event).load();
        LogEvent.logInfo(this.getClass().getName(), "method unkown", "Plugins loaded");
    }
}
