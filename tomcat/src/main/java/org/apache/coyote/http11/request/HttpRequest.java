package org.apache.coyote.http11.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {

    private final RequestLine requestLine;
    private final RequestHeader requestHeader;
    private final String requestBody;
    private boolean hasValidatedSession;

    public HttpRequest(final BufferedReader reader) throws IOException, URISyntaxException {
        final List<String> lines = readAllLines(reader);

        requestLine = createRequestLine(lines);
        requestHeader = createRequestHeader(lines);
        requestBody = addRequestBody(lines, reader);
        hasValidatedSession = false;
    }

    private static List<String> readAllLines(final BufferedReader reader) {
        final List<String> lines = new ArrayList<>();

        String line;
        while (!(line = readOneLine(reader)).equals("")) {
            lines.add(line);
        }
        return lines;
    }

    private RequestLine createRequestLine(List<String> lines) throws URISyntaxException {
        final String[] requestLineList = lines.get(0).split("\\s+");
        final URI uri = new URI(requestLineList[1]);
        final String resourcePath = uri.getPath();
        return new RequestLine(HttpMethod.valueOf(requestLineList[0]), resourcePath, requestLineList[2]);
    }

    private RequestHeader createRequestHeader(final List<String> lines) {
        return RequestHeader.from(lines.subList(1, lines.size()));
    }

    private static String readOneLine(final BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    private String addRequestBody(final List<String> lines,
                                  final BufferedReader reader) throws IOException {
        int contentLength = getContentLength(lines);
        if (contentLength > 0) {
            final char[] buffer = new char[contentLength];
            reader.read(buffer);
            return new String(buffer);
        }
        return "";
    }

    private int getContentLength(List<String> lines) {
        for (String line : lines) {
            if (line.startsWith("Content-Length:")) {
                return Integer.parseInt(line.split(":")[1].strip());
            }
        }
        return 0;
    }

    public void addJSessionId(final String jSessionId) {
        requestHeader.addSession(jSessionId);
    }

    public boolean hasJSessionId() {
        return requestHeader.hasJSessionId();
    }

    public void setHasValidatedSessionTrue() {
        this.hasValidatedSession = true;
    }

    public String getJSessionId() {
        return requestHeader.getJSessionId();
    }

    public RequestLine getRequestLine() {
        return requestLine;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public boolean isHasValidatedSession() {
        return hasValidatedSession;
    }
}
