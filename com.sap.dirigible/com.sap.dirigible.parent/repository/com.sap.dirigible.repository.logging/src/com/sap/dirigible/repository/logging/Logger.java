package com.sap.dirigible.repository.logging;

import java.util.logging.Level;

import org.slf4j.LoggerFactory;

/**
 * This is the central class used for logging in the Dirigible project.
 * <p>
 * This class works as a proxy to a real logging library by delegating calls to
 * it. The idea is that if at any moment a new logging library is used,
 * modifications would be confined only to this class.
 * 
 */
public class Logger {

	/**
	 * System property for disabling logging with DEBUG, WARN, INFO and TRACE levels
	 */
	private static final String DISABLE_LOGGING = "disable.logging";
	private static final boolean LOGGING_DISABLED = isLoggingDisabled();

	private static boolean isLoggingDisabled() {
		String disableLogging = System.getProperty(DISABLE_LOGGING);
		return (disableLogging != null) && (Boolean.valueOf(disableLogging));
	}

	protected boolean printInSystemOutput = false;

	/**
	 * Returns a {@link Logger} instance that is bound to the specified name.
	 * <p>
	 * Usually the <code>name</code> parameter should represent the name of the
	 * class that operates on the instance.
	 */
	public static Logger getLogger(String name) {
		return new Logger(LoggerFactory.getLogger(name), java.util.logging.Logger.getLogger(name));
	}

	/**
	 * Shorthand for getLogger(clazz.getName()).
	 */
	public static Logger getLogger(Class<?> clazz) {
		return new Logger(LoggerFactory.getLogger(clazz), java.util.logging.Logger.getLogger(clazz
				.getCanonicalName()));
	}

	private final org.slf4j.Logger logger1;
	private final java.util.logging.Logger logger2;

	private Logger(org.slf4j.Logger logger1, java.util.logging.Logger logger2) {
		this.logger1 = logger1;
		this.logger2 = logger2;
	}

	/**
	 * Log an error that might still allow the application to continue to
	 * operate.
	 */
	public void error(String message) {
		error(message, null);
	}

	/**
	 * Same as {@link #error(String)} with the ability to specify an exception
	 * that caused the logging.
	 */
	public void error(String message, Throwable t) {
		if (isErrorEnabled()) {
			logger1.error(message, t);
			logger2.log(Level.SEVERE, message, t);
		}
		logInSystemOutput(message, t);
	}

	/**
	 * Log a potentially harmful situation.
	 */
	public void warn(String message) {
		warn(message, null);
	}

	/**
	 * Same as {@link #warn(String)} with the ability to specify an exception
	 * that caused the logging.
	 */
	public void warn(String message, Throwable t) {
		if (isWarnEnabled()) {
			logger1.warn(message, t);
			logger2.log(Level.WARNING, message, t);
		}
		logInSystemOutput(message, t);
	}

	/**
	 * Log an event in the application or progress.
	 */
	public void info(String message) {
		info(message, null);
	}

	/**
	 * Same as {@link #info(String)} with the ability to specify an exception
	 * that caused the logging.
	 */
	public void info(String message, Throwable t) {
		if (isInfoEnabled()) {
			logger1.info(message, t);
			logger2.log(Level.INFO, message, t);
		}
		logInSystemOutput(message, t);
	}

	/**
	 * Log fine-grained information events.
	 */
	public void debug(String message) {
		debug(message, null);
	}

	/**
	 * Same as {@link #debug(String)} with the ability to specify an exception
	 * that caused the logging.
	 */
	public void debug(String message, Throwable t) {
		if (isDebugEnabled()) {
			logger1.debug(message, t);
			logger2.log(Level.FINE, message, t);
		}
		logInSystemOutput(message, t);
	}

	/**
	 * Log the most fine-grained events.
	 */
	public void trace(String message) {
		trace(message, null);
	}

	/**
	 * Same as {@link #trace(String)} with the ability to specify an exception
	 * that caused the logging.
	 */
	public void trace(String message, Throwable t) {
		if (isTraceEnabled()) {
			logger1.trace(message, t);
			logger2.log(Level.FINE, message, t);
		}
		logInSystemOutput(message, t);
	}

	/**
	 * Is the logger instance enabled for the ERROR level?
	 * 
	 * @return True if this Logger is enabled for the ERROR level, false
	 *         otherwise.
	 */
	public boolean isErrorEnabled() {
		return logger1.isErrorEnabled() || logger2.isLoggable(Level.SEVERE);
	}

	/**
	 * Is the logger instance enabled for the DEBUG level?
	 * 
	 * @return True if this Logger is enabled for the DEBUG level, false
	 *         otherwise.
	 */
	public boolean isDebugEnabled() {
		return (!LOGGING_DISABLED) && (logger1.isDebugEnabled() || logger2.isLoggable(Level.FINE));
	}

	/**
	 * Is the logger instance enabled for the WARN level?
	 * 
	 * @return True if this Logger is enabled for the WARN level, false
	 *         otherwise.
	 */
	public boolean isWarnEnabled() {
		return (!LOGGING_DISABLED) && (logger1.isWarnEnabled() || logger2.isLoggable(Level.WARNING));
	}
	
	/**
	 * Is the logger instance enabled for the INFO level?
	 * 
	 * @return True if this Logger is enabled for the INFO level, false
	 *         otherwise.
	 */
	public boolean isInfoEnabled() {
		return (!LOGGING_DISABLED) && (logger1.isInfoEnabled() || logger2.isLoggable(Level.INFO));
	}
	
	/**
	 * Is the logger instance enabled for the TRACE level?
	 * 
	 * @return True if this Logger is enabled for the TRACE level, false
	 *         otherwise.
	 */
	public boolean isTraceEnabled() {
		return (!LOGGING_DISABLED) && (logger1.isTraceEnabled() || logger2.isLoggable(Level.FINE));
	}

	private void logInSystemOutput(String message, Throwable t) {
		if (printInSystemOutput) {
			System.err.println(message);
			if (t != null) {
				t.printStackTrace();
			}
		}
	}
}