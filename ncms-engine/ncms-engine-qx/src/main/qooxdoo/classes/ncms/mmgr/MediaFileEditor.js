/**
 * Media item editor/info panel
 */
qx.Class.define("ncms.mmgr.MediaFileEditor", {
    extend : qx.ui.core.Widget,

    statics : {

        ATTR_ALIASES : null
    },

    events : {
    },

    properties : {

        /**
         * Example:
         * {"id":2,
         *   "name":"496694.png",
         *   "folder":"/test/",
         *   "content_type":"image/png",
         *   "content_length":10736,
         *   "creator" : "adam",
         *   "tags" :
         *   }
         * or null
         */
        "fileSpec" : {
            check : "Object",
            nullable : true,
            apply : "__applyFileSpec"
        }
    },

    construct : function() {
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

        el.setMaxLength(255);
        el = new qx.ui.form.TextField();
        el.addListener("input", this.__setMetaDuty, this);
        el.addListener("changeValue", this.__flushMeta, this);
        el.setPlaceholder(this.tr("Comma separated tags").toString());
        form.add(el, this.tr("Tags"), null, "tags");

        var fr = new sm.ui.form.FlexFormRenderer(form).set({padding : [10, 5, 0, 5]});
        topPane.add(fr);

        this.__infoTable = this.__createInfoTable();
        topPane.add(this.__infoTable);

        var viewPane = this.__viewPane = new sm.ui.cont.LazyStack();

        viewPane.registerWidget("default", function() {
            return new qx.ui.core.Widget();
        });

        viewPane.registerWidget("thumbnail", function() {
            var comp = new qx.ui.container.Composite(
                    new qx.ui.layout.HBox().set({alignX : "center", alignY : "middle"})
            );
            comp.add(new qx.ui.basic.Image().set({allowGrowX : false, allowGrowY : false, decorator : "main"}));
            return comp;
        });

        viewPane.registerWidget("texteditor", function() {
            return new qx.ui.core.Widget().set({backgroundColor : "red"});
        });

        var sp = new qx.ui.splitpane.Pane("vertical");
        sp.add(topPane, 0);
        sp.add(viewPane, 1);
        this._add(sp);

        this.hide();
    },

    members : {

        __isMetaDuty : false,

        __form : null,

        __infoTable : null,

        __viewPane : null,

        __createInfoTable : function() {
            var tm = new sm.model.JsonTableModel();
            this.__setJsonInfoTableData(tm, []);
            var table = new sm.table.Table(tm, tm.getCustom());
            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : false,
                focusCellOnMouseMove : true,
                height : 140});
            return table;
        },

        __setJsonInfoTableData : function(tm, items) {
            tm.setJsonData({
                "columns" : [
                    {
                        "title" : this.tr("Attribute").toString(),
                        "id" : "attribute",
                        "sortable" : true,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Value").toString(),
                        "id" : "value",
                        "sortable" : true,
                        "width" : "2*"
                    }
                ],
                "items" : items
            });
        },

        __setupDataView : function(spec) {
            var ctype = spec["content_type"] || "";
            var path = (spec["folder"] + spec["name"]).split("/");
            if (ncms.Utils.isImageContentType(ctype)) {
                var thumbnail = this.__viewPane.getWidget("thumbnail", true).getChildren()[0];
                thumbnail.setSource(ncms.Application.ACT.getRestUrl("media.thumbnail", path));
                this.__viewPane.showWidget("thumbnail");
            } else if (ncms.Utils.isTextualContentType(ctype)) {
                var editor = this.__viewPane.getWidget("texteditor");
                //todo init editor
                this.__viewPane.showWidget("texteditor");
            } else {
                this.__viewPane.showWidget("default");
            }
        },


        __setupFileForm : function(spec) {
            this.__form.reset();
            var items = this.__form.getItems();
            if (spec["description"] != null) {
                items["description"].setValue(spec["description"]);
            }
            if (spec["tags"] != null) {
                items["tags"].setValue(spec["tags"]);
            }
        },

        __setMetaDuty : function(val) {
            this.__isMetaDuty = (val == null) ? true : !!val;
        },

        __flushMeta : function(ev, spec) {
            spec = spec || this.getFileSpec();
            if (spec == null || !this.__isMetaDuty) {
                return;
            }
            this.__isMetaDuty = false;
            var items = this.__form.getItems();
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.meta", {"id" : spec["id"]}), "POST", "application/json");
            req.setParameter("description", items["description"].getValue(), true);
            req.setParameter("tags", items["tags"].getValue(), true);
            req.send();
        },

        __applyFileSpec : function(spec, old) {
            this.__flushMeta(null, old);
            if (spec == null) {
                this.hide();
                return;
            }
            var aliases = ncms.mmgr.MediaFileEditor.ATTR_ALIASES;
            var attrs = [];
            Object.keys(spec).forEach(function(k) {
                var alias = aliases[k];
                if (alias == null) {
                    return;
                }
                attrs.push([
                    [alias, spec[k] != null ? spec[k] : ""],
                    null
                ]);
            }, this);

            this.__setupFileForm(spec);
            this.__setJsonInfoTableData(this.__infoTable.getTableModel(), attrs);
            this.__setupDataView(spec);
            this.show();
        }
    },

    defer : function(statics, members) {
        var lm = qx.locale.Manager;
        statics.ATTR_ALIASES = {
            "content_type" : lm.tr("Content type").toString(),
            "content_length" : lm.tr("Size").toString(),
            "creator" : lm.tr("Creator").toString(),
            "folder" : lm.tr("Folder").toString(),
            "name" : lm.tr("Name").toString(),
            "imageSize" : lm.tr("Image size").toString()
        };
    },

    destruct : function() {
        this.__viewPane = null;
        this.__infoTable = null;
        this._disposeObjects("__form");
    }
});