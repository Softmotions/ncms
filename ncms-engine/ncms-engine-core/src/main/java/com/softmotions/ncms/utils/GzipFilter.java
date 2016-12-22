package com.softmotions.ncms.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public final class GzipFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(GzipFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String acceptEncoding = httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (acceptEncoding != null) {
            if (acceptEncoding.contains("gzip")) {
                GZIPHttpServletResponseWrapper gzipResponse = new GZIPHttpServletResponseWrapper(httpResponse);
                chain.doFilter(request, gzipResponse);
                gzipResponse.finish();
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public static final class GZIPHttpServletResponseWrapper extends HttpServletResponseWrapper {

        private ServletResponseGZIPOutputStream gzipStream;
        private ServletOutputStream outputStream;
        private PrintWriter printWriter;

        public GZIPHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
            super(response);
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        }

        @Override
        public void setHeader(String name, String value) {
            if (!HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                && !HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(name)) {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if (!HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)
                && !HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(name)) {
                super.addHeader(name, value);
            }
        }

        public void finish() throws IOException {
            if (printWriter != null) {
                printWriter.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (gzipStream != null) {
                gzipStream.close();
            }
        }

        @Override
        public void flushBuffer() throws IOException {
            if (printWriter != null) {
                printWriter.flush();
            }
            if (outputStream != null) {
                outputStream.flush();
            }
            super.flushBuffer();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (printWriter != null) {
                throw new IllegalStateException("printWriter already defined");
            }
            if (outputStream == null) {
                gzipStream = new ServletResponseGZIPOutputStream(getResponse().getOutputStream());
                outputStream = gzipStream;
            }
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (outputStream != null) {
                throw new IllegalStateException("printWriter already defined");
            }
            if (printWriter == null) {
                gzipStream = new ServletResponseGZIPOutputStream(getResponse().getOutputStream());
                printWriter = new PrintWriter(new OutputStreamWriter(gzipStream, "UTF-8"));
            }
            return printWriter;
        }

        @Override
        public void setContentLength(int len) {
        }
    }


    public static final class ServletResponseGZIPOutputStream extends ServletOutputStream {

        private final GZIPOutputStream gzipStream;
        private final AtomicBoolean open = new AtomicBoolean(true);

        public ServletResponseGZIPOutputStream(OutputStream output) throws IOException {
            gzipStream = new GZIPOutputStream(output, 8192);
        }

        @Override
        public void close() throws IOException {
            if (open.compareAndSet(true, false)) {
                gzipStream.close();
            }
        }

        @Override
        public void flush() throws IOException {
            gzipStream.flush();
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }
            gzipStream.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            if (!open.get()) {
                throw new IOException("Stream closed!");
            }
            gzipStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            try {
                writeListener.onWritePossible();
            } catch (IOException e) {
                log.error("", e);
            }
        }
    }
}




