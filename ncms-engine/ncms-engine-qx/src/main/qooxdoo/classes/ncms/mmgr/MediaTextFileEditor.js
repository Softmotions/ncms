/**
 * Text media file widget.
 */
qx.Class.define("ncms.mmgr.MediaTextFileEditor", {
    extend : qx.ui.core.Widget,

    statics : {
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
         *   "owner" : "adam",
         *   "tags" : "foo, bar"
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
        this._setLayout(new qx.ui.layout.VBox(5));
        this.__broadcaster = sm.event.Broadcaster.create({"enabled" : false});
        this.__area = new qx.ui.form.TextArea();
        this._add(this.__area, {flex : 1});
        this.__area.addListener("input", function() {
            this.__broadcaster.setEnabled(true);
        }, this);

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));
        hcont.setPadding([0, 5, 5, 0]);
        var bt = new qx.ui.form.Button(this.tr("Save"));
        this.__broadcaster.attach(bt, "enabled");
        bt.addListener("execute", this.__save, this);
        hcont.add(bt, {flex : 1});
        this._add(hcont);
    },

    members : {

        /**
         * File text data editor area.
         */
        __area : null,

        __broadcaster : null,

        setReadOnly : function(state) {
            this.__area.setReadOnly(state);
            this.__broadcaster.setEnabled(false);
        },

        __save : function() {
            var spec = this.getFileSpec();
            if (spec == null || !ncms.Utils.isTextualContentType(spec["content_type"])) {
                return;
            }
            var text = this.__area.getValue();
            var path = (spec["folder"] + spec["name"]).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.upload", path);
            var ctype = spec["content_type"] || "text/plain";
            ctype = ctype.split(";")[0];
            var req = new sm.io.Request(url, "PUT", "application/json");
            req.setRequestContentType(ctype);
            req.setData(text);
            this.__broadcaster.setEnabled(false);
            req.send();
        },

        __applyFileSpec : function(spec) {
            if (spec == null || !ncms.Utils.isTextualContentType(spec["content_type"])) {
                this.__cleanup();
                return;
            }
            var path = (spec.folder + spec.name).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.file", path);
            var req = new sm.io.Request(url, "GET", "text/plain");
            req.send(function(resp) {
                var text = resp.getContent();
                if (text == null) {
                    text = "";
                }
                this.__area.setValue(text);
            }, this);
        },

        __cleanup : function() {
            this.__area.resetValue();
        }

    },

    destruct : function() {
        this.__area = null;
        this.__broadcaster.destruct();
        //this._disposeObjects("__field_name");
    }
});