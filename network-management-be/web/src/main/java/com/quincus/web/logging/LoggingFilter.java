package com.quincus.web.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final String MDC_UUID = "UUID";
    private final ObjectMapper mapper = new ObjectMapper();

    public LoggingFilter() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        final ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        final String uuid = UUID.randomUUID().toString();
        MDC.put(MDC_UUID, uuid);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
            final LoggingTransaction loggingTransaction = new LoggingTransaction();
            loggingTransaction.setId(uuid);
            populateRequest(loggingTransaction, requestWrapper);
            populateResponse(loggingTransaction, responseWrapper);
            log.info("{}", mapper.writeValueAsString(loggingTransaction));
        } catch (IOException | ServletException e) {
            log.error(e.getMessage(), e);
        } finally {
            responseWrapper.addHeader(MDC_UUID, uuid);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void populateRequest(final LoggingTransaction loggingTransaction, final ContentCachingRequestWrapper requestWrapper) {
        try {
            loggingTransaction.setMethod(requestWrapper.getMethod());
            loggingTransaction.setRequestURI(requestWrapper.getRequestURI());

            final Iterator<String> it = requestWrapper.getHeaderNames().asIterator();
            while (it.hasNext()) {
                final String headerName = it.next();
                loggingTransaction.getRequest().addHeader(headerName, requestWrapper.getHeader(headerName));
            }

            final String contentType = requestWrapper.getContentType();
            if (StringUtils.isNotBlank(contentType) && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                loggingTransaction.getRequest().setBody(mapper.readTree(requestWrapper.getContentAsByteArray()));
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void populateResponse(final LoggingTransaction loggingTransaction, final ContentCachingResponseWrapper responseWrapper) {
        try {
            for (String headerName : responseWrapper.getHeaderNames()) {
                loggingTransaction.getResponse().addHeader(headerName, responseWrapper.getHeader(headerName));
            }

            final String contentType = responseWrapper.getContentType();
            if (StringUtils.isNotBlank(contentType) && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
                loggingTransaction.getResponse().setBody(mapper.readTree(responseWrapper.getContentAsByteArray()));
            }
            final int statusCode = responseWrapper.getStatus();
            loggingTransaction.setStatus(statusCode);
            loggingTransaction.setSuccess(HttpStatus.valueOf(statusCode).is2xxSuccessful());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}

@Data
final class LoggingTransaction {

    private final Request request = new Request();
    private final Response response = new Response();
    private String id;
    private String method;
    private String requestURI;
    private int status;
    private boolean success;

}

@Data
class Base {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Map<String, String> headers = new HashMap<>();

    private JsonNode body;

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }
}

final class Request extends Base {

}

final class Response extends Base {

}
