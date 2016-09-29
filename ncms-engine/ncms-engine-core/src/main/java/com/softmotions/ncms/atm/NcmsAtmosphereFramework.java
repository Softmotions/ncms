package com.softmotions.ncms.atm;

import java.util.Queue;

import org.atmosphere.cpr.AtmosphereFramework;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class NcmsAtmosphereFramework extends AtmosphereFramework {

    public NcmsAtmosphereFramework() {
        Queue<String> tq = objectFactoryType();
        tq.clear();
        tq.add(NcmsAtmosphereObjectFactory.class.getName());
    }
}
