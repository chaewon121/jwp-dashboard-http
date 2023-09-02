package org.apache.coyote.http11;

import nextstep.jwp.exception.UncheckedServletException;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.info("connect host: {}, port: {}", connection.getInetAddress(), connection.getPort());
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {

            StringBuilder requestBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString();

            String[] requestLines = request.split("\\s+");
            if (requestLines.length < 2) {
                throw new UncheckedServletException(new Exception("예외"));
            }
            String resourcePath = requestLines[1];

            byte[] responseBodyBytes;
            if (resourcePath.equals("/index.html")) {
                String projectRootPath = System.getProperty("user.dir");
                String htmlFilePath = projectRootPath + "/src/main/resources/static/index.html";
                responseBodyBytes = Files.readAllBytes(Paths.get(htmlFilePath));
            } else {
                responseBodyBytes = "Resource not found".getBytes(StandardCharsets.UTF_8);
            }

            final var response = String.join("\r\n",
                    "HTTP/1.1 200 OK ",
                    "Content-Type: text/html;charset=utf-8 ",
                    "Content-Length: " + responseBodyBytes.length + " ",
                    "",
                    new String(responseBodyBytes, StandardCharsets.UTF_8));

            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

}
