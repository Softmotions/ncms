package com.softmotions.ncms.asm.am;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.map.Flat3Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.client.cache.HeapResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import com.google.inject.Singleton;
import com.softmotions.commons.io.LimitedInputStream;
import com.softmotions.commons.json.JsonUtils;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.commons.lifecycle.Start;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmAttributeManagerContext;
import com.softmotions.ncms.asm.AsmOptions;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.web.GenericResponseWrapper;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@SuppressWarnings("unchecked")
@Singleton
public class AsmWebRefAM extends AsmAttributeManagerSupport {

    private static final Logger log = LoggerFactory.getLogger(AsmWebRefAM.class);

    public static final String[] TYPES = new String[]{"webref"};

    private static final int MAX_RESOURCE_LENGTH = 1048576; // 1Mb

    private CloseableHttpClient httpClient;

    private RequestConfig requestConfig;


    @Override
    public String[] getSupportedAttributeTypes() {
        return TYPES;
    }

    @Override
    public AsmAttribute prepareGUIAttribute(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            Asm page,
                                            Asm template,
                                            AsmAttribute tmplAttr,
                                            AsmAttribute attr) throws Exception {
        return attr;
    }

    @Override
    public Object renderAsmAttribute(AsmRendererContext ctx, String attrname,
                                     @Nonnull Map<String, String> options) throws AsmRenderingException {

        Asm asm = ctx.getAsm();
        AsmAttribute attr = asm.getEffectiveAttribute(attrname);
        if (attr == null || StringUtils.isBlank(attr.getEffectiveValue())) {
            return null;
        }
        AsmOptions opts = new AsmOptions();
        if (attr.getOptions() != null) {
            opts.loadOptions(attr.getOptions());
        }
        String location = attr.getEffectiveValue();
        URI uri;
        try {
            uri = new URI(location);
        } catch (URISyntaxException e) {
            log.warn("Invalid resource location: {} asm: {} attribute: {} error: {}", location, ctx.getAsm(), attrname, e.getMessage());
            return null;
        }
        if (BooleanUtils.toBoolean(opts.getString("asLocation"))) {
            return location;
        }
        if (log.isDebugEnabled()) {
            log.debug("Including resource: '{}" + '\'', uri);
        }
        Object res;
        if (uri.getScheme() == null) {
            res = internalInclude(ctx, attrname, uri, options);
        } else {
            res = externalInclude(ctx, attrname, uri, options);
        }
        //ctx.setNextEscapeSkipping(!BooleanUtils.toBoolean(opts.getString("escape")));
        return res;
    }

    @Nullable
    private String externalInclude(AsmRendererContext ctx,
                                   String attrname,
                                   URI location,
                                   Map<String, String> options) {

        try {

            String scheme = location.getScheme().toLowerCase().trim();
            if (scheme.startsWith("http")) {
                //HTTP GET
                if (options != null && !options.isEmpty()) {
                    URIBuilder ub = new URIBuilder(location);
                    for (Map.Entry<String, String> opt : options.entrySet()) {
                        ub.addParameter(opt.getKey(), opt.getValue());
                    }
                    location = ub.build();
                }
                HttpGet httpGet = new HttpGet(location);
                httpGet.setConfig(requestConfig);

                CloseableHttpResponse hresp = null;
                InputStream is = null;
                try {
                    hresp = httpClient.execute(httpGet);
                    HttpEntity entity = hresp.getEntity();

                    if (entity.getContentLength() > MAX_RESOURCE_LENGTH) {
                        log.warn("Resource response is too big: {} location: {} asm: {} attribute: {}",
                                 entity.getContentLength(), location, ctx.getAsm(), attrname);
                        return null;
                    }
                    if (hresp.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                        log.warn("Invalid resource response status code: {} location: {} asm: {} attribute: {}",
                                 hresp.getStatusLine().getStatusCode(), location, ctx.getAsm(), attrname);
                        return null;
                    }

                    String charset = "UTF-8";
                    Header cth = entity.getContentType();
                    if (cth != null && cth.getValue() != null) {
                        try {
                            Charset cset = MediaType.parse(cth.getValue()).charset().orNull();
                            if (cset != null) {
                                charset = cset.name();
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream(
                            entity.getContentLength() > 0 ? (int) entity.getContentLength() : 1024);
                    is = entity.getContent();
                    IOUtils.copy(is, bos);
                    return new String(bos.toByteArray(), 0, bos.size(), charset);

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
            } else if (scheme.contains("ftp:")) {
                URL url = location.toURL();
                try (InputStream is = url.openStream()) {
                    return IOUtils.toString(
                            new LimitedInputStream(MAX_RESOURCE_LENGTH,
                                                   String.format("Resource response is too big. Location: %s asm: %s attribute: %s",
                                                                 url.toString(), ctx.getAsm(), attrname),
                                                   is), "UTF-8");
                }
            } else {
                log.warn("Due to security restrictions rendering of: {} is prohibited", location);
                return null;
            }
        } catch (Exception e) {
            log.warn("Unable to load resource: {} asm: {} attribute: {}", location, ctx.getAsm(), attrname, e);
            return null;
        }
    }

    @Nullable
    private String internalInclude(AsmRendererContext ctx,
                                   String attrname,
                                   URI location,
                                   Map<String, String> options) {
        String cs = ctx.getServletRequest().getCharacterEncoding();
        if (cs == null) {
            cs = "UTF-8";
        }
        List<NameValuePair> qparams = URLEncodedUtils.parse(location, Charset.forName(cs));
        if (!qparams.isEmpty()) {
            if (options == null) {
                options = new Flat3Map<>();
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
            log.warn("Failed to include resource: {} asm: {} attribute: {}",
                     location, ctx.getAsm(), attrname, e);
            return null;
        }
        if (resp.getStatus() == HttpServletResponse.SC_OK) {
            return out.toString();
        } else {
            log.warn("Invalid resource response status code: {} location: {} asm: {} attribute: {} response: {}",
                     resp.getStatus(), location, ctx.getAsm(), attrname, out.toString());
            return null;
        }
    }

    @Override
    public AsmAttribute applyAttributeOptions(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        AsmOptions opts = new AsmOptions();
        JsonUtils.populateMapByJsonNode((ObjectNode) val, opts,
                                        "asLocation");
        attr.setOptions(opts.toString());
        attr.setEffectiveValue(StringUtils.trimToNull(val.path("value").asText(null)));
        return attr;
    }

    @Override
    public AsmAttribute applyAttributeValue(AsmAttributeManagerContext ctx, AsmAttribute attr, JsonNode val) throws Exception {
        attr.setEffectiveValue(StringUtils.trimToNull(val.path("value").asText(null)));
        return attr;
    }

    @Start(order = 10, parallel = true)
    public void start() {
        if (httpClient != null) {
            stop();
        }

        BasicHttpCacheStorage cacheStorage = new BasicHttpCacheStorage(
                CacheConfig
                        .custom()
                        // Estimated max mem usage: 67Mb (128Kb * 512)
                        .setMaxObjectSize(131072) // 128Kb
                        .setMaxCacheEntries(512)
                        .build());

        httpClient =
                CachingHttpClientBuilder
                        .create()
                        .setResourceFactory(new HeapResourceFactory())
                        .setHttpCacheStorage(cacheStorage)
                        .useSystemProperties()
                        .setMaxConnPerRoute(10)
                        .setMaxConnTotal(100)
                        .build();

        requestConfig =
                RequestConfig.custom()
                             .setConnectTimeout(1000)
                             .setConnectionRequestTimeout(1000)
                             .setSocketTimeout(10000)
                             .setCircularRedirectsAllowed(false)
                             .setRedirectsEnabled(true)
                             .setMaxRedirects(5)
                             .setAuthenticationEnabled(true)
                             .build();
    }

    @Dispose(order = 10)
    public void stop() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.error("", e);
            }
            httpClient = null;
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

        @Override
        public String getParameter(String name) {
            return params.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (paramsArr != null) {
                return paramsArr;
            }
            for (final Map.Entry<String, String> e : params.entrySet()) {
                paramsArr.put(e.getKey(), new String[]{e.getValue()});
            }
            return paramsArr;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return IteratorUtils.asEnumeration(params.keySet().iterator());
        }

        @Nullable
        @Override
        public String[] getParameterValues(String name) {
            String pv = params.get(name);
            return pv != null ? new String[]{pv} : null;
        }
    }
}
