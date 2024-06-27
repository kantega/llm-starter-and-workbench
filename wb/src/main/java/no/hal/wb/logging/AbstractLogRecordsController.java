package no.hal.wb.logging;

import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.jboss.logmanager.formatters.Formatters;

abstract class AbstractLogRecordsController<C> {

    protected final C logRecordsControl;

    AbstractLogRecordsController(C logRecordsControl) {
        this.logRecordsControl = logRecordsControl;
    }

    private Formatter formatter = Formatters.nullFormatter();

    void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    Formatter getFormatter() {
        return formatter;
    }

    abstract void addLogRecords(List<LogRecord> newLogRecords);
}
