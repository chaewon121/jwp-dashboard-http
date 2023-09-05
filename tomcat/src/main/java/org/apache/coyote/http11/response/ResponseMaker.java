package org.apache.coyote.http11.response;

import org.apache.coyote.http11.request.HttpRequest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class ResponseMaker {

    public abstract String createResponse(final HttpRequest request) throws IOException;

    protected byte[] getResponseBodyBytes(String resourcePath) throws IOException {
        final URL fileUrl = this.getClass().getClassLoader().getResource("static" + resourcePath);
        return Files.readAllBytes(Paths.get(fileUrl.getPath()));
    }

}
