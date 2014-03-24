package com.softmotions.commons.web;

import com.softmotions.commons.cl.ClassLoaderUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servlet provides access to the set of resources
 * stored in jar files in the classpath.
 * Supports automatic content reloading if
 * jar file updated.
 * <p>
 * <p>
 * Servlet parameters in the following format:
 * <pre>
 *      {prefix} => {injar path} [,watch=yes|no]
 * </pre>
 * <p>
 * Example:
 * <pre>
 *  &lt;servlet&gt;
 *      &lt;servlet-name&gt;JarResourcesServlet&lt;/servlet-name&gt;
 *      &lt;servlet-class&gt;com.softmotions.commons.web.JarResourcesServlet&lt;/servlet-class&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;ncms&lt;/param-name&gt;
 *          &lt;param-value&gt;ncms-engine-qx/ncms, watch=yes&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *  &lt;/servlet&gt;
 * </pre>
 *
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class JarResourcesServlet extends HttpServlet implements JarResourcesProvider {

    private static final Logger log = LoggerFactory.getLogger(JarResourcesServlet.class);

    List<MappingSlot> mslots;

    public void init() throws ServletException {
        mslots = new ArrayList<>();
        ServletConfig cfg = getServletConfig();
        Enumeration<String> pnames = cfg.getInitParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = pnames.nextElement();
            try {
                handleJarMapping(pname, cfg.getInitParameter(pname));
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    public void destroy() {
        for (MappingSlot ms : mslots) {
            try {
                ms.close();
            } catch (IOException e) {
            }
        }
        mslots.clear();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, true);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, true);
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        getContent(req, resp, false);
    }

    void getContent(HttpServletRequest req, HttpServletResponse resp, boolean transfer) throws ServletException, IOException {
        ContentDescriptor cd = getContentDescriptor(req.getPathInfo());
        if (cd == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (cd.getMimeType() != null) {
            resp.setContentType(cd.getMimeType());
            if (resp.getCharacterEncoding() == null && isTextualMimeType(cd.getMimeType())) {
                resp.setCharacterEncoding("UTF-8");
            }
        }
        URL url = cd.getUrl();
        try (InputStream is = url.openStream()) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(is, output);
            resp.setContentLength(output.size());
            if (transfer) {
                output.writeTo(resp.getOutputStream());
                resp.getOutputStream().flush();
            }
        }
    }

    public ContentDescriptor getContentDescriptor(final String path) {
        MappingSlot ms = findMatchingSlot(path);
        if (ms == null) {
            return null;
        }
        final URL url = ms.getResourceUrl(path);
        if (url == null) {
            return null;
        }
        final String mtype = getServletContext().getMimeType(path);
        return new ContentDescriptor() {
            public URL getUrl() {
                return url;
            }

            public String getMimeType() {
                return mtype;
            }

            public String getRequestedPath() {
                return path;
            }
        };
    }

    MappingSlot findMatchingSlot(String path) {
        for (final MappingSlot ms : mslots) {
            if (path.startsWith(ms.prefix)) {
                return ms;
            }
        }
        return null;
    }

    void handleJarMapping(String prefix, String spec) throws Exception {
        if (prefix.isEmpty()) {
            throw new Exception("Empty config param name");
        }
        mslots.add(new MappingSlot(prefix, spec));
    }

    private static class MappingSlot implements Closeable {

        final String prefix;

        final String path;

        final Object lock = new Object();

        JarResourcesClassLoader loader;

        private MappingSlot(String prefix, String spec) throws Exception {
            if (prefix.charAt(0) != '/') {
                prefix = "/" + prefix;
            }
            String[] parts = spec.split(",");
            if (parts.length == 0) {
                throw new ServletException(String.format("Invalid configuration param %s: %s", prefix, spec));
            }
            this.path = StringUtils.strip(parts[0].trim(), "/");
            //Parse options
            /*for (int i = 1; i < parts.length; ++i) {
                String p = parts[i].toLowerCase();
                String[] pp = p.split("=");
                if (pp.length == 2) {
                    p = pp[0].trim();
                    switch (p) {
                        case "watch":
                        case "watching":
                            this.watch = BooleanUtils.toBoolean(pp[1]);
                            break;
                    }
                }
            }*/
            this.prefix = prefix;
            log.info("Registered JAR resources mapping: " + prefix + " => " + spec);
        }

        URL getResourceUrl(String resource) {
            URL resourceUrl;
            resource = resource.substring(prefix.length());
            String resourceTranslated = path + ((resource.charAt(0) != '/') ? ("/" + resource) : resource);
            if (loader != null) {
                return loader.getResource(resourceTranslated);
            }
            ClassLoader baseLoader =
                    ObjectUtils.firstNonNull(Thread.currentThread().getContextClassLoader(),
                                             getClass().getClassLoader());
            resourceUrl = baseLoader.getResource(resourceTranslated);
            if (resourceUrl != null && "jar".equals(resourceUrl.getProtocol())) {
                synchronized (lock) {
                    if (loader != null) {
                        return loader.getResource(resourceTranslated);
                    }
                    String p = resourceUrl.getFile();
                    URL baseJar;
                    try {
                        baseJar = new URL(p.substring(0, p.indexOf('!')));
                    } catch (MalformedURLException e) {
                        log.error("", e);
                        return resourceUrl;
                    }
                    loader = new JarResourcesClassLoader(baseJar, baseLoader);
                    return loader.getResource(resourceTranslated);
                }
            }
            return resourceUrl;
        }

        public String toString() {
            return "MappingSlot{" + prefix + " => " + path + '}';
        }

        public int hashCode() {
            return prefix.hashCode();
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return prefix.equals(obj);
        }


        public void close() throws IOException {
            if (loader != null) {
                loader.close();
                loader = null;
            }
        }
    }

    @SuppressWarnings("StaticCollection")
    static final Set<String> APP_TXT_MTYPES;

    static {
        APP_TXT_MTYPES = new HashSet<>();
        APP_TXT_MTYPES.add("application/atom+xml");
        APP_TXT_MTYPES.add("application/rdf+xml");
        APP_TXT_MTYPES.add("application/rss+xml");
        APP_TXT_MTYPES.add("application/soap+xml");
        APP_TXT_MTYPES.add("application/xop+xml");
        APP_TXT_MTYPES.add("application/xhtml+xml");
        APP_TXT_MTYPES.add("application/json");
        APP_TXT_MTYPES.add("application/javascript");
        APP_TXT_MTYPES.add("application/xml");
        APP_TXT_MTYPES.add("application/xml-dtd");
        APP_TXT_MTYPES.add("application/x-tex");
        APP_TXT_MTYPES.add("application/x-latex");
        APP_TXT_MTYPES.add("application/x-javascript");
        APP_TXT_MTYPES.add("application/ecmascript");
    }

    private static boolean isTextualMimeType(String mtype) {
        return mtype != null && (mtype.startsWith("text/") || APP_TXT_MTYPES.contains(mtype));
    }

    private static final class JarResourcesClassLoader extends URLClassLoader {

        private JarResourcesClassLoader(URL url, ClassLoader parent) {
            super(new URL[]{url}, parent);
        }

        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class clazz;
            synchronized (getClassLoadingLock(name)) {
                clazz = findLoadedClass(name);
                if (clazz == null) {
                    clazz = findClass(name);
                }
                if (clazz != null) {
                    if (resolve) resolveClass(clazz);
                    return clazz;
                }
            }
            throw new ClassNotFoundException(name);
        }

        public URL getResource(String name) {
            return findResource(name);
        }

        public void close() throws IOException {
            ClassLoaderUtils.destroyClassLoader(this);
            super.close();
        }

        public String toString() {
            return "JarResourcesClassLoader{ " + Arrays.asList(getURLs()) + '}';
        }
    }
}
