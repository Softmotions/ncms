package ru.nsu;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsServletListener;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;
import net.sf.j2ep.ProxyFilter;

import javax.management.MBeanServer;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.lang.management.ManagementFactory;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NSUServletListener extends NcmsServletListener {

    protected void initBeforeFilters(NcmsEnvironment env, ServletContext sctx) {

        initEhcache(env, sctx);

        FilterRegistration.Dynamic fr = sctx.addFilter("nsuProxy", ProxyFilter.class);
        fr.setAsyncSupported(true);
        fr.addMappingForUrlPatterns(null, false, "/*");
        fr.setInitParameter("dataUrl", "/WEB-INF/proxy-rules.xml");
        fr.setInitParameter("cache", "true");
        fr.setInitParameter("httpCacheStorage", "ru.nsu.cache.EhcacheHttpClientStorage");
        fr.setInitParameter("ehcacheCache", "pageProxyCache");
        fr.setInitParameter("useHeuristicCaching", "true");
        fr.setInitParameter("connectionRequestTimeout", "1000"); //1sec
        fr.setInitParameter("connectTimeout", "1000"); //1sec
        fr.setInitParameter("socketTimeout", "10000"); //10sec
        fr.setInitParameter("maxConnPerRoute", "10");
        fr.setInitParameter("maxConnTotal", "100");

    }


    private void initEhcache(NcmsEnvironment env, ServletContext sctx) {
        String ecfg = sctx.getRealPath("/WEB-INF/ehcache.xml");
        log.info("Using EHCache config: " + ecfg);
        CacheManager cm = CacheManager.create(ecfg);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ManagementService.registerMBeans(cm, mbs, true, true, true, true);
    }
}
