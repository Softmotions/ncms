package ru.nsu.weather;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.softmotions.ncms.NcmsEnvironment;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.mybatis.guice.transactional.Transactional;
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
import javax.ws.rs.core.HttpHeaders;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

@Path("weather")
@Singleton
public class WeatherChartRS {

    private static final Logger log = LoggerFactory.getLogger(WeatherChartRS.class);

    private final NcmsEnvironment env;

    private static final String BACKGROUND_COLOR = "0xEEEEEE";
    private static final String ZERO_LINE_COLOR = "0x000080";
    private static final String CHART_COLOR = "0xBF0303";
    private static final String TEXT_COLOR = "0x000000";

    private int cWidth = 520;
    private int cHeight = 340;
    private int cPaddingX = 20;
    private int cPaddingY = 30;
    private String cHeader = "Температура около НГУ    {current} \u2103";
    private String wURL = "http://weather.nsu.ru/xml.php";
    private int wTimeback = 3 * 60 * 60 * 24;

    private Float currTemp;
    private NodeList tempData = null;

    @Inject
    public WeatherChartRS(NcmsEnvironment env) {
        this.env = env;
    }

    @GET
    @Path("/currtemp")
    @Produces("text/plain")
    public Float getCurrentTemp(@Context HttpServletRequest req) throws Exception {
        fetchData();
        return currTemp;
    }

    @GET
    @Path("/chart")
//    @Produces("image/png")
    public Response getChart(@Context HttpServletRequest req,
                             @Context HttpServletResponse resp) throws Exception {
        BufferedImage chartImage = drawChart();

        return Response.ok((StreamingOutput)output -> ImageIO.write(chartImage, "png", output))
                .type("image/png")
                .build();
    }

    private void fetchData() throws IOException {
        String tempXML = getXML();
        parseXML(tempXML);
    }

    private String getXML() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(wURL);

            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(final HttpResponse response)
                        throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();

                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: "
                                + status);
                    }
                }
            };

            return httpClient.execute(httpGet, responseHandler);
        } catch (IOException ioex) {
            throw ioex;
        }
    }

    private void parseXML(String xmlEntity) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = factory.newDocumentBuilder();

            Document wxml = db.parse(new InputSource(new StringReader(xmlEntity)));

            long cDate = System.currentTimeMillis() / 1000; //Current (end) date
            long sDate = cDate - wTimeback; //Start date

            XPath xPath = XPathFactory.newInstance().newXPath();
            String requiredNodes = "/weather/graph/temp[@timestamp>'" + sDate + "']";

            tempData = (NodeList)xPath.compile(requiredNodes).evaluate(wxml, XPathConstants.NODESET);
            currTemp = Float.parseFloat(wxml.getElementsByTagName("current").item(0).getTextContent());
        } catch (SAXException | ParserConfigurationException | IOException
                | XPathExpressionException ex) {
            ex.printStackTrace();
        }
    }

    public BufferedImage drawChart() throws IOException {
        fetchData();

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
            for (int curr  = 0; curr < tempData.getLength(); curr++) {
                Element elem = (Element) tempData.item(curr);
                float temp = Float.parseFloat(elem.getTextContent());

                sign |= (temp > 0 ? 1 : 2);

                tMax = Math.max(tMax, Math.abs(temp));
            }
        } catch (NumberFormatException nfe) {
            System.err.println("Error parsing chart point");
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
        int mergedPoints = (int)Math.ceil((float)tempData.getLength() / aWidth);
        java.util.List<Float> points = new ArrayList<>();
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

        float scaleTime = (float)aWidth / points.size();
        float scaleTemp = (1 == sign) ?
                ((float)(zeroY - cPaddingY) / tMax) :
                ((float)(aHeight - zeroY) / tMax);

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

        //Last bold point
        int dim = 5;
        Ellipse2D bold = new Ellipse2D.Float(nextX - dim / 2,
                nextY - dim / 2,
                dim, dim);
        ig2.fill(bold);

        //Draw header
        ig2.setFont(new Font("Impact", Font.PLAIN, 14));
        ig2.setPaint(Color.decode(TEXT_COLOR));
        cHeader = cHeader.replace("{current}", currTemp.toString());
        ig2.drawString(cHeader, cPaddingX, cPaddingY - 5);
        ig2.setFont(new Font("Impact", Font.PLAIN, 8));
        ig2.drawString("0 \u2103", cPaddingX, zeroY - 3);

        //Output image (should be modified to ImageOutputStream)
        //ImageIO.write(chartImage, "PNG", new File("wchart.png"));
        return chartImage;
    }
}
