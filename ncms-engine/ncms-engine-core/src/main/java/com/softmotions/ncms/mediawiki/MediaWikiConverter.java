package com.softmotions.ncms.mediawiki;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import info.bliki.htmlcleaner.ContentToken;
import info.bliki.htmlcleaner.EndTagToken;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.filter.WPList;
import info.bliki.wiki.model.Configuration;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.tags.HTMLTag;

import com.google.inject.Singleton;

/**
 * A converter which renders the internal tree node representation as HTML text
 */

@SuppressWarnings("OverlyNestedMethod")
@Singleton
public class MediaWikiConverter implements ITextConverter {
    private boolean renderLinks;

    public MediaWikiConverter(boolean renderLinks) {
        this.renderLinks = renderLinks;
    }

    public MediaWikiConverter() {
        this(false);
    }

    @Override
    public void nodesToText(List<?> nodes, Appendable resultBuffer, IWikiModel model) throws IOException {
        if (nodes != null && !nodes.isEmpty()) {
            try {
                int level = model.incrementRecursionLevel();

                if (level > Configuration.RENDERER_RECURSION_LIMIT) {
                    resultBuffer
                            .append("<span class=\"error\">Error - recursion limit exceeded rendering tags in HTMLConverter#nodesToText().</span>");
                    return;
                }
                Iterator<?> childrenIt = nodes.iterator();
                while (childrenIt.hasNext()) {
                    Object item = childrenIt.next();
                    if (item != null) {
                        if (item instanceof List) {
                            @SuppressWarnings("unchecked")
                            final List<Object> list = (List<Object>) item;
                            nodesToText(list, resultBuffer, model);
                        } else if (item instanceof ContentToken) {
                            ContentToken contentToken = (ContentToken) item;
                            String content = contentToken.getContent();
                            Utils.escapeXmlToBuffer(content, resultBuffer, true, true, true);
                        } else if (item instanceof HTMLTag) {
                            if (item instanceof WPList) {
                                WPList list = (WPList) item;
                                StringBuilder listSb = new StringBuilder();
                                list.renderHTML(this, listSb, model);
                                String listSpec = listSb.toString().trim();
                                if (listSpec.indexOf("<ul>") == 0) {
                                    listSpec = "<ul>" + listSpec.substring("<ul>".length());
                                } else if (listSpec.indexOf("<ol>") == 0) {
                                    listSpec = "<ol>" + listSpec.substring("<ol>".length());
                                }
                                resultBuffer.append(listSpec);
                            } else {
                                ((HTMLTag) item).renderHTML(this, resultBuffer, model);
                            }
                        } else if (item instanceof TagNode) {
                            TagNode node = (TagNode) item;
                            Map<String, Object> map = node.getObjectAttributes();
                            if (map != null && !map.isEmpty()) {
                                Object attValue = map.get("wikiobject");
                                if (attValue instanceof ImageFormat) {
                                    imageNodeToText(node, (ImageFormat) attValue, resultBuffer, model);
                                }
                            } else {
                                nodeToHTML(node, resultBuffer, model);
                            }
                        } else if (item instanceof EndTagToken) {
                            EndTagToken node = (EndTagToken) item;
                            resultBuffer.append('<');
                            resultBuffer.append(node.getName());
                            resultBuffer.append(" />");
                        }
                    }
                }
            } finally {
                model.decrementRecursionLevel();
            }
        }
    }

    protected void nodeToHTML(TagNode node, Appendable resultBuffer, IWikiModel model) throws IOException {
        String name = node.getName();
        if (HTMLTag.NEW_LINES) {
            if ("div".equals(name) || "p".equals(name) || "table".equals(name) || "ul".equals(name) || "ol".equals(name)
                || "li".equals(name) || "th".equals(name) || "tr".equals(name) || "td".equals(name) || "pre".equals(name)) {
                resultBuffer.append('\n');
            }
        }
        resultBuffer.append('<');
        resultBuffer.append(name);

        Map<String, String> tagAtttributes = node.getAttributes();

        for (Map.Entry<String, String> currEntry : tagAtttributes.entrySet()) {
            String attName = currEntry.getKey();
            if (attName.length() >= 1 && Character.isLetter(attName.charAt(0))) {
                String attValue = currEntry.getValue();

                resultBuffer.append(" ");
                resultBuffer.append(attName);
                resultBuffer.append("=\"");
                resultBuffer.append(attValue);
                resultBuffer.append("\"");
            }
        }

        List<Object> children = node.getChildren();
        if (children.isEmpty() && !"a".equals(name)) {
            resultBuffer.append(" />");
        } else {
            resultBuffer.append('>');
            if (!children.isEmpty()) {
                nodesToText(children, resultBuffer, model);
            }
            resultBuffer.append("</");
            resultBuffer.append(node.getName());
            resultBuffer.append('>');
        }
    }

    @Override
    public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer, IWikiModel model)
            throws IOException {
        Map<String, String> map = imageTagNode.getAttributes();
        String caption = imageFormat.getCaption();
        String alt = null;
        if (caption != null && !caption.isEmpty()) {
            alt = imageFormat.getAlt();
            caption = Utils.escapeXml(caption, true, false, true);
        }
        if (alt == null) {
            alt = "";
        }
        String location = imageFormat.getLocation();
        String type = imageFormat.getType();
        int pxWidth = imageFormat.getWidth();
        int pxHeight = imageFormat.getHeight();
        if ("thumb".equals(type) || "frame".equals(type)) {
            //noinspection ConstantConditions
            imageThumbToHTML(imageTagNode, resultBuffer, model, map,
                             caption, alt, location, type, pxWidth, pxHeight);
        } else {
            //noinspection ConstantConditions
            imageSimpleToHTML(imageTagNode, resultBuffer, model, map,
                              caption, alt, location, type, pxWidth, pxHeight);
        }
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private void imageThumbToHTML(TagNode imageTagNode,
                                  Appendable resultBuffer,
                                  IWikiModel model,
                                  Map<String, String> map,
                                  String caption,
                                  String alt,
                                  String location,
                                  String type,
                                  int pxWidth,
                                  int pxHeight) throws IOException {


        resultBuffer.append("\n<div class=\"thumb");
        if ("left".equals(location)) {
            resultBuffer.append(" tleft\"");
        } else if ("right".equals(location)) {
            resultBuffer.append(" tright\"");
        } else if ("center".equals(location)) {
            resultBuffer.append(" tcenter\"");
        } else {
            resultBuffer.append("\"");
        }
        resultBuffer.append('>');

        resultBuffer.append("\n<div class=\"thumbinner\" style=\"");
        if (pxHeight != -1) {
            resultBuffer.append("height:").append(Integer.toString(pxHeight)).append("px;");
        }
        if (pxWidth != -1) {
            resultBuffer.append("width:").append(Integer.toString(pxWidth + 2)).append("px;");
        }
        resultBuffer.append("display:inline-block;");
        resultBuffer.append("\">");

        String href = map.get("href");
        if (href != null) {
            resultBuffer.append("<a class=\"image\" href=\"").append(href).append("\" ");
            if (caption != null && !caption.isEmpty()) {
                resultBuffer.append("title=\"").append((alt.isEmpty()) ? caption : alt).append('"');
            }
            resultBuffer.append('>');
        }

        resultBuffer.append("<img src=\"").append(map.get("src")).append('"');
        resultBuffer.append(" class=\"thumbimage\"");
        if (caption != null && !caption.isEmpty()) {
            if (alt.isEmpty()) {
                resultBuffer.append(" alt=\"").append(caption).append('"').append(" title=\"").append(caption).append('"');
            } else {
                resultBuffer.append(" alt=\"").append(alt).append('"').append(" title=\"").append(alt).append('"');
            }
        }
        StringBuilder clazz = null;
        /*if (location != null && !(location.equalsIgnoreCase("none"))) {
            clazz = new StringBuilder(64);
            clazz.append(" class=\"location-");
            clazz.append(location);
        }*/
        if (type != null) {
            clazz = new StringBuilder(64);
            clazz.append(" class=\"");
            clazz.append("type-").append(type);
        }
        if (clazz != null) {
            resultBuffer.append(clazz).append('"');
        }
        if (pxHeight != -1) {
            resultBuffer.append(" height=\"").append(Integer.toString(pxHeight)).append('"');
        }
        if (pxWidth != -1) {
            resultBuffer.append(" width=\"").append(Integer.toString(pxWidth)).append('\"');
        }
        resultBuffer.append(" />\n");

        if (href != null) {
            resultBuffer.append("</a>");
        }
        List<Object> children = imageTagNode.getChildren();
        if (!children.isEmpty()) {
            nodesToText(children, resultBuffer, model);
        }

        resultBuffer.append("</div>\n");
        resultBuffer.append("</div>\n");
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    private void imageSimpleToHTML(TagNode imageTagNode,
                                   Appendable resultBuffer,
                                   IWikiModel model,
                                   Map<String, String> map,
                                   String caption,
                                   String alt,
                                   String location,
                                   String type,
                                   int pxWidth,
                                   int pxHeight) throws IOException {
        String href = map.get("href");
        if (href != null) {
            resultBuffer.append("<a class=\"image\" href=\"").append(href).append("\" ");

            if (caption != null && !caption.isEmpty()) {
                resultBuffer.append("title=\"").append((alt.isEmpty()) ? caption : alt).append('"');
            }
            resultBuffer.append('>');
        }

        resultBuffer.append("<img src=\"").append(map.get("src")).append('"');

        if (caption != null && !caption.isEmpty()) {
            if (alt.isEmpty()) {
                resultBuffer.append(" alt=\"").append(caption).append('"');
            } else {
                resultBuffer.append(" alt=\"").append(alt).append('"');
            }
        }

        StringBuilder clazz = null;
        if (location != null && !("none".equalsIgnoreCase(location))) {
            clazz = new StringBuilder(64);
            clazz.append(" class=\"location-");
            clazz.append(location);
        }
        if (type != null) {
            if (clazz == null) {
                clazz = new StringBuilder(64);
                clazz.append(" class=\"");
            } else {
                clazz.append(" ");
            }
            clazz.append(" type-").append(type);
        }
        if (clazz != null) {
            resultBuffer.append(clazz).append('"');
        }

        if (pxHeight != -1) {
            resultBuffer.append(" height=\"").append(Integer.toString(pxHeight)).append('"');
        }
        if (pxWidth != -1) {
            resultBuffer.append(" width=\"").append(Integer.toString(pxWidth)).append('\"');
        }
        resultBuffer.append(" />\n");

        if (href != null) {
            resultBuffer.append("</a>");
        }
        List<Object> children = imageTagNode.getChildren();
        if (!children.isEmpty()) {
            nodesToText(children, resultBuffer, model);
        }
    }

    @Override
    public boolean renderLinks() {
        return renderLinks;
    }
}
