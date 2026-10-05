package com.eykcorp.clientes;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.slf4j.LoggerFactory;

/** Captura los eventos de log de una clase (nivel DEBUG) y restaura el logger al cerrar. */
public final class CapturaLogs implements AutoCloseable {

    private final Logger logger;
    private final Level nivelOriginal;
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    private CapturaLogs(Class<?> clase) {
        this.logger = (Logger) LoggerFactory.getLogger(clase);
        this.nivelOriginal = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    public static CapturaLogs de(Class<?> clase) {
        return new CapturaLogs(clase);
    }

    /** Mensajes formateados con su nivel, p. ej. {@code "INFO Cliente creado id=1"}. */
    public List<String> mensajes() {
        return appender.list.stream()
                .map(e -> e.getLevel() + " " + e.getFormattedMessage())
                .toList();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        logger.setLevel(nivelOriginal);
    }
}
