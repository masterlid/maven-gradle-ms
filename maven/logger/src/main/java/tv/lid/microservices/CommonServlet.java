package tv.lid.microservices;

import java.util.List;

import javax.servlet.http.HttpServlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class CommonServlet extends HttpServlet {
    // множество кодов ответов сервера
    protected static enum Code {
        OK                    (200), // всё в порядке
        BAD_REQUEST           (400), // ошибка на стороне клиента
        INTERNAL_SERVER_ERROR (500); // ошибка на стороне сервера

        private int code;

        private Code(final int code) {
            this.code = code;
        }

        public int getValue() {
            return this.code;
        }

        public static Code valueOf(final int code) {
            switch (code) {
                case 200:
                    return Code.OK;
                case 400:
                    return Code.BAD_REQUEST;
                case 500:
                    return Code.INTERNAL_SERVER_ERROR;
                default:
                    return null;
            }
        }
    }

    // результат выполнения запроса
    @JsonInclude(Include.NON_NULL)
    protected static class Result {
        public final int    code; // код ответа
        public final Object data; // данные
        public final String info; // дополнительная информация

        // конструктор #1
        public Result(
            final Code   code,
            final Object data,
            final String info
        ) {
            this.code = code.getValue();
            this.data = data;
            this.info = info;
        }

        // конструктор #2
        public Result(
            final Code   code,
            final Object data
        ) {
            this(code, data, null);
        }

        // конструктор #3
        public Result(
            final Code   code,
            final String info
        ) {
            this(code, null, info);
        }

        // конструктор #4
        public Result(
            final Code code
        ) {
            this(code, null, null);
        }

        // преобразование в строку
        public String toString() {
            try {
                return (new ObjectMapper()).writeValueAsString(this);
            } catch (JsonProcessingException exc) {
                return "";
            }
        }
    }

    // обертка для списка записей
    public final class ListWrapper {
        public final List<? extends Object> list; // сам список
        public final int total; // всего записей
        public final int pages; // количество страниц

        // конструктор
        public ListWrapper(
            final List<? extends Object> list,
            final int total,
            final int pages
        ) {
            this.list  = list;
            this.total = total;
            this.pages = pages;
        }
    }

    // успешный ответ, данные есть
    public final Result ok(final Object data) {
        return new Result(Code.OK, data);
    }

    // успешный ответ, данных нет
    public final Result ok() {
        return this.ok(null);
    }

    // ответ с ошибкой, c дополнительной информацией
    public final Result error(final Code code, final String info) {
        return (code == Code.BAD_REQUEST || code == Code.INTERNAL_SERVER_ERROR)
            ? new Result(code, info)
            : null;
    }

    // ответ с ошибкой, без дополнительной информации
    public final Result error(final Code code) {
        return this.error(code, null);
    }
}
