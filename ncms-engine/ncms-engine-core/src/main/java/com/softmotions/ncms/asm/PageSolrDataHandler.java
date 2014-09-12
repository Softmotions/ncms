package com.softmotions.ncms.asm;

import com.softmotions.ncms.asm.am.AsmAttributeManager;
import com.softmotions.ncms.asm.am.AsmAttributeManagersRegistry;
import com.softmotions.ncms.asm.events.AsmCreatedEvent;
import com.softmotions.ncms.asm.events.AsmModifiedEvent;
import com.softmotions.ncms.asm.events.AsmRemovedEvent;
import com.softmotions.ncms.events.NcmsEventBus;
import com.softmotions.weboot.solr.SolrDataHandler;

import com.google.common.collect.AbstractIterator;
import com.google.common.eventbus.Subscribe;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class PageSolrDataHandler implements SolrDataHandler {

    protected static final Logger log = LoggerFactory.getLogger(PageSolrDataHandler.class);

    private final AsmAttributeManagersRegistry aamr;

    private final NcmsEventBus ebus;

    private final SolrServer solr;

    private final AsmDAO adao;

    Collection<String> extraAttributeNames;

    @Inject
    public PageSolrDataHandler(AsmAttributeManagersRegistry aamr, NcmsEventBus ebus, SolrServer solr, AsmDAO adao) {
        this.aamr = aamr;
        this.ebus = ebus;
        this.solr = solr;
        this.adao = adao;
    }

    public void init(Configuration cfg) {
        String[] attrs = cfg.getStringArray("extra-attributes");
        if (attrs != null && attrs.length == 1 && "*".equals(attrs[0])) {
            extraAttributeNames = null;
        } else {
            extraAttributeNames = new ArrayList<>();
            if (attrs != null && attrs.length > 0) {
                extraAttributeNames.addAll(Arrays.asList(attrs));
            }
        }

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
        res.addField("name", asm.getHname());
        res.addField("description", asm.getDescription());
        res.addField("published", asm.isPublished());
        res.addField("type", asm.getType());

        for (String attrName : extraAttributeNames == null ? asm.getEffectiveAttributeNames() : extraAttributeNames) {
            AsmAttribute attr = asm.getEffectiveAttribute(attrName);
            if (attr != null) {
                AsmAttributeManager aam = aamr.getByType(attr.getType());
                if (aam != null) {
                    String[] strings = aam.prepareFulltextSearchData(attr);
                    for (String string : strings) {
                        if (string != null) {
                            res.addField("asm_attr_" + attrName, string);
                        }
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("SolrDocument: " + res);
        }

        return res;
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
