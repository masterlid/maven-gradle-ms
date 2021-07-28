package tv.lid.microservices.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tv.lid.microservices.CommonServlet;
import tv.lid.microservices.logger.models.LogRecordModel;

@WebServlet("/logger/*")
public class LoggerServlet extends CommonServlet {
    private final String LOGGER_DATA_KEY  = "logger"; // ключ для хранения списка записей

    private final int DEFAULT_COUNT_VALUE = 10; // дефолтное количество сообщений на одной странице
    private final int DEFAULT_PAGE_VALUE  = 1;  // дефолтный номер страницы

    // вывести список записей по заданным параметрам
    @Override
    @SuppressWarnings("unchecked")
    public void doGet(
        final HttpServletRequest  req,
        final HttpServletResponse rsp
    ) throws ServletException, IOException {
        // задаём MIME-тип выходных данных
        rsp.setContentType("application/json");

        // количество сообщений на страницу и номер страницы
        int count = DEFAULT_COUNT_VALUE,
            page  = DEFAULT_PAGE_VALUE;

        // парсим path из URL
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            pathInfo = pathInfo.substring(1); // убираем символ '/' в начале строки
            final int hyphenPos = pathInfo.indexOf('-');
            if (hyphenPos > 0 && hyphenPos < (pathInfo.length() - 1)) {
                try {
                    // парсим количество сообщений на одну страницу
                    count = Integer.parseInt(pathInfo.substring(0, hyphenPos));

                    // парсим номер страницы
                    page = Integer.parseInt(pathInfo.substring(hyphenPos + 1));
                } catch (Exception exc) {}
            }
        }

        // запрашиваем список записей из атрибута сервлета
        ArrayList<LogRecordModel> list;
        final Object attrValue = this.getServletContext().getAttribute(LOGGER_DATA_KEY);
        if (attrValue == null) {
            list = new ArrayList<LogRecordModel>();
            this.getServletContext().setAttribute(LOGGER_DATA_KEY, list);
        } else {
            list = (ArrayList<LogRecordModel>) attrValue;
        }

        // считаем общее количество записей и количество страниц
        final int total = list.size();
        final int pages = (int) Math.ceil((float) total / count);

        // вырезаем нужную часть списка в соответствии с заданной страницей
        List<LogRecordModel> subList;
        try {
            if (total == 0) {
                throw new Exception();
            }
            final int from = page > 1 ? (page - 1) * count : 0;
            final int to   = (from + count) < total ? (from + count) : total;
            subList = list.subList(from, to);
        } catch (Exception exc) {
            subList = new ArrayList<LogRecordModel>();
        }

        // отправляем готовый результат клиенту
        rsp.getWriter().println(
            ok(new ListWrapper(subList, total, pages))
        );
    }

    // сохранить новую запись в логе
    @Override
    @SuppressWarnings("unchecked")
    public void doPost(
        final HttpServletRequest  req,
        final HttpServletResponse rsp
    ) throws ServletException, IOException {
        // задаём MIME-тип выходных данных
        rsp.setContentType("application/json");

        // создаём writer для отправки данных клиенту
        final PrintWriter writer = rsp.getWriter();

        // читаем строку с входными данными
        String data;
        try {
            final BufferedReader reader = req.getReader();
            final StringBuffer    buff  = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) {
                buff.append(line);
            }
            reader.close();

            data = buff.toString();
        } catch (IOException exc) {
            writer.println(error(Code.BAD_REQUEST, "Unable to read the incoming log data!"));
            return;
        }

        // преобразовываем полученную строку в экземпляр записи в логе
        LogRecordModel record;
        try {
            record = (new ObjectMapper()).readValue(data, LogRecordModel.class);
        } catch (JsonProcessingException exc) {
            writer.println(error(Code.BAD_REQUEST, "Unable to parse the incoming log data!"));
            return;
        }

        // добавляем новую запись в начало списка
        ArrayList<LogRecordModel> list;
        final Object attrValue = this.getServletContext().getAttribute(LOGGER_DATA_KEY);
        if (attrValue == null) {
            list = new ArrayList<LogRecordModel>();
            this.getServletContext().setAttribute(LOGGER_DATA_KEY, list);
        } else {
            list = (ArrayList<LogRecordModel>) attrValue;
        }
        list.add(0, record);

        // отправляем клиенту сообщение с готовой записью в логе
        writer.println(ok(record));
    }
}
