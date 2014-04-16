package com.softmotions.ncms.jaxrs;

import com.softmotions.ncms.HttpTestResponse;
import com.softmotions.ncms.NcmsWebTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
public class JAXRSExceptionHandlingTest extends NcmsWebTest {

    @Test
    public void testExceptionHandling() throws Exception {

        //
        HttpTestResponse resp = testBrowser.makeGET(getServerAddress() + "/ncms/rs/test/runtime-exception");
        assertEquals(500, resp.statusCode);
        assertEquals("Softmotions-Msg-Err0: a2d01be21ed4449ba48d0ba2019fa8fd",
                     String.valueOf(resp.resp.getFirstHeader("Softmotions-Msg-Err0")));
        assertEquals(0, resp.rawData.size());

        //
        resp = testBrowser.makeGET(getServerAddress() + "/ncms/rs/test/message-exception");
        assertEquals(500, resp.statusCode);
        assertEquals("Softmotions-Msg-Err0: 37e871c1226f425e8b0b774f276c3fa4",
                     String.valueOf(resp.resp.getFirstHeader("Softmotions-Msg-Err0")));
        assertEquals("Softmotions-Msg-Err1: 3635166bb1d940cd868b6cd744ee8cb3",
                     String.valueOf(resp.resp.getFirstHeader("Softmotions-Msg-Err1")));
        assertEquals("Softmotions-Msg-Reg0: 11264600f10d455283c5a13de2beb0ac",
                     String.valueOf(resp.resp.getFirstHeader("Softmotions-Msg-Reg0")));
        assertEquals(0, resp.rawData.size());

        //
        resp = testBrowser.makeGET(getServerAddress() + "/ncms/rs/test/message-regular-exception");
        assertEquals(200, resp.statusCode);
        assertEquals("Softmotions-Msg-Reg0: 5fb3c71bf81441c492ac3d7e784c8701",
                     String.valueOf(resp.resp.getFirstHeader("Softmotions-Msg-Reg0")));
        assertEquals(0, resp.rawData.size());


        //
        resp = testBrowser.makeGET(getServerAddress() + "/ncms/rs/test/message-not-found");
        assertEquals(404, resp.statusCode);
        assertTrue(resp.toString().contains(getServerAddress() + "/ncms/rs/test/message-not-found"));
    }

}
