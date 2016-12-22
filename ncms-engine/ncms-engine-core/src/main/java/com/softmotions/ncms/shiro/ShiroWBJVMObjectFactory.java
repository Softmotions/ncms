package com.softmotions.ncms.shiro;

import org.apache.shiro.util.Factory;

import com.softmotions.commons.JVMResources;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class ShiroWBJVMObjectFactory implements Factory {

    private String resourceName;

    private Class requiredType;

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setRequiredType(Class requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    public Object getInstance() {
        if (resourceName == null) {
            throw new RuntimeException("resourceName is not set");
        }
        Object res = JVMResources.getOrFail(resourceName);
        if (requiredType != null) {
            return requiredType.cast(res);
        } else {
            return res;
        }
    }
}
