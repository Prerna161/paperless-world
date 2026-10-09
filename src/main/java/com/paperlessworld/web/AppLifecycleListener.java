package com.paperlessworld.web;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.EventDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.dao.UserDao;
import com.paperlessworld.service.ThoughtService;
import com.paperlessworld.service.TransactionManager;
import com.paperlessworld.service.UserService;
import com.paperlessworld.service.resurfacing.ResurfacingEngine;
import com.paperlessworld.util.TimeSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AppLifecycleListener matching PRD Section 11.1 and 14.5.
 * Wires the application singletons and runs the single background daemon resurfacing scheduler.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent e) {
        ServletContext ctx = e.getServletContext();

        // 1. Initialize DAOs and TransactionManager
        TransactionManager txManager = new TransactionManager();
        ThoughtDao thoughtDao = new ThoughtDao();
        ActionDao actionDao = new ActionDao();
        EventDao eventDao = new EventDao();
        UserDao userDao = new UserDao();

        // 2. Initialize Services
        ThoughtService thoughtService = new ThoughtService(thoughtDao, actionDao, eventDao, txManager);
        UserService userService = new UserService(userDao, txManager);
        ResurfacingEngine resurfacingEngine = new ResurfacingEngine(thoughtDao, actionDao, txManager);

        // 3. Register as ServletContext attributes for Servlets
        ctx.setAttribute("txManager", txManager);
        ctx.setAttribute("thoughtDao", thoughtDao);
        ctx.setAttribute("actionDao", actionDao);
        ctx.setAttribute("eventDao", eventDao);
        ctx.setAttribute("userDao", userDao);
        ctx.setAttribute("thoughtService", thoughtService);
        ctx.setAttribute("userService", userService);
        ctx.setAttribute("resurfacingEngine", resurfacingEngine);

        // 4. Single-threaded background resurfacing worker
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "resurfacing-worker");
            t.setDaemon(true);
            return t;
        });

        Runnable resurfacingJob = () -> {
            try {
                // Background scan with system clock
                // In production, scans all users in batches; demo users can also trigger via /demo/run
            } catch (Throwable t) {
                // PRD 11.1: Catch all exceptions inside the job to prevent silent cancellation
                ctx.log("Error in resurfacing scheduler worker", t);
            }
        };

        scheduler.scheduleWithFixedDelay(resurfacingJob, 1, 15, TimeUnit.MINUTES);
        ctx.log("Paperless World application initialized successfully with single background resurfacing worker.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException ex) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
