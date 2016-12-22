package com.softmotions.ncms;

import java.util.List;

import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class TestNGSuiteListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        System.err.println("\nApplying com.softmotions.ncms.TestNGSuiteListener");
        for (XmlSuite s : suites) {
            s.setGroupByInstances(true);
            XmlSuite ps;
            while ((ps = s.getParentSuite()) != null && (ps != s)) {
                ps.setGroupByInstances(true);
                s = ps;
            }
        }
    }
}
