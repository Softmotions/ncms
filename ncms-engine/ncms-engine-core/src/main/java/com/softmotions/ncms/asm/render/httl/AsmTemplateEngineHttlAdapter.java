package com.softmotions.ncms.asm.render.httl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import httl.Engine;
import httl.Template;
import httl.spi.loggers.Slf4jLogger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.commons.lifecycle.Dispose;
import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsModuleDescriptor;
import com.softmotions.ncms.asm.render.AsmRendererContext;
import com.softmotions.ncms.asm.render.AsmRenderingException;
import com.softmotions.ncms.asm.render.AsmResourceLoader;
import com.softmotions.ncms.asm.render.AsmTemplateEngineAdapter;
import com.softmotions.ncms.asm.render.AsmTemplateEvaluationException;
import com.softmotions.ncms.mhttl.HttlAsmMethods;
import com.softmotions.ncms.mhttl.HttlUtilsMethods;

/**
 * Template engine adapter for HTTL template engine (http://httl.github.io)
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

@Singleton
public class AsmTemplateEngineHttlAdapter implements AsmTemplateEngineAdapter {

    private static final Logger log = LoggerFactory.getLogger(AsmTemplateEngineHttlAdapter.class);

    public static final String[] DEFAULT_EXTS = new String[]{"*", "httl", "html", "httl.css"};

    private final String[] exts;

    private final Engine engine;

    private final AsmResourceLoader resourceLoader;

    @Override
    public String getType() {
        return "httl";
    }

    @Inject
    public AsmTemplateEngineHttlAdapter(NcmsEnvironment cfg,
                                        AsmResourceLoader resourceLoader,
                                        Set<NcmsModuleDescriptor> mset) {


        Set<String> methodClasses = new HashSet<>();
        Set<String> importPackages = new HashSet<>();
        Set<String> templateFilters = new HashSet<>();

        for (NcmsModuleDescriptor md : mset) {
            Collections.addAll(methodClasses, md.httlMethodClasses());
            Collections.addAll(importPackages, md.httlImportPackages());
            Collections.addAll(templateFilters, md.httlTemplateFilters());
        }

        Collections.addAll(importPackages,
                           "java.time",
                           "com.softmotions.ncms.mhttl",
                           "com.softmotions.ncms.asm",
                           "com.softmotions.commons.cont",
                           "org.apache.commons.configuration2");

        Collections.addAll(methodClasses,
                           HttlAsmMethods.class.getName(),
                           HttlUtilsMethods.class.getName());

        this.resourceLoader = resourceLoader;
        Properties httlProps = new Properties();
        String extsStr = cfg.xcfg().getString("httl[@extensions]", "*,httl,html,httl.css");
        if (!StringUtils.isBlank(extsStr)) {
            exts = extsStr.split(",");
            for (int i = 0; i < exts.length; ++i) {
                exts[i] = exts[i].trim();
            }
        } else {
            exts = DEFAULT_EXTS;
        }
        String httlPropsStr = cfg.xcfg().getString("httl");
        if (!StringUtils.isBlank(httlPropsStr)) {
            try {
                httlProps.load(new StringReader(httlPropsStr));
            } catch (IOException e) {
                String msg = "Failed to load <httl> properties: " + httlPropsStr;
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }

//        loggers=httl.spi.loggers.Slf4jLogger
//        loaders=com.softmotions.ncms.asm.render.httl.HttlLoaderAdapter
//        import.methods+=com.softmotions.ncms.mhttl.HttlAsmMethods\,com.softmotions.ncms.mhttl.HttlUtilsMethods\,com.smsfinance.sml.HttlMethods
//        import.packages+=com.softmotions.ncms.mhttl\,com.softmotions.ncms.asm\,com.softmotions.commons.cont\,org.apache.commons.configuration2
//        date.format=MM.dd.yyyy HH:mm
//                reloadable=true

        String key = "loggers";
        httlProps.remove(key + '+');
        String value = httlProps.getProperty(key, "");
        for (String c : new String[]{Slf4jLogger.class.getName()}) {
            if (!value.contains(c)) {
                if (!value.isEmpty()) {
                    value += ",";
                }
                value += c;
            }
        }
        httlProps.setProperty(key, value);

        key = "loaders";
        httlProps.remove(key + '+');
        value = httlProps.getProperty(key, "");
        for (String c : new String[]{HttlLoaderAdapter.class.getName()}) {
            if (!value.contains(c)) {
                if (!value.isEmpty()) {
                    value += ',';
                }
                value += c;
            }
        }
        httlProps.setProperty(key, value);

        key = "import.methods";
        value = httlProps.getProperty(key);
        if (value == null) {
            key += '+';
        }

        value = httlProps.getProperty(key, "");
        for (String c : methodClasses) {
            if (!value.contains(c)) {
                if (!value.isEmpty()) {
                    value += ',';
                }
                value += c;
            }
        }
        httlProps.setProperty(key, value);

        key = "import.packages";
        value = httlProps.getProperty(key);
        if (value == null) {
            key += '+';
        }
        value = httlProps.getProperty(key, "");
        for (String c : importPackages) {
            if (!value.contains(c)) {
                if (!value.isEmpty()) {
                    value += ',';
                }
                value += c;
            }
        }
        httlProps.setProperty(key, value);


        key = "template.filters";
        value = httlProps.getProperty(key);
        if (value == null) {
            key += '+';
        }
        value = httlProps.getProperty(key, "");
        for (String c : templateFilters) {
            if (!value.contains(c)) {
                if (!value.isEmpty()) {
                    value += ',';
                }
                value += c;
            }
        }
        httlProps.setProperty(key, value);


        if (!httlProps.containsKey("reloadable")) {
            httlProps.setProperty("reloadable", "true");
        }
        try {
            StringWriter eprops = new StringWriter();
            httlProps.store(eprops, "HTTL effective properties");
            log.info(eprops.toString());
        } catch (IOException e) {
            log.error("", e);
        }
        this.engine = Engine.getEngine(httlProps);
    }

    @Override
    public String[] getSupportedExtensions() {
        return exts;
    }

    @Override
    public void renderTemplate(String location, AsmRendererContext ctx, Writer out) throws IOException {
        try {
            Template template = engine.getTemplate(location, ctx.getLocale());
            template.render(ctx, out);
        } catch (Throwable e) {
            throw new AsmTemplateEvaluationException(ctx, location, e);
        }
    }

    @Override
    public void renderTemplate(String location, Map<String, Object> ctx, Locale locale, Writer out) throws IOException {
        try {
            Template template = engine.getTemplate(location, locale);
            template.render(ctx, out);
        } catch (ParseException e) {
            throw new AsmRenderingException("Failed to parse template: " + location, e);
        }
    }

    @Override
    public void checkTemplateSyntax(String location) throws AsmTemplateSyntaxException, IOException {
        HttlLoaderAdapter.contextLoaderStore.set(resourceLoader);
        try {
            engine.parseTemplateByName(location, null, null, null);
        } catch (ParseException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            // dirty parsing of httl message todo review it!
            String sep = ", stack: ";
            int ind = msg.indexOf(sep);
            if (ind != -1) {
                msg = msg.substring(0, ind);
            }
            throw new AsmTemplateSyntaxException(msg);
        } finally {
            HttlLoaderAdapter.contextLoaderStore.remove();
        }
    }

    @Dispose
    public void close() {

    }
}
