package com.quincus.web;

import com.amazonaws.util.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private byte[] body;

    public CacheHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException ex) {
            body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(new ByteArrayInputStream(body));
    }

    static class CachedServletInputStream extends ServletInputStream {

        private final InputStream cachedInputStream;

        public CachedServletInputStream(InputStream cachedInputStream) {
            this.cachedInputStream = Objects.requireNonNull(cachedInputStream);
        }

        @Override
        public int read() throws IOException {
            return cachedInputStream.read();
        }

        @Override
        public boolean isFinished() {
            try {
                return cachedInputStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
