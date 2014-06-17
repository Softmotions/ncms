/**
 * Edit page tab of page editor tabbox.
 *
 * @asset(ncms/icon/16/misc/document-template.png)
 * @asset(ncms/icon/16/misc/tick.png)
 * @asset(ncms/icon/16/misc/monitor.png)
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

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));
        hcont.setPadding(5);

        this.__templateBf =
                new sm.ui.form.ButtonField(this.tr("Template"),
                        "ncms/icon/16/misc/document-template.png",
                        true).set({readOnly : true});
        this.__templateBf.setPlaceholder(this.tr("Please select the page template"));
        this.__templateBf.addListener("execute", this.__onChangeTemplate, this);
        hcont.add(this.__templateBf, {flex : 1});

        bt = this.__previewBt = new qx.ui.form.Button(this.tr("Preview"), "ncms/icon/16/misc/monitor.png");
        bt.addListener("execute", this.__preview, this);
        hcont.add(bt);

        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Save"), "ncms/icon/16/misc/tick.png");
        bt.addListener("execute", this.__save, this);
        hcont.add(bt);

        this.add(hcont);

        this.__scroll = new qx.ui.container.Scroll().set({marginTop : 20});
        this.add(this.__scroll, {flex : 1});


        this.addListener("loadPane", this.__onLoadPane, this);
    },

    members : {

        /**
         * Page template selector
         */
        __templateBf : null,

        /**
         * Page controls container
         */
        __scroll : null,

        /**
         * Current page form
         */
        __form : null,

        __saveBt : null,

        __previewBt : null,

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
            var t = spec["template"];
            if (t == null) {
                this.__templateBf.resetValue();
                this.__cleanupFormPane();
                this.__syncState();
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

            var attrs = spec["attributes"] || [];
            var form = new sm.ui.form.ExtendedForm();
            var vmgr = form.getValidationManager();
            vmgr.setRequiredFieldMessage(this.tr("This field is required"));

            attrs.forEach(function(aspec) {
                this.__processAttribute(aspec, form);
            }, this);

            this.__cleanupFormPane();
            var fr = new sm.ui.form.FlexFormRenderer(form);
            this.__form = form;
            this.__scroll.add(fr);
            this.__syncState();
        },


        __cleanupFormPane : function() {
            if (this.__scroll) {
                this.__scroll.getChildren().forEach(function(c) {
                    c.destroy();
                });
            }
            if (this.__form != null) {
                var items = this.__form.getItems();
                for (var k in items) {
                    var w = items[k];
                    var am = w.getUserData("attributeManager");
                    w.setUserData("attributeManager", null);
                    if (am) {
                        try {
                            am.dispose();
                        } catch (e) {
                            qx.log.Logger.error(e);
                        }
                    }
                }
                this.__form = null;
            }
        },

        __processAttribute : function(aspec, form) {
            var am = ncms.asm.AsmAttrManagersRegistry.createAttrManagerInstanceForType(aspec["type"]);
            if (am == null) {
                qx.log.Logger.warn("Missing attribute manager for type: " + aspec["type"]);
                return;
            }
            var w = am.activateValueEditorWidget(aspec);
            if (w == null) {
                qx.log.Logger.warn("Attribute manager used for type: " + aspec["type"] + " produced invalid widget: null");
                return;
            }
            var wclass = qx.Class.getByName(w.classname);
            if (wclass == null) {
                qx.log.Logger.warn("Attribute manager used for type: " + aspec["type"] + " produced invalid widget: " + w);
                return;
            }
            if (!qx.Class.hasInterface(wclass, qx.ui.form.IForm)) {
                w = new sm.ui.form.FormWidgetAdapter(w);
            }
            w.setUserData("attributeManager", am);
            var validator = null;
            if (typeof w.validator === "function") {
                validator = w.validator;
            }
            form.add(w, aspec["label"], validator, aspec["name"]);
        },

        __onChangeTemplate : function() {
            var pspec = this.getPageSpec();
            var dlg = new ncms.asm.AsmSelectorDlg(this.tr("Please select the template page"), null, {
                "template" : true
            }, ["description", "name"]);
            dlg.addListener("completed", function(ev) {
                var t = ev.getData()[0];
                var url = ncms.Application.ACT.getRestUrl("pages.set.template", {
                    id : pspec["id"],
                    templateId : t["id"]
                });
                var req = new sm.io.Request(url, "PUT", "application/json");
                req.send(function(resp) {
                    this.setPageEditSpec(resp.getContent());
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        },

        __syncState : function() {
            this.__previewBt.setEnabled(this.__form != null);
            this.__saveBt.setEnabled(this.__form != null);
        },

        __save : function() {
            if (this.__form == null || !this.__form.validate()) {
                return;
            }
            var data = {};
            var items = this.__form.getItems();
            for (var k in items) {
                var w = items[k];
                var am = w.getUserData("attributeManager");
                if (am == null) {
                    continue;
                }
                data[k] = am.valueAsJSON();
            }

            var spec = this.getPageSpec();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.edit", spec), "PUT");
            req.setRequestContentType("application/json");
            req.setData(JSON.stringify(data));
            req.send(function(resp) {
                ncms.Application.alert(this.tr("Page '%1' saved successfully", spec["name"]));
            }, this);
        },

        __preview : function() {
            //todo
        }
    },

    destruct : function() {
        this.__cleanupFormPane();
        this.__templateBf = null;
        this.__form = null;
        this.__scroll = null;
        this.__previewBt = null;
        this.__saveBt = null;
    }
});