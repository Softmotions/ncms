package com.softmotions.ncms.asm;

import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.asm.events.AsmCreatedEvent;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.mhttl.ImageMeta;
import com.softmotions.weboot.solr.SolrDataHandler;

import com.google.common.collect.AbstractIterator;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class PageSolrDataHandler implements SolrDataHandler {

    protected static final Logger log = LoggerFactory.getLogger(PageSolrDataHandler.class);

    private static final Pattern ANNOTATION_BREAKER_PATTERN = Pattern.compile("[.;,:\\n]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    private final AsmAttributeManagersRegistry aamr;

    private final NcmsEventBus ebus;

    private final SolrServer solr;

    private final AsmDAO adao;

    private Collection<String> extraAttributeNames;

    private int annotationLength;

    @Inject
    public PageSolrDataHandler(AsmAttributeManagersRegistry aamr,
                               NcmsEventBus ebus,
                               SolrServer solr,
                               AsmDAO adao) {
        this.aamr = aamr;
        this.ebus = ebus;
        this.solr = solr;
        this.adao = adao;
    }

    public void init(Configuration cfg) {
        String[] attrs = cfg.getStringArray("extra-attributes");
        if (attrs == null || attrs.length == 0 || (attrs.length == 1 && "*".equals(attrs[0]))) {
            extraAttributeNames = null;
        } else {
            extraAttributeNames = new ArrayList<>();
            if (attrs.length > 0) {
                extraAttributeNames.addAll(Arrays.asList(attrs));
            }
        }

        annotationLength = cfg.getInt("annotation-length", 300);

        ebus.register(this);
    }


    public Iterator<SolrInputDocument> getData() {
        final Iterator<Asm> asmsi = adao.asmSelectAllPlain().iterator();

        return new AbstractIterator<SolrInputDocument>() {
            protected SolrInputDocument computeNext() {
                while (true) {
                    if (!asmsi.hasNext()) {
                        return endOfData();
                    }

                    SolrInputDocument solrDocument = asmToSolrDocument(adao.asmSelectById(asmsi.next().getId()));
                    if (solrDocument != null) {
                        return solrDocument;
                    }
                }
            }
        };
    }

    private SolrInputDocument asmToSolrDocument(Asm asm) {
        if (StringUtils.isBlank(asm.getType())) {
            return null;
        }
        SolrInputDocument res = new SolrInputDocument();
        res.addField("id", asm.getId());
        res.addField("name", asm.getName());
        res.addField("hname", asm.getHname());
        res.addField("description", asm.getDescription());
        res.addField("published", asm.isPublished());
        res.addField("type", asm.getType());
        if (asm.getCdate() != null) {
            res.addField("cdate", asm.getCdate().getTime());
        }
        if (asm.getMdate() != null) {
            res.addField("mdate", asm.getMdate().getTime());
        }
        for (String attrName : extraAttributeNames == null ? asm.getEffectiveAttributeNames() : extraAttributeNames) {
            AsmAttribute attr = asm.getEffectiveAttribute(attrName);
            if (attr != null) {
                AsmAttributeManager aam = aamr.getByType(attr.getType());
                if (aam != null) {
                    Object[] data = aam.fetchFTSData(attr);
                    if (data != null) {
                        for (Object obj : data) {
                            addData(res, "asm_attr", attrName, obj);
                        }
                    }
                }
            }
        }

        String annotation = (String) res.getFieldValue("asm_attr_s_annotation");

        if (!StringUtils.isBlank(annotation)) {
            res.addField("annotation", annotation);
        } else {
            String content = (String) res.getFieldValue("asm_attr_s_content");
            if (!StringUtils.isBlank(content)) {
                if (content.length() < annotationLength) {
                    annotation = content;
                } else {
                    Matcher matcher = ANNOTATION_BREAKER_PATTERN.matcher(content);
                    int start;
                    if (matcher.find(annotationLength / 2) && ((start = matcher.start()) < annotationLength)) {
                        annotation = content.substring(0, start + 1).trim();
                    } else {
                        matcher = WHITESPACE_PATTERN.matcher(content);
                        if (matcher.find(annotationLength / 2) && ((start = matcher.start()) < annotationLength)) {
                            annotation = content.substring(0, start);
                        } else {
                            annotation = content.substring(0, annotationLength);
                        }
                    }
                }
            }

            if (!StringUtils.isBlank(annotation)) {
                res.addField("annotation", StringUtils.normalizeSpace(annotation.replaceAll("(\\n\\s*)+", "<br/>")));
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SolrDocument: " + res);
        }

        return res;
    }

    private void addData(SolrInputDocument sid, String prefix, String suffix, Object data) {
        //noinspection IfStatementWithTooManyBranches
        if (data == null) {
        } else if (data instanceof Long || data instanceof Integer) {
            sid.addField(prefix + "_l_" + suffix, data);
        } else if (data instanceof Boolean) {
            sid.addField(prefix + "_b_" + suffix, data);
        } else if (data instanceof ImageMeta) {
            sid.addField(prefix + "_image_" + suffix, SerializationUtils.serialize((ImageMeta) data));
        } else {
            sid.addField(prefix + "_s_" + suffix, data);
        }
    }

    @Subscribe
    public void onAsmCreate(AsmCreatedEvent e) {
        updateAsmInSolr(e.getId());
    }

    @Subscribe
    public void onAsmModify(AsmModifiedEvent e) {
        updateAsmInSolr(e.getId());
    }

    @Subscribe
    public void onAsmRemove(AsmRemovedEvent e) {
        updateAsmInSolr(e.getId());
    }

    private void updateAsmInSolr(Long id) {
        Asm asm = adao.asmSelectById(id);
        SolrInputDocument solrDocument = asm != null ? asmToSolrDocument(asm) : null;
        try {
            if (solrDocument == null) {
                solr.deleteById(String.valueOf(id));
            } else {
                solr.add(solrDocument);
            }
            solr.commit();
        } catch (Exception ex) {
            log.error("", ex);
        }
    }
}
