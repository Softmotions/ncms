/**
 * Media item editor/info panel
 *
 * @asset(ncms/icon/16/user/user-blue.png)
 */
qx.Class.define("ncms.mmgr.MediaFileEditor", {
    extend: qx.ui.core.Widget,

    events: {
        "fileMetaUpdated": "qx.event.type.Data"
    },

    properties: {

        /**
         * Example:
         * {"id":2,
         *   "name":"496694.png",
         *   "folder":"/test/",
         *   "content_type":"image/png",
         *   "content_length":10736,
         *   "creator" : "adam",
         *   "tags" : "foo, bar"
         *   }
         * or null
         */
        "fileSpec": {
            check: "Object",
            nullable: true,
            event: "changeFileSpec",
            apply: "__applyFileSpec"
        }
    },

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Grow());


        var topPane = new qx.ui.container.Composite(new qx.ui.layout.VBox(5));
        var form = this.__form = new qx.ui.form.Form();

        var el = new qx.ui.form.TextField();
        el.addListener("input", this.__setMetaDuty, this);
        el.addListener("changeValue", this.__flushMeta, this);
        el.setMaxLength(255);
        el.setPlaceholder(this.tr("Short file description").toString());
        form.add(el, this.tr("Description"), null, "description");

        el = new qx.ui.form.TextField();
        el.addListener("input", this.__setMetaDuty, this);
        el.addListener("changeValue", this.__flushMeta, this);
        el.setMaxLength(255);
        el.setPlaceholder(this.tr("Comma separated tags").toString());
        form.add(el, this.tr("Tags"), null, "tags");

        el = new sm.ui.form.ButtonField(this.tr("Change"), "ncms/icon/16/user/user-blue.png");
        el.addListener("changeValue", this.__flushMeta, this);
        el.setPlaceholder(this.tr("Choose the new file owner"));
        el.setReadOnly(true);
        el.addListener("execute", this.__selectOwner, this);
        form.add(el, this.tr("Owner"), null, "owner");


        var fr = new sm.ui.form.FlexFormRenderer(form).set({padding: [10, 5, 0, 5]});
        topPane.add(fr);

        this.__infoTable = this.__createInfoTable();
        topPane.add(this.__infoTable, {flex: 1});

        topPane.setContextMenu(new qx.ui.menu.Menu());
        topPane.addListener("beforeContextmenuOpen", this.__beforeInfoTableContextMenuOpen, this);
        
        var viewPane = this.__viewPane = new sm.ui.cont.LazyStack();

        viewPane.registerWidget("default", function () {
            return new qx.ui.core.Widget();
        });

        viewPane.registerWidget("thumbnail", function () {
            var comp = new qx.ui.container.Composite(
                new qx.ui.layout.HBox().set({alignX: "center", alignY: "middle"})
            );
            comp.add(new qx.ui.basic.Image().set(
                {
                    allowGrowX: false, allowGrowY: false,
                    allowShrinkX: true, allowShrinkY: true,
                    decorator: "main"
                }), {flex: 1});
            return comp;
        });

        viewPane.registerWidget("texteditor", function () {
            return new ncms.mmgr.MediaTextFileEditor({ui: "sideEditor", noAutoFocus: true});
        }, null, this);

        viewPane.setContextMenu(new qx.ui.menu.Menu());
        viewPane.addListener("beforeContextmenuOpen", this.__beforeViewPaneContextMenuOpen, this);

        var sp = new qx.ui.splitpane.Pane("vertical");
        sp.add(topPane, 0);
        sp.add(viewPane, 1);
        this._add(sp);

        this.hide();
    },

    members: {

        __isMetaDuty: false,

        __form: null,

        __infoTable: null,

        __viewPane: null,

        __owner: null,

        setUpdateFileMeta: function (update) {
            if (update == null) {
                return;
            }
            var items = this.__form.getItems();
            if (items[update["id"]]) {
                items[update["id"]].setValue(update["value"]);
            }

            var spec = {};
            qx.lang.Object.mergeWith(spec, this.getFileSpec(), false);
            spec[update["id"]] = update["value"];
            this.__updateInfoTable(spec);
        },

        __createInfoTable: function () {
            var tm = new sm.model.JsonTableModel();
            this.__setJsonInfoTableData(tm, []);
            var table = new sm.table.Table(tm, tm.getCustom());
            table.set({
                showCellFocusIndicator: false,
                statusBarVisible: false,
                focusCellOnPointerMove: true,
                height: 120,
                allowGrowY: true
            });
            return table;
        },

        __beforeInfoTableContextMenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var value = this.__getSelectedInfoTableValue();
            if (value != null) {
                var bt = new qx.ui.menu.Button(this.tr("Copy value"));
                bt.getContentElement().setAttribute("class", "copy_button");
                bt.getContentElement().setAttribute("data-clipboard-text", value);
                menu.add(bt);
            }

            var file = this.getFileSpec();
            if (file != null && file.folder != null && file.name != null) {
                bt = new qx.ui.menu.Button(this.tr("Copy path"));
                bt.getContentElement().setAttribute("class", "copy_button");
                bt.getContentElement().setAttribute("data-clipboard-text", file.folder + file.name);
                menu.add(bt);
            }
            
        },

        __getSelectedInfoTableValue: function () {
            var sind = this.__infoTable.getSelectionModel().getAnchorSelectionIndex();
            var info = sind != -1 ? this.__infoTable.getTableModel().getRowData(sind) : null;
            return (info != null && info[1] != null) ? info[1] : null;
        },

        __setJsonInfoTableData: function (tm, items) {
            tm.setJsonData({
                "columns": [
                    {
                        "title": this.tr("Attribute").toString(),
                        "id": "attribute",
                        "sortable": true,
                        "width": "1*"
                    },
                    {
                        "title": this.tr("Value").toString(),
                        "id": "value",
                        "sortable": true,
                        "width": "2*"
                    }
                ],
                "items": items
            });
        },

        __setupDataView: function (spec) {
            var ctype = spec["content_type"] || "";
            if (ncms.Utils.isImageContentType(ctype)) {
                var thumbnail = this.__viewPane.getWidget("thumbnail", true).getChildren()[0];
                this.__viewPane.showWidget("thumbnail");
                thumbnail.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail2", spec));
            } else if (ncms.Utils.isTextualContentType(ctype)) {
                var editor = this.__viewPane.getWidget("texteditor", true);
                editor.setFileSpec(this.getFileSpec());
                editor.setReadOnly(!this.__checkEditAccess(spec));
                this.__viewPane.showWidget("texteditor");
            } else {
                this.__viewPane.showWidget("default");
            }
        },

        __setupFileForm: function (spec) {
            this.__form.reset();
            var items = this.__form.getItems();
            if (spec["description"] != null) {
                items["description"].setValue(spec["description"]);
            }
            if (spec["tags"] != null) {
                items["tags"].setValue(spec["tags"]);
            }
            if (this.__owner) {
                items["owner"].setValue(this.__buildUserInfo(this.__owner));
            }

            var editAccess = this.__checkEditAccess(spec);
            items["description"].setReadOnly(!editAccess);
            items["tags"].setReadOnly(!editAccess);
            items["owner"].setEnabled(editAccess);
        },

        __setMetaDuty: function (val) {
            this.__isMetaDuty = (val == null) ? true : !!val;
        },

        __flushMeta: function (ev, spec) {
            spec = spec || this.getFileSpec();
            if (spec == null || !this.__isMetaDuty) {
                return;
            }
            if (!this.__checkEditAccess(spec)) {
                return;
            }

            this.__isMetaDuty = false;
            var items = this.__form.getItems();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.meta",
                {"id": spec["id"]}), "POST", "application/json");
            var description = items["description"].getValue();
            var tags = items["tags"].getValue();
            var owner = this.__owner;
            req.setShowMessages(false);
            if (description != null) {
                req.setParameter("description", description, true);
            }
            if (tags != null) {
                req.setParameter("tags", tags, true);
            }
            if (owner != null && owner["name"]) {
                req.setParameter("owner", owner["name"], true);
            }

            req.send(function () {
                var newSpec = {};
                qx.lang.Object.mergeWith(newSpec, spec);
                qx.lang.Object.mergeWith(newSpec, {
                    "description": description,
                    "tags": tags,
                    "owner": owner["name"],
                    "owner_fullName": owner["fullName"]
                });

                if (this.hasListener("fileMetaUpdated")) {
                    this.fireDataEvent("fileMetaUpdated", newSpec);
                }

                this.setFileSpec(newSpec);
            }, this);
        },

        __applyFileSpec: function (spec, old) {
            this.__flushMeta(null, old);
            if (spec == null) {
                this.hide();
                return;
            }
            this.__owner = {
                name: spec["owner"],
                fullName: spec["owner_fullName"]
            };

            this.__setupFileForm(spec);
            this.__updateInfoTable(spec);
            this.__setupDataView(spec);
            this.show();
        },

        __updateInfoTable: function (spec) {
            var aliases = {
                "content_type": this.tr("Content type").toString(),
                "content_length": this.tr("Size").toString(),
                "folder": this.tr("Folder").toString(),
                "name": this.tr("Name").toString(),
                "imageSize": this.tr("Image size").toString()
            };
            var attrs = [];
            Object.keys(spec).forEach(function (k) {
                var alias = aliases[k];
                if (alias == null) {
                    return;
                }
                attrs.push([
                    [alias, spec[k] != null ? spec[k] : ""],
                    null
                ]);
            }, this);

            this.__setJsonInfoTableData(this.__infoTable.getTableModel(), attrs);
        },

        __beforeViewPaneContextMenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("Preview"));
            bt.addListenerOnce("execute", this.__previewFile, this);
            menu.add(bt);

            bt = new qx.ui.menu.Button(this.tr("Download"));
            bt.addListenerOnce("execute", this.__downloadFile, this);
            menu.add(bt);
        },

        __previewFile: function (ev) {
            var f = this.getFileSpec();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = (f.folder + f.name).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.file", path);
            qx.bom.Window.open(url + "?inline=true", "NCMS:Media");
        },

        __downloadFile: function (ev) {
            var f = this.getFileSpec();
            if (f == null || f.folder == null || f.name == null) {
                return;
            }
            var path = (f.folder + f.name).split("/");
            var form = document.getElementById("ncms-download-form");
            form.action = ncms.Application.ACT.getRestUrl("media.file", path);
            form.submit();
        },

        __selectOwner: function () {
            var dlg = new ncms.usr.UserSelectorDlg(
                this.tr("Choose the file owner")
            );
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                dlg.destroy();
                this.__setOwner(data[0]);
            }, this);
            dlg.show();
        },

        __setOwner: function (user) {
            var items = this.__form.getItems();
            this.__setMetaDuty(true);
            this.__owner = user;
            items["owner"].setValue(this.__buildUserInfo(this.__owner));
        },

        __checkEditAccess: function (spec) {
            var appState = ncms.Application.APP_STATE;
            var user = appState.getUserLogin();
            // TODO: admin  role name to config/constant
            if (!appState.userHasRole("admin") && spec["owner"] != user) {
                return false;
            }

            return true;
        },

        __buildUserInfo: function (udata) {
            var user = [udata["name"]];
            if (udata["fullName"]) {
                user.push("|");
                user.push(udata["fullName"]);
            }
            return user.join(" ");
        }
    },

    destruct: function () {
        this.__viewPane = null;
        this.__infoTable = null;
        this.__owner = null;
        this._disposeObjects("__form");
        this.removeAllBindings();
    }
});