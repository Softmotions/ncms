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
         *   "creator" : "adam",
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
        this.__area = new qx.ui.form.TextArea();
        this._add(this.__area, {flex : 1});
    },

    members : {

        /**
         * File text data editor area.
         */
        __area : null,

        __applyFileSpec : function(spec) {
            if (spec == null) {
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


            //qx.log.Logger.info("spec=" + JSON.stringify(spec));
        },

        __cleanup : function() {
            this.__area.resetValue();
        }

    },

    destruct : function() {
        this.__area = null;
        //this._disposeObjects("__field_name");                                
    }
});