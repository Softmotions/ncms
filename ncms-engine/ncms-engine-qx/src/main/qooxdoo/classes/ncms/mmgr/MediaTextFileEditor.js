/**
 * Text media file widget.
 *
 * @asset(ncms/icon/16/misc/disk.png)
 * @ignore(ace.*, require)
 */
qx.Class.define("ncms.mmgr.MediaTextFileEditor", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],


    statics: {},

    events: {},

    properties: {

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
        "fileSpec": {
            check: "Object",
            nullable: true,
            apply: "__applyFileSpec"
        }
    },

    /**
     * @param fileSpec {Object?} Optional file specification.
     */
    construct: function (fileSpec) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(5));
        this.__broadcaster = sm.event.Broadcaster.create({"enabled": false});


        var badIE = qx.core.Environment.get("engine.name") == "mshtml";
        if (badIE) {
            badIE = parseFloat(qx.core.Environment.get("browser.version")) <= 8 ||
                qx.core.Environment.get("browser.documentmode") <= 8;
        }
        var canAce = !(!document.createElement("div").getBoundingClientRect || badIE || !window.ace);
        if (!canAce) {
            this.__area = new qx.ui.form.TextArea().set({font: "monospace", wrap: false});
            this._add(this.__area, {flex: 1});
            this.__area.addListener("input", function () {
                this.__broadcaster.setEnabled(true);
            }, this);
        } else { // setup ace editor
            this.__aceContainer = new qx.ui.core.Widget();
            this.__aceContainer.addListenerOnce("appear", this.__onAceContainerAppear, this);
            this._add(this.__aceContainer, {flex: 1});
        }

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));
        hcont.setPadding([0, 5, 5, 0]);
        var bt = new qx.ui.form.Button(this.tr("Save"), "ncms/icon/16/misc/disk.png");
        this.__broadcaster.attach(bt, "enabled");
        bt.addListener("execute", this.__save, this);
        hcont.add(bt, {flex: 1});
        this._add(hcont);

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Control+S"),
            this.__save, this);
        this._registerCommandFocusWidget(this);

        if (fileSpec != null) {
            this.setFileSpec(fileSpec)
        }
    },

    members: {

        /**
         * File text data editor area.
         */
        __area: null,

        /**
         * Ace editor containers
         */
        __aceContainer: null,

        /**
         * Activated ace editor
         */
        __ace: null,

        __pendigCode: null,

        __pendigRo: null,

        __readOnly: false,

        __broadcaster: null,

        setReadOnly: function (state) {
            this.__readOnly = !!state;
            if (this.__area) {
                this.__area.setReadOnly(state);
            } else if (this.__ace) {
                this.__ace.setReadOnly(state);
            } else {
                this.__pendigRo = state
            }
            this.__broadcaster.setEnabled(false);
        },

        __save: function () {
            var spec = this.getFileSpec();
            if (spec == null || !ncms.Utils.isTextualContentType(spec["content_type"])) {
                return;
            }
            var text = this.__getCode();
            var path = (spec["folder"] + spec["name"]).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.upload", path);
            var ctype = spec["content_type"] || "text/plain";
            ctype = ctype.split(";")[0];
            var req = new sm.io.Request(url, "PUT", "application/json");
            req.setRequestContentType(ctype);
            req.setData(text);
            this.__broadcaster.setEnabled(false);
            req.send(function() {
                ncms.Application.infoPopup(this.tr("File successfully saved"));
            }, this);
        },

        __applyFileSpec: function (spec) {
            this.__cleanup();
            if (spec == null || !ncms.Utils.isTextualContentType(spec["content_type"])) {
                return;
            }

            var path = (spec.folder + spec.name).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.file", path);
            var req = new sm.io.Request(url, "GET", "text/plain");

            req.send(function (resp) {
                var fname = spec["name"] || "";
                var ctype = spec["content_type"] || "";
                var text = resp.getContent();
                if (text == null) {
                    text = "";
                }
                var lclass = null;
                if (ctype.indexOf("text/html") !== -1 || qx.lang.String.endsWith(fname, ".httl")) {
                    lclass = "html";
                } else if (ctype.indexOf("application/javascript") !== -1) {
                    lclass = "javascript";
                } else if (ctype.indexOf("application/xml") !== -1) {
                    lclass = "xml";
                } else if (ctype.indexOf("text/css") !== -1) {
                    lclass = "css";
                } else if (ctype.indexOf("application/json") !== -1) {
                    lclass = "json"
                } else {
                    lclass = "text";
                }
                this.__setCode(text, lclass);
            }, this);
        },


        __getCode: function () {
            if (this.__area) {
                return this.__area.getValue();
            } else if (this.__ace) {
                return this.__ace.getSession().getValue();
            }
        },

        __setCode: function (code, lclass) {
            if (this.__area) {
                this.__area.setValue(code);
            } else if (this.__ace) {
                var ace = this.__ace;
                var sess = ace.getSession();
                if (lclass != null) {
                    var Mode = require("ace/mode/" + lclass).Mode;
                    sess.setMode(new Mode());
                }
                sess.setValue(code);
                // move cursor to start to prevent scrolling to the bottom
                ace.renderer.scrollToX(0);
                ace.renderer.scrollToY(0);
                ace.selection.moveCursorFileStart();
                ace.focus();
            } else {
                this.__pendigCode = {code: code, lclass: lclass};
            }
            this.__broadcaster.setEnabled(false);
        },

        __cleanup: function () {
            if (this.__area) {
                this.__area.resetValue();
            } else if (this.__ace) {
                //todo review
                this.__setCode("");
            }
        },

        __onAceContainerAppear: function () {
            // timout needed for chrome to not get the ACE layout wrong and show the
            // text on top of the gutter
            qx.event.Timer.once(function () {
                var me = this;
                var ace = this.__ace = window.ace.edit(this.__aceContainer.getContentElement().getDomElement());
                ace.$blockScrolling = Infinity;
                var session = ace.getSession();
                session.setUseSoftTabs(true);
                session.setTabSize(2);
                session.setUseWorker(false);
                ace.renderer.setShowGutter(false);
                if (this.__pendigCode != null) {
                    this.__setCode(this.__pendigCode["code"], this.__pendigCode["lclass"]);
                    this.__pendigCode = null;
                }
                if (this.__pendigRo != null) {
                    ace.setReadOnly(this.__pendigRo);
                    this.__pendigRo = null;
                }
                session.on("change", function () {
                    me.__broadcaster.setEnabled(true);
                });
                this.__aceContainer.addListener("resize", function () {
                    // use a timeout to let the layout queue apply its changes to the dom
                    window.setTimeout(function () {
                        me.__ace.resize();
                    }, 0);
                });
            }, this, 500);
        }
    },

    destruct: function () {
        this.__aceContainer = null;
        this.__ace = null;
        this.__area = null;
        this.__pendigCode = null;
        this.__pendigRo = null;
        this.__broadcaster.destruct();
        //this._disposeObjects("__field_name");
    }
});