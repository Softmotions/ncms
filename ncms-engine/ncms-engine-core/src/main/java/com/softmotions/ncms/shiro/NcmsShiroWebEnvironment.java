package com.softmotions.ncms.shiro;

import javax.servlet.ServletContext;

import org.apache.shiro.web.env.IniWebEnvironment;

import com.softmotions.ncms.NcmsEnvironment;
import com.softmotions.weboot.WBServletListener;

/**
 * @author Motyrev Pavel (legioner.r@gmail.com)
 */
public class NcmsShiroWebEnvironment extends IniWebEnvironment {

    @Override
    public void setServletContext(ServletContext sctx) {
        super.setServletContext(sctx);
        NcmsEnvironment env = (NcmsEnvironment)
                sctx.getAttribute(WBServletListener.WEBOOT_CFG_SCTX_KEY);
        String configLocations = env.xcfg().getString("security.shiro-config-locations", "/WEB-INF/shiro.ini");
        setConfigLocations(configLocations);
    }
}
