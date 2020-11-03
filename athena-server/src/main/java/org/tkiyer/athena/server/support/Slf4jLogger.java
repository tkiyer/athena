package org.tkiyer.athena.server.support;

import org.codehaus.plexus.logging.Logger;

public class Slf4jLogger implements Logger {

    private final org.slf4j.Logger logger;

    public Slf4jLogger(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void info(String message) {
        logger.info(message);
    }

    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void fatalError(String message) {
        logger.error(message);
    }

    public void fatalError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    public boolean isFatalErrorEnabled() {
        return logger.isErrorEnabled();
    }

    /**
     * <b>Warning</b>: ignored (always return <code>0 == Logger.LEVEL_DEBUG</code>).
     */
    public int getThreshold() {
        return 0;
    }

    /**
     * <b>Warning</b>: ignored.
     */
    public void setThreshold(int threshold) {
    }

    /**
     * <b>Warning</b>: ignored (always return <code>null</code>).
     */
    public Logger getChildLogger(String name) {
        return null;
    }

    public String getName() {
        return logger.getName();
    }
}
