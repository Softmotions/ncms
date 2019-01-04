package com.softmotions.ncms;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.softmotions.weboot.WBConfiguration;

/**
 * Ncms configuration.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@ThreadSafe
public class NcmsEnvironment extends WBConfiguration {

    private String adminRoot;

    @Override
    protected String getCorePropsLocationResource() {
        return "/com/softmotions/ncms/core/Core.properties";
    }

    @Override
    public void load(String location, ServletContext sctx) {
        super.load(location, sctx);
        this.adminRoot = getAppRoot() + "/adm";
    }

    @Deprecated
    @Nonnull
    public String getNcmsPrefix() {
        return getAppPrefix();
    }

    @Deprecated
    @Nonnull
    public String getNcmsRoot() {
        return getAppRoot();
    }

    @Nonnull
    public String getNcmsAdminRoot() {
        return adminRoot;
    }

    public String[] attrArray(String av) {
        if (av == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        if (av.indexOf(',') != -1) {
            String[] ret = StringUtils.split(av, ',');
            for (int i = 0, l = ret.length; i < l; ++i) {
                ret[i] = ret[i].trim();
            }
            return ret;
        } else {
            return new String[]{av};
        }
    }
}
