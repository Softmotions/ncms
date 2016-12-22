package com.softmotions.ncms.js;


import org.slf4j.Logger;

import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.LightweightMessageFormatter;
import com.google.javascript.jscomp.MessageFormatter;
import com.google.javascript.jscomp.parsing.parser.util.format.SimpleFormat;

/**
 * Based on {@link com.google.javascript.jscomp.LoggerErrorManager}
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class ClosureLoggerErrorManager extends BasicErrorManager {
    private final MessageFormatter formatter;
    private final Logger logger;

    /**
     * Creates an instance.
     */
    public ClosureLoggerErrorManager(MessageFormatter formatter, Logger logger) {
        this.formatter = formatter;
        this.logger = logger;
    }

    /**
     * Creates an instance with a source-less error formatter.
     */
    public ClosureLoggerErrorManager(Logger logger) {
        this(LightweightMessageFormatter.withoutSource(), logger);
    }

    @Override
    public void println(CheckLevel level, JSError error) {
        switch (level) {
            case ERROR:
                logger.error(error.format(level, formatter));
                break;
            case WARNING:
                logger.warn(error.format(level, formatter));
                break;
            case OFF:
                break;
        }
    }

    @Override
    protected void printSummary() {
        String msg = null;
        if (getTypedPercent() > 0.0) {
            msg = SimpleFormat.format(
                    "%d error(s), %d warning(s), %.1f%% typed",
                    getErrorCount(), getWarningCount(), getTypedPercent());
        } else if (getErrorCount() + getWarningCount() > 0) {
            msg = SimpleFormat.format(
                    "%d error(s), %d warning(s)",
                    getErrorCount(), getWarningCount());
        }
        if (msg != null) {
            logger.warn(msg);
        }
    }
}