/**
 * Edit page tab of page editor tabbox.
 *
 * @asset(ncms/icon/16/misc/document-template.png)
 */
qx.Class.define("ncms.pgs.PageEditorEditPage", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    properties : {

        /**
         * Extended page spec used in this page tab.
         */
        "pageEditSpec" : {
            "check" : "Object",
            "apply" : "__applyPageEditSpec"
        }
    },

    construct : function() {
        this.base(arguments, this.tr("Edit"));
        this.setLayout(new qx.ui.layout.VBox(5));

        //Page name
        this.__pageNameLabel = new qx.ui.basic.Label();
        this.__pageNameLabel.setFont("headline");
        this.add(this.__pageNameLabel);

        this.__templateBf =
                new sm.ui.form.ButtonField(this.tr("Template"),
                        "ncms/icon/16/misc/document-template.png",
                        true).set({readOnly : true});
        this.__templateBf.setPlaceholder(this.tr("Please select the page template"));
        this.__templateBf.addListener("execute", this.__onChangeTemplate, this);
        this.add(this.__templateBf);


        this.addListener("loadPane", this.__onLoadPane, this);
    },

    members : {

        /**
         * Page template selector
         */
        __templateBf : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();
            this.__pageNameLabel.setValue(spec["name"]);
            //{"id":4,"name":"test"}
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.edit", spec),
                    "GET", "application/json");
            req.send(function(resp) {
                this.setPageEditSpec(resp.getContent());
            }, this);
        },

        __applyPageEditSpec : function(spec) {
            //{"id":4,"template":null,"attributes":[]}
            qx.log.Logger.info("Edit spec=" + JSON.stringify(spec));
            var t = spec["template"];
            if (t == null) {
                this.__templateBf.resetValue();
                return;
            }
            var sb = [];
            if (t["description"] != null) {
                sb.push(t["description"]);
            }
            if (t["name"] != null) {
                sb.push(t["name"]);
            }
            this.__templateBf.setValue(sb.join(" | "));
        },

        __onChangeTemplate : function() {
            var pspec = this.getPageSpec();
            var dlg = new ncms.asm.AsmSelectorDlg(this.tr("Please select the template page"), null, {
                "template" : true
            });
            dlg.addListener("completed", function(ev) {
                var t = ev.getData()[0];
                var url = ncms.Application.ACT.getRestUrl("pages.set.template", {
                    id : pspec["id"],
                    templateId : t["id"]
                });
                var req = new sm.io.Request(url, "PUT");
                req.send(function() {
                    var espec = sm.lang.Object.shallowClone(this.getPageEditSpec());
                    espec["template"] = t;
                    this.setPageEditSpec(espec);
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        }
    },

    destruct : function() {
        this.__templateBf = null;
    }
});