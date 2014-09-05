package com.softmotions.ncms.asm;

import com.softmotions.weboot.solr.SolrImportHandler;

import com.google.common.collect.AbstractIterator;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
public class PageSolrImportHandler implements SolrImportHandler {

    final SolrServer solr;

    final AsmDAO adao;

    @Inject
    public PageSolrImportHandler(SolrServer solr, AsmDAO adao) {
        this.solr = solr;
        this.adao = adao;
    }

    public void init() {
        // TODO: add listener for events
    }

    public Iterator<SolrInputDocument> getData() {
//        List<Asm> asms = adao.asmSelectAllPlain();
//        final Iterator<Asm> asmsi = asms.iterator();
        final Iterator asmsi = Arrays.asList(1, 2).iterator();

        return new AbstractIterator<SolrInputDocument>() {
            protected SolrInputDocument computeNext() {
                if (!asmsi.hasNext()) {
                    return endOfData();
                }
                return asmToSolrDocument((Integer) asmsi.next());
            }
        };
    }

//    private SolrInputDocument asmToSolrDocument(Asm asm) {
    private SolrInputDocument asmToSolrDocument(int id) {
        // TODO:
        SolrInputDocument res = new SolrInputDocument();
        res.addField("id", id);
        res.addField("title", "Name: " + id);
//        res.addField("Description", "Description: " + id);

//
//        SolrInputDocument res = new SolrInputDocument();
//        res.addField("id", asm.getId());
//        res.addField("name", asm.getName());
//        res.addField("core", asm.getCore());
//        res.addField("type", asm.getType());
//        res.addField("published", asm.isPublished());
//        res.addField("description", asm.getDescription());

        return res;
    }
}
