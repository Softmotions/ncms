package com.softmotions.ncms.asm;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;


/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */

public class AsmOptionsTest {

    private static final Logger log = LoggerFactory.getLogger(AsmOptionsTest.class);

    @Test
    public void testOptions() throws Exception {
        AsmOptions opts = new AsmOptions("a=b");
        assertEquals("b", opts.get("a"));

        opts = new AsmOptions("a=b,vvv=ccc,fff =xxx,esc\\,aped=some\\, escaped\\, text, k= 123");
        assertEquals("b", opts.get("a"));
        assertEquals("ccc", opts.get("vvv"));
        assertEquals("xxx", opts.get("fff"));
        assertEquals("123", opts.get("k"));
        assertEquals("some, escaped, text", opts.get("esc,aped"));
    }
}
