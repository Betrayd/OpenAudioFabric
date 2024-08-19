package com.craftmend.openaudiomc.generic.logging;

import com.openaudiofabric.OpenAudioFabric;

/**
 * Class exists to cut work out of changing all log statements to use the new Logger
 */
public class OpenAudioLogger {

    //should not need this variable as we already have a debugger in the logger
    //private static boolean debugEnabled = false;

    /**
     * Log a regular information message
     * @param message the message to log
     */
    public static void info(String message) {
        OpenAudioFabric.LOGGER.info(message);
    }

    /**
     * Print debug information with the caller of this method (depth=1)
     * @param message the message to log
     */
    public static void debug(String message) {
        debug(message, 1);
    }

    /**
     * Print debug information with a custom depth for the caller. 0 would be the invocation, 1 its parent, etc
     * @param message the message to log
     * @param callerDepth the depth of the caller in the stack trace
     */
    public static void debug(String message, int callerDepth) {
        // get the class and method name of the caller
        String caller = Thread.currentThread().getStackTrace()[2 + callerDepth].getFileName();
        String method = Thread.currentThread().getStackTrace()[2 + callerDepth].getMethodName();
        OpenAudioFabric.LOGGER.debug("[" + caller + ":" + method + "] " +message);
    }

    /**
     * Log an error message with a throwable
     * @param e the throwable to log
     * @param message the message to log
     */
    public static void error(Throwable e, String message) {
        OpenAudioFabric.LOGGER.error(message, e);
    }

    /**
     * Write a warning message
     * @param message the message to log
     */
    public static void warn(String message) {
        OpenAudioFabric.LOGGER.warn(message);
    }

    /**
     * Enable or disable debug logging
     * @param enable true to enable, false to disable
     */
    /*public static void enableDebug(boolean enable) {
        debugEnabled = enable;
    }*/

    /**
     * Check if debug logging is enabled
     * @return true if debug logging is enabled
     */
    /*public static boolean isDebugEnabled() {
        return debugEnabled;
    }*/

}
