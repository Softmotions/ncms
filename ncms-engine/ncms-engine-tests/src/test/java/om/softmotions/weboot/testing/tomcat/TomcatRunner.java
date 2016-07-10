package om.softmotions.weboot.testing.tomcat;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class TomcatRunner {

    private static final Logger log = LoggerFactory.getLogger(TomcatRunner.class);

    private Tomcat tomcat;

    private Builder builder;

    private boolean cleanupTmpOnExit;

    private Context context;

    @SuppressWarnings("MethodOnlyUsedFromInnerClass")
    private void doConfigure(Builder b) throws Exception {
        this.builder = b;
        tomcat = new Tomcat();
        if (b.tmpDir == null) {
            Path tempDir = Files.createTempDirectory("tomcat-runner");
            b.withTmpDir(tempDir.toString());
            cleanupTmpOnExit = b.cleanupTmpOnExit;
        }
        File baseDirFile = new File(b.tmpDir);
        if (!baseDirFile.isDirectory()) {
            baseDirFile.mkdirs();
        }
        if (baseDirFile.isDirectory()) {
            throw new Exception("Failed to create base directory: " + baseDirFile);
        }

        log.info("Using basedir: {}", baseDirFile);
        tomcat.setBaseDir(baseDirFile.toString());
        tomcat.setPort(b.getPort());
        Connector connector = tomcat.getConnector();
        connector.setURIEncoding("UTF-8");
        if (b.resourcesBase == null) {
            return;
        }

        log.info("Context path: {}", b.contextPath);
        context = tomcat.addWebapp(b.contextPath, new File(b.resourcesBase).getAbsolutePath());
        if (b.contextResources != null && !b.contextResources.isEmpty()) {
            tomcat.enableNaming();
            NamingResourcesImpl namingResources = context.getNamingResources();
            b.contextResources.forEach(namingResources::addResource);
        }
    }

    public void start() throws Exception {
        tomcat.start();
        log.info("Tomcat server started");
    }

    public void shutdown() throws Exception {
        try {
            tomcat.stop();
            tomcat.destroy();
            log.info("Tomcat server stopped");
        } finally {
            context = null;
            if (cleanupTmpOnExit) {
                FileUtils.deleteDirectory(new File(builder.tmpDir));
            }
        }
    }

    public <T> T getContextEventListener(Class<T> type) throws Exception {
        if (context != null) {
            for (Object el : context.getApplicationEventListeners()) {
                if (type.isAssignableFrom(el.getClass())) {
                    //noinspection unchecked
                    return (T) el;
                }
            }
        }
        throw new Exception("Unable to find ContextEventListener of type: " + type);
    }

    public Builder usedBuilder() {
        return builder;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {

        private int port = 8282;

        private String contextPath = "/";

        private String resourcesBase;

        private String tmpDir;

        private boolean cleanupTmpOnExit = true;

        private Map<String, String> initPararams;

        private List<ContextResource> contextResources;

        public Builder withInitParameter(String name, String value) {
            if (initPararams == null) {
                initPararams = new HashMap<>();
            }
            initPararams.put(name, value);
            return this;
        }

        public Builder withContextResource(String name,
                                           String auth,
                                           String type,
                                           String scope,
                                           Map<String, String> properties) {
            if (contextResources == null) {
                contextResources = new ArrayList<>();
            }
            ContextResource cr = new ContextResource();
            cr.setName(name);
            if (auth != null) {
                cr.setAuth(auth);
            }
            if (type != null) {
                cr.setType(type);
            }
            if (scope != null) {
                cr.setScope(scope);
            }
            if (properties != null && !properties.isEmpty()) {
                for (Map.Entry<String, String> e : properties.entrySet()) {
                    cr.setProperty(e.getKey(), e.getValue());
                }
            }
            contextResources.add(cr);
            return this;
        }

        public Builder withTmpDir(String tmpDir) {
            this.tmpDir = tmpDir;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withContextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder withResourcesBase(String resourcesBase) {
            this.resourcesBase = resourcesBase;
            return this;
        }

        public Builder withoutCleanupTmpOnExit() {
            cleanupTmpOnExit = false;
            return this;
        }


        public TomcatRunner build() throws Exception {
            TomcatRunner runner = new TomcatRunner();
            runner.doConfigure(this);
            return runner;
        }

        public int getPort() {
            return port;
        }

        public String getTmpDir() {
            return tmpDir;
        }

        public String getContextPath() {
            return contextPath;
        }

        public String getResourcesBase() {
            return resourcesBase;
        }

        public Map<String, String> getInitPararams() {
            return initPararams;
        }
    }
}
