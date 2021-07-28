package tv.lid.microservices.logger.models;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


// класс модели записи в логе
public final class LogRecordModel {
    // множество уровней важности сообщения
    protected static enum Severity {
        UNKNOWN (0), // не определено
        DEBUG   (1), // отладка
        NORMAL  (2), // норма
        WARNING (3), // предупреждение
        ALERT   (4), // тревога
        PANIC   (5); // всё пропало

        private int severity;

        private Severity(final int severity) {
            this.severity = severity;
        }

        public int getValue() {
            return this.severity;
        }

        public static Severity valueOf(final int severity) {
            switch (severity) {
                case 5:
                    return Severity.PANIC;
                case 4:
                    return Severity.ALERT;
                case 3:
                    return Severity.WARNING;
                case 2:
                    return Severity.NORMAL;
                case 1:
                    return Severity.DEBUG;
                case 0:
                default:
                    return Severity.UNKNOWN;
            }
        }
    }

    // timestamp создания
    @JsonProperty(value = "timestamp", required = false)
    public final long timestamp;

    // уровень важности
    @JsonProperty(value = "severity", required = true)
    public final Severity severity;

    // текст сообщения
    @JsonProperty(value = "message", required = true)
    public final String message;

    // вычисляемое поле: дата и время создания в формате RFC 3339
    @JsonProperty(value = "datetime", required = false)
    public final String datetime() {
        return (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(new Date(this.timestamp));
    }

    // конструктор -- используется для создания экземпляра из входящего запроса
    @JsonCreator
    public LogRecordModel(
        @JsonProperty("severity") final Severity severity,
        @JsonProperty("message")  final String   message
    ) {
        this.timestamp = System.currentTimeMillis();
        this.severity  = severity;
        this.message   = message;
    }
}
