package com.softmotions.ncms.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.softmotions.ncms.asm.Asm;
import com.softmotions.ncms.asm.AsmAttribute;
import com.softmotions.ncms.asm.AsmDAO;
import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.asm.events.AsmCreatedEvent;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.ncms.mhttl.ImageMeta;
import com.softmotions.weboot.solr.SolrDataHandler;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */

@Singleton
public class PageSolrDataHandler implements SolrDataHandler {

    protected static final Logger log = LoggerFactory.getLogger(PageSolrDataHandler.class);

    protected static final Pattern ANNOTATION_BREAKER_PATTERN = Pattern.compile("[.;,:\\n]");

    protected static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    protected final AsmAttributeManagersRegistry aamr;

    protected final NcmsEventBus ebus;

    protected final SolrServer solr;

    protected final AsmDAO adao;

    protected float gfBoost;

    protected float dfBoost;

    protected int annotationLength;

    protected String[] annotationCandidates;

    protected Collection<String> extraAttributeNames;


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

    @Override
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
        gfBoost = cfg.getFloat("general-field-boost", 1.0F);
        dfBoost = cfg.getFloat("dynamic-field-boost", 1.0F);
        annotationCandidates = cfg.getStringArray("annotation-candidates");
        if (annotationCandidates == null) {
            annotationCandidates = ArrayUtils.EMPTY_STRING_ARRAY;
        }
        annotationLength = cfg.getInt("annotation-length", 300);
        ebus.register(this);
    }


    @Override
    public Iterator<SolrInputDocument> getData() {
        final Iterator asmsi = ((List) adao.select("asmSelectAllIds")).iterator();
        AtomicInteger cnt = new AtomicInteger(0);
        return new AbstractIterator<SolrInputDocument>() {
            @Override
            protected SolrInputDocument computeNext() {
                while (true) {
                    if (!asmsi.hasNext()) {
                        log.info("Indexed {} solr documents", cnt.get());
                        return endOfData();
                    }
                    SolrInputDocument solrDocument = asmToSolrDocument(adao.asmSelectById((Number) asmsi.next()));
                    if (solrDocument != null) {
                        if ((cnt.addAndGet(1) % 100) == 0) {
                            log.info("Indexed {} solr documents", cnt.get());
                        }
                        return solrDocument;
                    }
                }
            }
        };
    }

    @Nullable
    protected SolrInputDocument asmToSolrDocument(@Nullable Asm asm) {
        if (asm == null || StringUtils.isBlank(asm.getType())) {
            return null;
        }
        SolrInputDocument res = new SolrInputDocument();
        res.addField("id", String.valueOf(asm.getId()), gfBoost);
        res.addField("name", asm.getName(), gfBoost);
        res.addField("hname", asm.getHname(), gfBoost);
        res.addField("description", asm.getDescription(), gfBoost);
        res.addField("published", asm.isPublished(), gfBoost);
        res.addField("type", asm.getType(), gfBoost);
        if (asm.getCdate() != null) {
            res.addField("cdate", asm.getCdate().getTime(), gfBoost);
        }
        if (asm.getMdate() != null) {
            res.addField("mdate", asm.getMdate().getTime(), gfBoost);
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

        extractAnnotation(res);
        if (log.isDebugEnabled()) {
            log.debug("SolrDocument: {}", res);
        }
        return res;
    }

    protected void extractAnnotation(SolrInputDocument res) {
        String annotation = null;
        for (int i = 0; i < annotationCandidates.length && StringUtils.isBlank(annotation); ++i) {
            annotation = (String) res.getFieldValue(annotationCandidates[i]);
            if ("null".equals(annotation)) {
                annotation = null;
            }
        }
        if (annotation != null && !StringUtils.isBlank(annotation)) {
            if (annotation.length() > annotationLength) {
                Matcher matcher = ANNOTATION_BREAKER_PATTERN.matcher(annotation);
                int start;
                if (matcher.find(annotationLength / 2) && ((start = matcher.start()) < annotationLength)) {
                    annotation = annotation.substring(0, start + 1).trim();
                } else {
                    matcher = WHITESPACE_PATTERN.matcher(annotation);
                    if (matcher.find(annotationLength / 2) && ((start = matcher.start()) < annotationLength)) {
                        annotation = annotation.substring(0, start);
                    } else {
                        annotation = annotation.substring(0, annotationLength);
                    }
                }
            }
            annotation = StringEscapeUtils.escapeHtml4(annotation);
            if (!StringUtils.isBlank(annotation)) {
                res.addField("annotation", StringUtils.normalizeSpace(annotation.replaceAll("(\\n\\s*)+", "<br/>")));
            }
        }
    }

    protected void addData(SolrInputDocument sid, String prefix, String suffix, Object data) {
        //noinspection IfStatementWithTooManyBranches
        if (data == null) {
        } else if (data instanceof Long || data instanceof Integer) {
            sid.addField(prefix + "_l_" + suffix, data, dfBoost);
        } else if (data instanceof Boolean) {
            sid.addField(prefix + "_b_" + suffix, data, dfBoost);
        } else if (data instanceof ImageMeta) {
            sid.addField(prefix + "_image_" + suffix, SerializationUtils.serialize((ImageMeta) data), dfBoost);
        } else {
            sid.addField(prefix + "_s_" + suffix, data, dfBoost);
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

    protected void updateAsmInSolr(Long id) {
        if (id == null) {
            return;
        }
        updateAsmInSolr(id, adao.asmSelectById(id));
    }

    protected void updateAsmInSolr(Long id, @Nullable Asm asm) {
        SolrInputDocument solrDocument = asmToSolrDocument(asm);
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
