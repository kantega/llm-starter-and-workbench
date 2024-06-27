package no.kantega.llm.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.jboss.logmanager.formatters.Formatters;

import jakarta.enterprise.context.ApplicationScoped;
import javafx.application.Platform;

// https://stackoverflow.com/questions/76488986/how-to-display-log-in-javafx-textarea

@ApplicationScoped
public class AppLoggingHandler extends Handler {

    public AppLoggingHandler() {
        setFormatter(Formatters.nullFormatter());
    }
    

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            System.out.println(">>> publishing " + getFormatter().formatMessage(record));
            if (Platform.isFxApplicationThread()) {
                ///
            } else {
                Platform.runLater(() -> publish(record));
            }
        }
    }
    
    @Override
    public void flush() {
        System.out.println(">>> flushing ");
    }
    
    @Override
    public void close() throws SecurityException {
        System.out.println(">>> closing ");
    }
}
