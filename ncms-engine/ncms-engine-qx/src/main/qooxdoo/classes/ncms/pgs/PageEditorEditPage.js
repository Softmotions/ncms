/**
 * Edit page tab of page editor tabbox.
 *
 * @asset(ncms/icon/16/misc/document-template.png)
 * @asset(ncms/icon/16/misc/tick.png)
 * @asset(ncms/icon/16/misc/cross-script.png)
 * @asset(ncms/icon/16/misc/monitor.png)
 * @asset(ncms/icon/16/misc/light-bulb.png)
 * @asset(ncms/icon/16/misc/light-bulb-off.png)
 * @asset(ncms/icon/16/misc/images.png)
 * @asset(ncms/icon/16/misc/puzzle.png)
 * @asset(ncms/icon/16/misc/tick-button.png)
 * @asset(ncms/icon/16/misc/disk.png)
 * @asset(ncms/icon/32/exclamation.png)
 */
qx.Class.define("ncms.pgs.PageEditorEditPage", {
    extend: qx.ui.tabview.Page,
    include: [ncms.pgs.MPageEditorPane],

    properties: {

        /**
         * Extended page spec used in this page tab.
         */
        "pageEditSpec": {
            "check": "Object",
            "apply": "__applyPageEditSpec",
            init: null
        },

        /**
         * True if page editor interface is blocked
         * due to another user page lock.
         */
        "pageBlocked": {
            "check": "Boolean",
            "init": false,
            "apply": "__applyPageBlocked"
        }
    },

    construct: function () {
        this.base(arguments, this.tr("Edit"));
        this.setLayout(new qx.ui.layout.VBox(1));

        var header = new qx.ui.container.Composite(new qx.ui.layout.VBox(5))
        .set({padding: [0, 5, 0, 0]});

        //Blocker
        this.__alertBlocker = new ncms.cc.AlertBlocker(this);

        //Page name
        this.__pageNameLabel = new qx.ui.basic.Label();
        this.__pageNameLabel.setFont("headline");
        header.add(this.__pageNameLabel);

        // First row
        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));
        var bt = this.__publishPageBt = new qx.ui.form.ToggleButton(null, "ncms/icon/16/misc/light-bulb-off.png");
        bt.addListener("execute", this.__publishPage, this);
        hcont.add(bt);

        bt = this.__publishChangesBt = new qx.ui.form.Button(null, "ncms/icon/16/misc/tick-button.png");
        bt.setEnabled(false);
        bt.setToolTipText(this.tr("Publish page changes"));
        bt.addListener("execute", this.__publishChanges, this);
        hcont.add(bt);

        bt = this.__saveBt = new qx.ui.form.Button(this.tr("Save"), "ncms/icon/16/misc/disk.png").set({width: 110});
        bt.setEnabled(false);
        bt.addListener("execute", this.__save, this);
        hcont.add(bt);
        this.__saveSc = new sm.bom.ExtendedShortcut("Ctrl+S", false, this);
        this.__saveSc.addListener("execute", this.__save, this);

        bt = this.__cancelBt = new qx.ui.form.Button(this.tr("Cancel"), "ncms/icon/16/misc/cross-script.png");
        bt.setEnabled(false);
        bt.addListener("execute", this.__cancel, this);
        hcont.add(bt);

        var epoins = ncms.Application.extensionPoints("ncms.pgs.PageEditorEditPage.HEADER_BUTTONS");
        if (epoins.length > 0) {
            var menu = new qx.ui.menu.Menu();
            var embt = new qx.ui.form.MenuButton(null, "ncms/icon/16/misc/puzzle.png", menu);
            hcont.add(embt);
            epoins.forEach(function (ep) {
                ep(this, menu);
            }, this);
        }
        header.add(hcont);

        // Second row
        var hcont2 = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));
        bt = this.__previewBt = new qx.ui.form.Button(null, "ncms/icon/16/misc/monitor.png");
        bt.setToolTipText(this.tr("Preview"));
        bt.addListener("execute", this.__preview, this);
        hcont2.add(bt);

        bt = new qx.ui.form.Button(null, "ncms/icon/16/misc/images.png");
        bt.setToolTipText(this.tr("Page files"));
        bt.addListener("execute", this.__files, this);
        hcont2.add(bt);

        this.__templateBf =
            new sm.ui.form.ButtonField(this.tr("Template"),
                "ncms/icon/16/misc/document-template.png",
                true).set({readOnly: true});
        this.__templateBf.getMainButton().set({width: 110});
        this.__templateBf.setPlaceholder(this.tr("Please select the template page"));
        this.__templateBf.setToolTipText(this.tr("Please select the template page"));
        this.__templateBf.addListener("execute", this.__onChangeTemplate, this);
        hcont2.add(this.__templateBf, {flex: 1});
        header.add(hcont2);
        this.add(header);

        this.addListener("loadPane", this.__onLoadPane, this);
        this.addListener("clearPane", this.__onClearPane, this);
        var events = ncms.Events.getInstance();
        events.addListener("pageLocked", this.__onPageLocked, this);
        events.addListener("pageUnlocked", this.__onPageUnlocked, this);
        events.addListener("pageEdited", this.__onPageEdited, this);
        events.addListener("pageChangePublished", this.__onPageEdited, this);
        events.addListener("pageChangeTemplate", this.__onPageEdited, this);
    },

    members: {

        /**
         * Page template selector
         */
        __templateBf: null,

        /**
         * Page controls container
         */
        __scroll: null,

        /**
         * Current page form
         */
        __form: null,

        __saveBt: null,

        __cancelBt: null,

        __publishPageBt: null,

        __publishChangesBt: null,

        __previewBt: null,

        __saveSc: null,

        __alertBlocker: null,

        getForm: function () {
            return this.__form;
        },

        __onPageEdited: function (ev) {
            var myspec = this.getPageSpec(),
                evspec = ev.getData();
            if (myspec != null && evspec != null
                && myspec["id"] === evspec["id"]
                && (evspec.hints["app"] !== ncms.Application.UUID || evspec.hints["asmui"] === true)) {
                this.__refresh();
            }
        },

        __onPageLocked: function (ev) {
            var data = ev.getData(),
                ps = this.getPageEditSpec();
            if (ps != null && ps.id == data.id) {
                var user = ncms.Application.getUserId();
                ps.lockUser = data.user;
                this.setPageBlocked(data.user != user);
            }
        },

        __onPageUnlocked: function (ev) {
            var data = ev.getData(),
                ps = this.getPageEditSpec();
            if (ps != null && ps.id == data.id) {
                ps.lockUser = null;
                this.setPageBlocked(false);
            }
        },

        __applyPageBlocked: function (val, old) {
            if (val === old) {
                return;
            }
            if (val) {
                var ps = this.getPageEditSpec();
                this.__alertBlocker.block(this.tr(
                    "This page is locked<br>since the user <b>%1</b> is editing it",
                    ps.lockUser
                ));
                this.addListenerOnce("disappear", function () {
                    this.__alertBlocker.unblock();
                    this.addListenerOnce("appear", function () {
                        this.__alertBlocker.onAppear(this.getPageBlocked());
                    }, this);
                }, this);
            } else {
                this.__alertBlocker.unblock();
            }
        },

        __onLoadPane: function (ev) {
            this.setPageBlocked(false);
            var spec = ev.getData();
            this.__pageNameLabel.setValue(spec["name"]);
            //{"id":4,"name":"test"}
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.edit", spec),
                "GET", "application/json");
            req.send(function (resp) {
                var nspec = resp.getContent();
                if (spec.$keepModified) {
                    nspec.$keepModified = spec.$keepModified;
                }
                this.setPageEditSpec(nspec);
            }, this);
        },

        __onClearPane: function (ev) {
            this.setPageBlocked(false);
        },

        __applyPageEditSpec: function (spec) {
            this.__setPublishState(!!spec["published"]);
            var fp = spec["firstParent"];
            var t = spec["template"];
            var attrs = spec["attributes"] || [];
            if (t == null) {
                this.__templateBf.resetValue();
                this.__cleanupFormPane();
                this.__syncState();
                return;
            }
            var sb = [];
            if (t["name"] != null) {
                sb.push(t["name"]);
            }
            if (fp && fp.id != t.id) {
                if (fp.name != null) {
                    sb.push(fp.name);
                }
            }
            if (sb.length < 2 && t.description != null) {
                sb.push(t.description);
            }
            this.__templateBf.setValue(sb.join(" | "));
            var form = new sm.ui.form.ExtendedForm();
            var vmgr = form.getValidationManager();
            vmgr.setRequiredFieldMessage(this.tr("This field is required"));
            attrs.forEach(function (attrSpec) {
                this.__processAttribute(attrSpec, spec, form);
            }, this);

            this.__cleanupFormPane();
            this.__form = form;
            this.__scroll = new qx.ui.container.Scroll().set({marginTop: 5});
            var fr = new sm.ui.form.OneColumnFormRenderer(form).set({paddingRight: 15});
            this.__scroll.add(fr);
            this.add(this.__scroll, {flex: 1});

            this.__syncState();
            if (!spec.$keepModified) {
                this.setModified(false);
            } else {
                delete spec.$keepModified;
            }
        },

        __cleanupFormPane: function () {
            if (this.__form != null) {
                try {
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
                        try {
                            w.destroy();
                        } catch (e) {
                            qx.log.Logger.error(e);
                        }
                    }
                } catch (ignored) {
                } finally {
                    this.__form.dispose();
                    this.__form = null;
                }
            }
            if (this.__scroll) {
                this.__scroll.destroy();
                this.__scroll = null;
            }
        },

        __processAttribute: function (attrSpec, asmSpec, form) {
            var am = ncms.asm.am.AsmAttrManagersRegistry.createAttrManagerInstanceForType(attrSpec["type"]);
            if (am == null) {
                qx.log.Logger.warn("Missing attribute manager for type: " + attrSpec["type"]);
                return;
            }
            var w = am.activateValueEditorWidget(attrSpec, asmSpec);
            if (w == null) {
                qx.log.Logger.warn(
                    "Attribute manager used for type: " + attrSpec["type"] + " produced invalid widget: null");
                return;
            }
            var wclass = qx.Class.getByName(w.classname);
            if (wclass == null) {
                qx.log.Logger.warn(
                    "Attribute manager used for type: " + attrSpec["type"] + " produced invalid widget: " + w);
                return;
            }
            var oou = qx.util.OOUtil;
            var awclass = wclass;
            var aw = w;
            if (w.getUserData("ncms.asm.activeWidget") != null) {
                aw = w.getUserData("ncms.asm.activeWidget");
                awclass = qx.Class.getByName(aw.classname);
                if (awclass == null) {
                    qx.log.Logger.warn("Attribute manager used for type: " + attrSpec["type"] +
                        " produced invalid 'ncms.asm.activeWidget' widget: " + aw);
                    return;
                }
                w.setUserData("ncms.asm.activeWidget", null);
            }

            //Listen modified events
            if (qx.Class.hasInterface(awclass, ncms.asm.am.IValueWidget)) {
                aw.addListener("modified", this._onModifiedWidget, this);
                aw.addListener("requestSave", this._onRequestSavePage, this);
            } else if (oou.supportsEvent(awclass,
                    "input") && !((typeof aw.getReadOnly === "function") && aw.getReadOnly() === true)) {
                aw.addListener("input", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "changeValue")) {
                aw.addListener("changeValue", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "changeSelection")) {
                aw.addListener("changeSelection", this._onModifiedWidget, this);
            } else if (oou.supportsEvent(awclass, "execute")) {
                aw.addListener("execute", this._onModifiedWidget, this);
            }

            if (!qx.Class.hasInterface(wclass, qx.ui.form.IForm)) {
                w = new sm.ui.form.FormWidgetAdapter(w);
            }

            w.setUserData("attributeManager", am);
            var validator = null;
            if (typeof w.getUserData("ncms.asm.validator") === "function") {
                validator = w.getUserData("ncms.asm.validator");
            } else if (typeof w.getValidator === "function") {
                validator = w.getValidator();
            }

            form.add(w, attrSpec["label"] + " (" + attrSpec["name"] + ")", validator, attrSpec["name"], w);
        },

        _onModifiedWidget: function (ev) {
            var w = ev.getTarget();
            if (w != null) {
                if (w.getEnabled() === false || w.hasState("widgetNotReady")) {
                    return;
                }
            }
            this.setModified(true);
        },

        _onRequestSavePage: function (ev) {
            this.__save();
        },

        _applyModified: function (val, old) {
            if (val) {
                var ps = this.getPageSpec();
                ncms.pgs.PageLocker.getInstance().lock({
                    id: ps["id"],
                    reportErrors: true
                }, function (ret) {
                    var success = !!(ret && ret.success);
                    if (!success) {
                        this.setModified(false);
                    }
                    this.__saveBt.setEnabled(success);
                    this.__cancelBt.setEnabled(success);
                    this.__syncState();
                }, this);
            } else {
                this.__saveBt.setEnabled(val);
                this.__cancelBt.setEnabled(val);
                this.__syncState();
            }
        },

        __onChangeTemplate: function () {
            var me = this;
            var ps = this.getPageSpec();
            var dlg = new ncms.pgs.PagesSelectorDlg(
                this.tr("Please select the template page"),
                false, {
                    pageId: ps.id,
                    asmOpts: {
                        title: me.tr("Templates"),
                        useColumns: ["description", "name"],
                        constViewSpec: {
                            template: true,
                            pageId: ps.id
                        }
                    }
                }
            );
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                //Completed: {"id":81,"template":1,"assembly":true,"dblClick":true}
                //Completed: {"id":101,"name":"Вконтакте","accessMask":"ownd",
                //            "idPath":[1,101],"labelPath":["Лендинги","Вконтакте"],
                //            "guidPath":["d76200ca883563e4d971e38951b332c0","12d5c7a0c3167d3d21d30f1c43368b32"]}
                var url = ncms.Application.ACT.getRestUrl("pages.set.template", {
                    id: ps["id"],
                    templateId: data["id"]
                });
                var req = new sm.io.Request(url, "PUT", "application/json");
                req.send(function (resp) {
                    this.setPageEditSpec(resp.getContent());
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        },

        __syncState: function () {
            var es = this.getPageEditSpec();
            if (es["lockUser"] != null
                && es["lockUser"] != ncms.Application.getUserId()) {
                this.setPageBlocked(true);
            }
            var hasCore = (this.__form != null && es != null && es["core"] != null);
            this.__previewBt.setEnabled(hasCore && this.getEnabled());
            this.__publishPageBt.setEnabled(hasCore && this.getEnabled());
        },

        __save: function (cb) {
            var me = this;
            if (this.__saveBt.getEnabled() === false) {
                if (typeof cb === "function") {
                    cb(false);
                }
                return;
            }
            if (this.__form == null || !this.__form.validate()) {
                ncms.Application.infoPopup(this.tr("Page fields contain errors"), {
                    showTime: 5000,
                    icon: "ncms/icon/32/exclamation.png"
                });
                return;
            }
            this.__saveBt.setEnabled(false);
            try {
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
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.edit", spec),
                    "PUT", "application/json");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify(data));
                req.setMessageHandler(function (isError, messages) {
                    var message = me.tr("Failed to save page.");
                    if (messages != null && messages.length > 0) {
                        message += "<br/>" + messages.join("<br/>");
                    }
                    if (isError) {
                        ncms.Application.errorPopup(message);
                    } else {
                        ncms.Application.infoPopup(message);
                    }
                });
                req.addListener("error", function (ev) {
                    this.__saveBt.setEnabled(true);
                    if (typeof cb === "function") {
                        cb(true);
                    }
                }, this);
                req.send(function (resp) {
                    var ecore = resp.getContent();
                    var espec = this.getPageEditSpec();
                    espec["core"] = ecore;
                    this.__syncState();
                    this.setModified(false);
                    ncms.Application.infoPopup(this.tr("Page saved successfully"));
                    if (typeof cb === "function") {
                        cb(false);
                    }
                }, this);
            } catch (e) {
                this.__saveBt.setEnabled(true);
                ncms.Application.errorPopup(this.tr("Failed to save page. Error: %1",
                    (e != null && e.message) ? e.message : e));
                if (typeof cb === "function") {
                    cb(true);
                }
            }
        },

        __publishPage: function (ev) {
            var val = ev.getTarget().getValue();
            if (!val) { //unpublish
                this.__unpublishPrompt();
                return;
            }

            this.__doPublish(val); //publish
        },

        __publishChanges: function (ev) {
            // todo implement it!
        },

        __doPublish: function (publish) {
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl(
                publish ? "pages.publish" : "pages.unpublish",
                {"id": this.getPageSpec()["id"]}), "PUT");
            req.send(function () {
                var ps = this.getPageEditSpec();
                ps["published"] = true;
                this.__setPublishState(publish);
            }, this);
        },

        __unpublishPrompt: function () {
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl(
                "pages.referrers.count", {"id": this.getPageSpec()["id"]}), "GET");
            req.send(function (resp) {
                var rc = resp.getContent();
                if (rc > 0) {
                    ncms.Application.confirm(
                        this.tr(
                            "Are you sure to unpublish this page? There are pages linked with this page. Please see the <a href=\"%1\" target='_blank' rel='noopener noreferrer'>list of linked pages</a>",
                            ncms.Application.ACT.getRestUrl("pages.referrers", {guid: this.getPageEditSpec()["guid"]})),
                        function (yes) {
                            if (!yes) {
                                this.__publishPageBt.setValue(true);
                                return;
                            }

                            this.__doPublish(false);
                        }, this);
                } else {
                    this.__doPublish(false);
                }
            }, this);
        },

        __setPublishState: function (val) {
            this.__publishPageBt.setToolTipText(val ? this.tr("Published") : this.tr("Not published"));
            this.__publishPageBt.setIcon(
                val ? "ncms/icon/16/misc/light-bulb.png" : "ncms/icon/16/misc/light-bulb-off.png");
            this.__publishPageBt.setValue(val);
        },

        __cancel: function () {
            ncms.Application.confirm(this.tr("Dow you really want to dispose pending changes?"), function (yes) {
                if (yes) {
                    var ps = this.getPageSpec();
                    ncms.pgs.PageLocker.getInstance().unlock({
                        id: ps["id"],
                        reportErrors: true
                    }, function () {
                        this.setPageSpec(sm.lang.Object.shallowClone(this.getPageSpec()))
                    }, this);
                }
            }, this);
        },

        __refresh: function () {
            var nspec = sm.lang.Object.shallowClone(this.getPageSpec());
            nspec.$keepModified = true;
            this.setPageSpec(nspec);
        },

        __files: function () {
            var ps = this.getPageSpec();
            var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                ps["id"],
                this.tr("Organize the page files"), {
                    allowModify: true,
                    linkText: false,
                    noActions: true,
                    smode: qx.ui.table.selection.Model.MULTIPLE_INTERVAL_SELECTION
                });
            dlg.open();
        },

        __preview: function () {
            var me = this;
            this.__save(function (err) {
                if (err) {
                    return;
                }
                var pp = ncms.Application.ACT.getRestUrl("pages.preview", me.getPageEditSpec());
                qx.bom.Window.open(pp, "NCMS:Preview");
            });
        }
    },

    destruct: function () {
        var events = ncms.Events.getInstance();
        events.removeListener("pageLocked", this.__onPageLocked, this);
        events.removeListener("pageUnlocked", this.__onPageUnlocked, this);
        events.removeListener("pageEdited", this.__onPageEdited, this);
        events.removeListener("pageChangePublished", this.__onPageEdited, this);
        events.removeListener("pageChangeTemplate", this.__onPageEdited, this);
        events.removeListener("pageChangeTemplate", this.__onPageEdited, this);
        this._disposeObjects("__alertBlocker");
        this.__cleanupFormPane();
        this.__templateBf = null;
        this.__form = null;
        this.__scroll = null;
        this.__previewBt = null;
        this.__saveBt = null;
        this.__cancelBt = null;
        this.__publishPageBt = null;
        this.__publishChangesBt = null;
        this.__saveSc = null;
    }
});
