package no.hal.wb.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.formatters.PatternFormatter;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

// https://stackoverflow.com/questions/76488986/how-to-display-log-in-javafx-textarea

@ApplicationScoped
public class AppLoggingHandler extends Handler {

    @ConfigProperty(name = "wb.log.format")
    String formatPattern;
    
    @PostConstruct
    void init() {
        setFormatter(new PatternFormatter(formatPattern));
    }

    private LogRecordsController logRecordsController;

    public void setLogRecordsController(LogRecordsController logRecordsController) {
        this.logRecordsController = logRecordsController;
        logRecordsController.setFormatter(getFormatter());
    }

    @Override
    public void publish(LogRecord logRecord) {
        if (isLoggable(logRecord)) {
            if (logRecordsController != null) {
                logRecordsController.addLogRecord(logRecord);
            }
        }
    }
    
    @Override
    public void flush() {
        // ignore
    }
    
    @Override
    public void close() throws SecurityException {
        logRecordsController = null;
    }
}
