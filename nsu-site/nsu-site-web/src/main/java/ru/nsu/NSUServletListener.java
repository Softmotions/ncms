package ru.nsu;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.ncms.NcmsServletListener;

import net.sf.j2ep.ProxyFilter;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NSUServletListener extends NcmsServletListener {

    protected void initBeforeFilters(NcmsEnvironment env, ServletContext sctx) {
        FilterRegistration.Dynamic fr = sctx.addFilter("nsuProxy", ProxyFilter.class);
        fr.addMappingForUrlPatterns(null, false, "/*");
        fr.setInitParameter("dataUrl", "/WEB-INF/proxy-rules.xml");
        fr.setInitParameter("cache", "true");
        fr.setInitParameter("cacheDir", "/tmp/nsusite-proxy");
        fr.setInitParameter("maxCacheEntries", "10000");
        fr.setInitParameter("maxCacheEntitySize", "1048576"); //1Mb
        fr.setInitParameter("connectionRequestTimeout", "1000"); //1sec
        fr.setInitParameter("connectTimeout", "1000"); //1sec
        fr.setInitParameter("socketTimeout", "10000"); //10sec
        fr.setInitParameter("maxConnPerRoute", "10");
        fr.setInitParameter("maxConnTotal", "100");

    }
}
