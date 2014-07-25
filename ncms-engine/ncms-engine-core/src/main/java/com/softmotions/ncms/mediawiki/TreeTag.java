package com.softmotions.ncms.mediawiki;

import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.tags.HTMLTag;
import info.bliki.wiki.tags.util.INoBodyParsingTag;
import com.softmotions.commons.cont.Pair;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * <code>
 * - первый
 * -- Первый у первого
 * -- Второй у первого
 * --- Первый у второго
 * -- Третий у первого
 * - второй
 * -третий
 * --первый у третьего
 * </tree>
 * </code>
 *
 * @author Adamansky Anton (anton@adamansky.com)
 * @version $Id$
 */
@Singleton
public class TreeTag extends HTMLTag implements INoBodyParsingTag {

    private static final Logger log = LoggerFactory.getLogger(TreeTag.class);

    static final char PREFIX = '-';


    public TreeTag() {
        super("div");
    }

    public boolean isAllowedAttribute(String attName) {
        return "open".equalsIgnoreCase(attName);
    }

    @Override
    public void renderHTML(ITextConverter converter, Appendable buf, IWikiModel model) throws IOException {

        Map<String, String> attrs = this.getAttributes();
        boolean isClosed = "false".equalsIgnoreCase(attrs.get("open"));

        String body = this.getBodyString();
        String[] bodyLines = body.split("\n");

        WikiModel inModel = new WikiModel((WikiModel) model);
        ITextConverter inConverter = new MediaWikiConverter();

        TreeNode root = new TreeNode(0, null, inModel, inConverter);
        Stack<TreeNode> nstack = new Stack<>();
        nstack.push(root);

        //Preprocess lines
        List<String> normLines = new ArrayList<>();
        for (int i = 0; i < bodyLines.length; ++i) {
            String line = bodyLines[i];
            Pair<Integer, String> res = treeNodeLevel(line);
            int lvl = res.getOne();
            int last = normLines.size() - 1;
            if (lvl == 0) {
                if (last == -1) {
                    continue;
                }
                normLines.set(last, normLines.get(last) + "\n" + line);
                continue;
            }
            normLines.add(line);
        }

        //Setup new body lines
        bodyLines = normLines.toArray(new String[normLines.size()]);
        //Process lines
        for (int i = 0; i < bodyLines.length; ++i) {
            String line = bodyLines[i];
            if ("".equals(line.trim())) {
                continue;
            }
            Pair<Integer, String> res = treeNodeLevel(line);
            int lvl = res.getOne();
            String markup = res.getTwo();

            if (lvl != nstack.size()) {
                int delta = lvl - nstack.size();
                int adelta = Math.abs(delta);
                for (int d = 0; d < adelta; ++d) {
                    if (delta > 0) {
                        TextNode labelNode;
                        TreeNode parent = nstack.peek();
                        if (!parent.childs.isEmpty() && parent.childs.get(parent.childs.size() - 1) instanceof TextNode) {
                            labelNode = (TextNode) parent.childs.get(parent.childs.size() - 1);
                            parent.childs.remove(parent.childs.size() - 1);
                        } else {
                            log.warn("Invalid markup at: " + line);
                            continue;
                        }

                        TreeNode ntnode = new TreeNode(lvl, parent, inModel, inConverter);
                        if (parent.lvl == 0) {
                            ntnode.closed = isClosed;
                        }
                        ntnode.labelNode = labelNode;
                        nstack.push(ntnode);
                    } else {
                        try {
                            nstack.pop();
                        } catch (EmptyStackException e) {
                            log.warn("Got empty stack, on: " + body);
                        }
                    }
                }
            }
            TreeNode current = nstack.peek();
            TextNode tn = new TextNode(lvl, current, markup, inModel, inConverter);
        }

        buf.append(root.toHtml());
    }


    Pair<Integer, String> treeNodeLevel(String line) {
        int res = 0;
        int i = 0;
        boolean onlyWhite = true;
        while (i < line.length()) {
            char c = line.charAt(i++);
            if (onlyWhite && Character.isWhitespace(c)) {
                continue;
            }
            onlyWhite = false;
            if (c != PREFIX) {
                break;
            }
            ++res;
        }
        return new Pair<>(res, i > 0 ? line.substring(i - 1) : line);
    }


    static class Node {

        protected final int lvl;
        protected final TreeNode parent;
        protected boolean first;
        protected boolean last;

        protected final IWikiModel model;
        protected final ITextConverter converter;


        String repeat(char c, int count) {
            if (count <= 0) {
                return "";
            }
            StringBuilder sb = new StringBuilder(count);
            for (int i = 0; i < count; ++i) {
                sb.append(c);
            }
            return sb.toString();
        }

        Node(int lvl, TreeNode parent, IWikiModel model, ITextConverter converter) {
            this.lvl = lvl;
            this.parent = parent;
            this.model = model;
            this.converter = converter;
            if (parent != null) {
                if (!parent.childs.isEmpty()) {
                    parent.childs.get(parent.childs.size() - 1).last = false;
                } else {
                    this.first = true;
                }
                if (parent.lvl == 0) { //it is root
                    this.first = true;
                }
                parent.childs.add(this);
                this.last = true;
            }
        }

        String toHtml() {
            return "";
        }
    }

    static final class TreeNode extends Node {

        TextNode labelNode;
        final List<Node> childs = new ArrayList<Node>();
        boolean closed;

        TreeNode(int lvl, TreeNode parent, IWikiModel model, ITextConverter converter) {
            super(lvl, parent, model, converter);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            final String offset = repeat(' ', lvl - 1);
            sb.append(offset).append("Tree");
            if (labelNode != null) {
                sb.append("#").append(labelNode);
            }
            sb.append("{");
            for (int i = 0; i < childs.size(); ++i) {
                sb.append("\n");
                sb.append(childs.get(i));
            }
            sb.append("\n");
            sb.append(offset).append("}");
            return sb.toString();
        }

        @Override
        String toHtml() {
            StringBuilder sb = new StringBuilder();
            final String offset = repeat(' ', lvl - 1);

            if (lvl == 0) { //root

                sb.append(offset).append("<ul class='tree'>");
                for (int i = 0; i < childs.size(); ++i) {
                    sb.append("\n").append(childs.get(i).toHtml()).append("\n");
                }
                sb.append("\n");
                sb.append(offset).append("</ul>");

            } else {  //Branch

                sb.append("<li class='folder");
                if (first && labelNode.lvl == 1) {
                    sb.append(" firstItem");
                }
                sb.append(closed ? " close" : " open");
                sb.append("'");
                sb.append(">");

                //Label
                if (first && labelNode.lvl == 1) {
                    sb.append("<p class='firstItem'>");
                } else {
                    sb.append("<p>");
                }
                sb.append("<span class=\"tree-icons\"></span>");
                sb.append(labelNode.wikiToHtml());
                sb.append("</p>");
                //EOF Label


                sb.append("<ul "); //ul body
                if (closed) {
                    sb.append("style='display:none;'");
                }
                sb.append(">");
                sb.append("\n");

                for (int i = 0; i < childs.size(); ++i) {
                    sb.append("\n").append(childs.get(i).toHtml()).append("\n");
                }

                sb.append("\n");
                sb.append("</ul>");

                sb.append("\n");
                sb.append("</li>");
            }

            return sb.toString();
        }
    }

    static class TextNode extends Node {

        @SuppressWarnings({"StringBufferWithoutInitialCapacity", "StringBufferField"})
        protected final StringBuilder markup = new StringBuilder();

        TextNode(int lvl, TreeNode parent, String text, IWikiModel model, ITextConverter converter) {
            super(lvl, parent, model, converter);
            if (text != null) {
                markup.append(text);
            }
        }

        String wikiToHtml() {
            String ri = model.render(converter, markup.toString().trim()).trim();
            if (ri.startsWith("<p>")) {
                ri = ri.substring("<p>".length());
            }
            if (ri.endsWith("</p>")) {
                ri = ri.substring(0, ri.length() - "</p>".length());
            }
            return ri;
        }

        @Override
        String toHtml() {
            StringBuilder sb = new StringBuilder();
            final String offset = repeat(' ', lvl);

            sb.append("<li class='file");
            if (last) {
                sb.append(" last");
            }
            sb.append("'");
            sb.append(">");
            sb.append("<p><span class='tree-icons'></span>");
            sb.append(wikiToHtml());
            sb.append("</p>");
            sb.append("</li>");

            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(repeat(' ', lvl)).append("Text{").append(markup).append("}");
            return sb.toString();
        }
    }
}
