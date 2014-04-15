package com.softmotions.ncms.asm.render;

import ninja.lifecycle.Dispose;
import ninja.lifecycle.Start;
import com.softmotions.web.GenericResponseWrapper;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;

import com.google.inject.Singleton;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.map.Flat3Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
@SuppressWarnings("unchecked")
@Singleton
public class AsmResourceAttributeRenderer implements AsmAttributeRenderer {

    private static final Logger log = LoggerFactory.getLogger(AsmResourceAttributeRenderer.class);

    public static final String[] TYPES = new String[]{"resource"};

    CloseableHttpClient httpclient;

    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    public String renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     Map<String, String> options) throws AsmRenderingException {

        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || StringUtils.isBlank(attr.getEffectiveValue())) {
            return null;
        }
        String location = attr.getEffectiveValue();
        URI uri;
        try {
            uri = new URI(location);
        } catch (URISyntaxException e) {
            log.warn("Invalid resource location: " + location +
                     " asm: " + ctx.getAsm());
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Including resource: '" + uri + '\'');
        }
        if (uri.getScheme() == null) {
            return internalInclude(ctx, uri, options);
        } else {
            return externalInclude(ctx, uri, options);
        }
    }

    private String externalInclude(AsmRendererContext ctx, URI location, Map<String, String> options) {
        StringWriter out = new StringWriter(1024);
        String cs = ctx.getServletRequest().getCharacterEncoding();
        if (cs == null) {
            cs = "UTF-8";
        }
        try {
            if (location.getScheme().startsWith("http")) {
                //HTTP GET
                if (options != null && !options.isEmpty()) {
                    URIBuilder ub = new URIBuilder(location);
                    for (Map.Entry<String, String> opt : options.entrySet()) {
                        ub.addParameter(opt.getKey(), opt.getValue());
                    }
                    location = ub.build();
                }
                HttpGet httpGet = new HttpGet(location);
                CloseableHttpResponse hresp = null;
                InputStream is = null;
                try {
                    httpclient = HttpClients.createSystem();
                    hresp = httpclient.execute(httpGet);
                    if (hresp.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                        log.warn("Invalid resource response status code: " + hresp.getStatusLine().getStatusCode() +
                                 " location: " + location +
                                 " asm: " + ctx.getAsm() +
                                 " response: " + out.toString());
                        return null;
                    }
                    is = hresp.getEntity().getContent();
                    IOUtils.copy(is, out, cs);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                    if (hresp != null) {
                        try {
                            hresp.close();
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    }
                    httpGet.reset();
                }
            } else {
                URL url = location.toURL();
                try (InputStream is = url.openStream()) {
                    IOUtils.copy(is, out, cs);
                }
            }
        } catch (Exception e) {
            log.warn("Unable to load resource: " + location +
                     " asm: " + ctx.getAsm(), e);
            return null;
        }
        return out.toString();
    }

    private String internalInclude(AsmRendererContext ctx, URI location, Map<String, String> options) {
        String cs = ctx.getServletRequest().getCharacterEncoding();
        if (cs == null) {
            cs = "UTF-8";
        }
        List<NameValuePair> qparams = URLEncodedUtils.parse(location, cs);
        if (!qparams.isEmpty()) {
            if (options == null) {
                options = new Flat3Map();
            }
            for (NameValuePair pair : qparams) {
                if (!options.containsKey(pair.getName())) {
                    options.put(pair.getName(), pair.getValue());
                }
            }
        }
        StringWriter out = new StringWriter(1024);
        HttpServletResponse resp = new GenericResponseWrapper(ctx.getServletResponse(), out, false);
        HttpServletRequest req = new InternalHttpRequest(ctx.getServletRequest(), options);
        RequestDispatcher rd = ctx.getServletRequest().getRequestDispatcher(location.getPath());
        try {
            rd.include(req, resp);
        } catch (IOException | ServletException e) {
            log.warn("", e);
            return null;
        }
        if (resp.getStatus() == HttpServletResponse.SC_OK) {
            return out.toString();
        } else {
            log.warn("Invalid resource response status code: " + resp.getStatus() +
                     " location: " + location +
                     " asm: " + ctx.getAsm() +
                     " response: " + out.toString());
            return null;
        }
    }


    @Start(order = 10)
    public void start() {
        httpclient = HttpClients.createSystem();
    }

    @Dispose(order = 10)
    public void stop() {
        if (httpclient != null) {
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error("", e);
            }
            httpclient = null;
        }
    }

    @SuppressWarnings("unchecked")
    static final class InternalHttpRequest extends HttpServletRequestWrapper {

        final Map<String, String> params;

        Map<String, String[]> paramsArr;

        InternalHttpRequest(HttpServletRequest request, Map<String, String> params) {
            super(request);
            this.params = (params != null ? params : Collections.EMPTY_MAP);

        }

        public String getParameter(String name) {
            return params.get(name);
        }

        public Map<String, String[]> getParameterMap() {
            if (paramsArr != null) {
                return paramsArr;
            }
            for (final Map.Entry<String, String> e : params.entrySet()) {
                paramsArr.put(e.getKey(), new String[]{e.getValue()});
            }
            return paramsArr;
        }

        public Enumeration<String> getParameterNames() {
            return IteratorUtils.asEnumeration(params.keySet().iterator());
        }

        public String[] getParameterValues(String name) {
            String pv = params.get(name);
            return pv != null ? new String[]{pv} : null;
        }
    }
}
