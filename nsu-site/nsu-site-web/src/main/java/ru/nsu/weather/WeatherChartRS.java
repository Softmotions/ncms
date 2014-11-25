package ru.nsu.weather;

import com.softmotions.ncms.NcmsEnvironment;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Path("weather")
@Singleton
public class WeatherChartRS {

    private static final Logger log = LoggerFactory.getLogger(WeatherChartRS.class);

    private static final String BACKGROUND_COLOR = "0xEEEEEE";
    private static final String ZERO_LINE_COLOR = "0x000080";
    private static final String CHART_COLOR = "0xBF0303";
    private static final String TEXT_COLOR = "0x000000";

    private int cWidth;
    private int cHeight;
    private int cPaddingX;
    private int cPaddingY;
    private String cHeader;
    private String cImgFormat;

    private String wURL;
    private int wTimeback;
    private int wCacheTime;

    private Float currTemp;
    private BufferedImage currChart;
    private long dataTimestamp;
    private NodeList tempData;

    @Inject
    public WeatherChartRS(NcmsEnvironment env) {
        XMLConfiguration cfg = env.xcfg();

        SubnodeConfiguration chartCfg = cfg.configurationAt("wchart.chart");

        cWidth = chartCfg.getInt("width", 520);
        cHeight = chartCfg.getInt("height", 340);
        cPaddingX = chartCfg.getInt("padding-x", 20);
        cPaddingY = chartCfg.getInt("padding-y", 30);
        cHeader = chartCfg.getString("header", "Температура около НГУ    {current} \u2103");
        cImgFormat = chartCfg.getString("img-format", "png");

        SubnodeConfiguration weatherCfg = cfg.configurationAt("wchart.weather");

        wURL = weatherCfg.getString("url", "http://weather.nsu.ru/xml.php");
        wTimeback = weatherCfg.getInt("timeback", 3 * 60 * 60 * 24);
        wCacheTime = weatherCfg.getInt("cache-time", 300);
    }

    @GET
    @Path("/currtemp")
    @Produces("text/plain")
    public Float getCurrentTemp(@Context HttpServletRequest req) throws Exception {
        checkCache();

        return currTemp;
    }

    @GET
    @Path("/chart")
    public Response getChart(@Context HttpServletRequest req,
                             @Context HttpServletResponse resp) throws Exception {
        checkCache();
        return Response.ok((StreamingOutput) output -> ImageIO.write(currChart, cImgFormat, output))
                .type("image/" + cImgFormat)
                .build();
    }

    private synchronized void checkCache() throws IOException {
        long currDate = System.currentTimeMillis() / 1000;
        if (dataTimestamp < currDate - wCacheTime) {
            fetchData();
            currChart = drawChart();
        }
    }

    private void fetchData() throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            //Get weather XML file
            HttpGet httpGet = new HttpGet(wURL);
            ResponseHandler<String> responseHandler = (response) -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: "
                                                      + status);
                }
            };

            String xmlEntity = httpClient.execute(httpGet, responseHandler);

            //Parse weather XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = factory.newDocumentBuilder();

            Document wxml = db.parse(new InputSource(new StringReader(xmlEntity)));

            long currDate = System.currentTimeMillis() / 1000; //End date
            long startDate = currDate - wTimeback;

            XPath xPath = XPathFactory.newInstance().newXPath();
            String requiredNodes = "/weather/graph/temp[@timestamp>'" + startDate + "']";

            tempData = (NodeList) xPath.compile(requiredNodes).evaluate(wxml, XPathConstants.NODESET);
            currTemp = Float.parseFloat(wxml.getElementsByTagName("current").item(0).getTextContent());
            dataTimestamp = currDate;
        } catch (SAXException | ParserConfigurationException | XPathExpressionException ex) {
            log.warn("", ex);
        }
    }

    public BufferedImage drawChart() throws IOException {

        //Actual height/width
        int aHeight = cHeight - cPaddingY * 2;
        int aWidth = cWidth - cPaddingX * 2;
        int zeroY = cHeight / 2;

        BufferedImage chartImage =
                new BufferedImage(cWidth, cHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D ig2 = chartImage.createGraphics();
        ig2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        ig2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int sign = 0;
        float tMax = Float.parseFloat(tempData.item(0).getTextContent());

        try {
            for (int curr = 0; curr < tempData.getLength(); curr++) {
                Element elem = (Element) tempData.item(curr);
                float temp = Float.parseFloat(elem.getTextContent());
                sign |= (temp > 0 ? 1 : 2);
                tMax = Math.max(tMax, Math.abs(temp));
            }
        } catch (NumberFormatException e) {
            log.error("", e);
        }

        if (2 == sign) { //Only negative temperature
            zeroY = cHeight / 4;
        } else if (1 == sign) { //Only positive temperature
            zeroY = cHeight - cPaddingY;
        }

        ig2.setPaint(Color.decode(BACKGROUND_COLOR));
        Rectangle2D background = new Rectangle2D.Float(0, 0, cWidth, cHeight);
        ig2.draw(background);
        ig2.fill(background);

        //Zero line
        ig2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        ig2.setPaint(Color.decode(ZERO_LINE_COLOR));
        ig2.draw(new Line2D.Float(cPaddingX, zeroY,
                                  cWidth - cPaddingX, zeroY));

        //Draw chart
        int mergedPoints = (int) Math.ceil((float) tempData.getLength() / aWidth);
        List<Float> points = new ArrayList<>();
        float avgValue = 0;

        int currPoint = 1;

        for (int curr = 0; curr < tempData.getLength(); curr++) {
            float currTemp = Float.parseFloat(tempData.item(curr).getTextContent());
            avgValue += currTemp;
            if (0 == currPoint % mergedPoints) {
                avgValue /= mergedPoints;
                points.add(avgValue);
                avgValue = 0;
            } else if (curr == tempData.getLength() - 1) {
                points.add(currTemp);
            }
            currPoint++;
        }

        float scaleTime = (float) aWidth / points.size();
        float scaleTemp = (1 == sign) ?
                          ((float) (zeroY - cPaddingY) / tMax) :
                          ((float) (aHeight - zeroY) / tMax);

        ig2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        ig2.setPaint(Color.decode(CHART_COLOR));

        Path2D chart = new Path2D.Float();
        chart.moveTo(cPaddingX, zeroY - points.get(0) * scaleTemp);
        float nextX = 0, nextY = 0;
        for (int i = 1; i < points.size(); i++) {
            nextX = (i * scaleTime) + cPaddingX;
            nextY = zeroY - points.get(i) * scaleTemp;

            chart.lineTo(nextX, nextY);
        }

        ig2.draw(chart);

        //Draw last bold point
        float dim = 5;
        Ellipse2D bold = new Ellipse2D.Float(nextX - dim / 2, nextY - dim / 2, dim, dim);
        ig2.fill(bold);

        //Draw header
        ig2.setFont(new Font("Impact", Font.PLAIN, 14));
        ig2.setPaint(Color.decode(TEXT_COLOR));
        cHeader = cHeader.replace("{current}", currTemp.toString());
        ig2.drawString(cHeader, cPaddingX, cPaddingY - 5);
        ig2.setFont(new Font("Impact", Font.PLAIN, 10));
        ig2.drawString("0 \u2103", cPaddingX, zeroY - 3);
        return chartImage;
    }
}
