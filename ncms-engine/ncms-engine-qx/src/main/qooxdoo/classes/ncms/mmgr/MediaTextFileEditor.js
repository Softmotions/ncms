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
         * {
         *   "id":2,
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
        },

        // overridden
        focusable: {
            refine: true,
            init: true
        }
    },

    /**
     * @param opts {Object?} Optional file specification.
     */
    construct: function (opts) {
        this.__opts = opts || {};
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(5));
        this.__broadcaster = sm.event.Broadcaster.create({"enabled": false});
        this.__alertBlocker = new ncms.cc.AlertBlocker(this);

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Control+S"),
            this.__save, this);
        this._registerCommandFocusWidget(this);

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
        var bt = this.__saveBt = new qx.ui.form.Button(this.tr("Save (Ctrl+S)"), "ncms/icon/16/misc/disk.png");
        this.__broadcaster.attach(bt, "enabled");
        bt.addListener("execute", this.__save, this);
        hcont.add(bt, {flex: 1});
        this._add(hcont);

        // Update itself
        var events = ncms.Events.getInstance();
        events.addListener("mediaUpdated", this.__onMediaUpdated, this);
    },

    members: {

        /**
         * Options
         */
        __opts: null,

        __saveBt: null,

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

        __alertBlocker: null,

        _forwardStates: {
            focused: true
        },

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

        __onMediaUpdated: function (ev) {
            var spec = this.getFileSpec(),
                evspec = ev.getData(),
                part = evspec.hints["ui"];
            if (spec == null) {
                return;
            }
            if (evspec.hints["app"] !== ncms.Application.UUID) {
                part = null; // force refresh (no locking implemented in this version)
            }
            if (part != this.__opts.ui && spec.id == evspec.id) { // edit the same file
                this.__opts.skipFocus = true;
                this.__applyFileSpec(spec); // refresh
            }
        },

        isSaved: function () {
            return this.__saveBt.getEnabled() == false; // Save bt not enabled
        },

        __save: function () {
            if (this.isSaved()) {
                return;
            }
            var spec = this.getFileSpec();
            if (spec == null || !ncms.Utils.isTextualContentType(spec["content_type"])) {
                return;
            }
            var text = this.__getCode();
            var path = (spec["folder"] + spec["name"]).split("/");
            var url = ncms.Application.ACT.getRestUrl("media.upload", path);
            if (this.__opts.ui != null) {
                url = url + "?__ui=" + encodeURIComponent(this.__opts.ui);
            }
            var ctype = spec["content_type"] || "text/plain";
            ctype = ctype.split(";")[0];
            var req = new sm.io.Request(url, "PUT", "application/json");
            req.setRequestContentType(ctype);
            req.setData(text);
            this.__broadcaster.setEnabled(false);
            req.send(function () {
                ncms.Application.infoPopup(this.tr("File successfully saved"));
            }, this);
        },

        __applyFileSpec: function (spec) {
            this.__alertBlocker.unblock();
            this.__cleanup();
            if (spec == null || !ncms.Utils.isTextualContentType(spec["content_type"])) {
                return;
            }
            var maxEditTextSize = ncms.Application.APP_STATE.getStateProperty("max-edit-text-size");
            if (spec["content_length"] > maxEditTextSize) {
                this.__alertBlocker.block(this.tr("File is too big for internal editor"));
                this.addListenerOnce("disappear", function () {
                    this.__alertBlocker.unblock();
                    this.addListenerOnce("appear", function () {
                        var spec = this.getFileSpec();
                        this.__alertBlocker.onAppear(spec["content_length"] > maxEditTextSize);
                    }, this);
                }, this);
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
                } else if (ctype.indexOf("application/json") !== -1) {
                    lclass = "json"
                } else if (qx.lang.String.endsWith(fname, ".scss")) {
                    lclass = "scss";
                } else if (qx.lang.String.endsWith(fname, ".sass")) {
                    lclass = "sass";
                } else if (ctype.indexOf("text/css") !== -1) {
                    lclass = "css";
                } else if (qx.lang.String.endsWith(fname, ".jsp")) {
                    lclass = "jsp";
                } else if (ctype.indexOf("python") !== -1) {
                    lclass = "python";
                } else {
                    lclass = "text";
                }
                this.__setCode(text, lclass);
                if (!this.__opts.noAutoFocus) {
                    this.__focus();
                }
            }, this);
        },

        __focus: function () {
            if (this.__opts.skipFocus !== true) {
                this.__ace && this.__ace.focus();
                this.__area && this.__area.focus();
            } else {
                this.__opts.skipFocus = false;
            }
        },

        __getCode: function () {
            if (this.__area) {
                return this.__area.getValue();
            } else if (this.__ace) {
                return this.__ace.getSession().getValue();
            }
        },

        setCode: function (code) {
            this.__setCode(code, null);
            this.__broadcaster.setEnabled(true);
        },

        __setCode: function (code, lclass) {
            if (this.__area) {
                this.__area.setValue(code);
            } else if (this.__ace) {
                var ace = this.__ace;
                var sess = ace.getSession();
                sess.$lclass = lclass;
                if (lclass != null) {
                    var Mode = require("ace/mode/" + lclass).Mode;
                    sess.setMode(new Mode());
                }
                sess.setValue(code);
                // move cursor to start to prevent scrolling to the bottom
                ace.renderer.scrollToX(0);
                ace.renderer.scrollToY(0);
                ace.selection.moveCursorFileStart();
            } else {
                this.__pendigCode = {code: code, lclass: lclass};
            }
            this.__broadcaster.setEnabled(false);
        },

        __cleanup: function () {
            this.__setCode("");
        },

        __onAceContainerAppear: function () {
            // timout needed for chrome to not get the ACE layout wrong and show the
            // text on top of the gutter
            qx.event.Timer.once(function () {
                var me = this;
                require("ace/config").set("workerPath", "resource/ncms/script");
                var lt = require("ace/ext/language_tools");
                var ace = this.__ace = window.ace.edit(this.__aceContainer.getContentElement().getDomElement());
                if (!ace.completers || ace.completers.filter(function (c) {
                        return !!c.$httl;
                    }).length == 0) {
                    lt.addCompleter(me.__httlAutocompleter());
                }

                ace.setOptions({
                    enableLiveAutocompletion: true
                });
                ace.$blockScrolling = Infinity;
                var session = ace.getSession();
                session.setUseSoftTabs(true);
                session.setTabSize(2);
                session.setUseWorker(false);
                ace.renderer.setShowGutter(false);
                if (this.__pendigCode != null) {
                    this.__setCode(this.__pendigCode["code"], this.__pendigCode["lclass"]);
                    if (!this.__opts.noAutoFocus) {
                        this.__focus();
                    }
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
        },

        //todo move it to safe place
        __httlAutocompleter: function () {
            var words = ncms.mmgr.MediaTextFileEditor.httlWords;
            return {
                $httl: true,
                identifierRegexps: [/[#$*@{A-Za-z0-9]+/],
                getCompletions: function (editor, sess, pos, prefix, callback) {
                    if (prefix.length === 0 || sess.$lclass !== 'html') {
                        callback(null, []);
                        return;
                    }
                    var sa = [];
                    for (var i = 0, l = words.length; i < l; ++i) {
                        var w = words[i];
                        if (prefix.indexOf(w.length > prefix.length ? w.substring(0, prefix.length) : w) === 0) {
                            sa.push({
                                caption: words[i],
                                value: words[i],
                                score: -words[i].length,
                                meta: "httl"
                            });
                        }
                    }
                    callback(null, sa);
                }
            }
        }
    },

    destruct: function () {
        var events = ncms.Events.getInstance();
        events.removeListener("mediaUpdated", this.__onMediaUpdated, this);
        this.__opts = null;
        this.__aceContainer = null;
        this.__ace = null;
        this.__area = null;
        this.__pendigCode = null;
        this.__pendigRo = null;
        this.__saveBt = null;
        this._disposeObjects("__alertBlocker", "__broadcaster");
    },

    defer: function (statics) {
        var words = [
            '#if()',
            '#for()',
            '#foreach()',
            '#end',
            '${@',
            '${',
            '#break',
            '#set',
            '#break()',
            '#else',
            '#elseif()',
            '#macro(',
            '${include()}',
            '${read()}',
            'asm()',
            'asmAnyTTN()',
            'asmHasAnyTTN()',
            'asmCachedTTN()',
            'asmHasAttribute()',
            'asmAny()',
            'page()',
            'asmNavChilds()',
            'asmNavChildsAll()',
            'asmNavChildsPageCriteria()',
            'link()',
            'resolve()',
            'linkHtml()',
            'asRichRef()',
            'asTree()',
            'asTable()',
            'asImage()',
            'ogmeta()',
            'in()',
            'inICase()',
            'notNull()',
            'blank()',
            'ifTrue()',
            'requestParamMatched()',
            'requestParameter()',
            'ifRequestParameter()',
            'cookie()',
            'ifCookie()',
            'requestLanguage()',
            'encodeUriComponent()',
            'randomSublist()',
            'split()',
            'includeTemplate()',
            'siteFile()',
            'format()',
            'format2()',
            'formatEng()',
            'translate()',
            'config()',
            'env()',
            'rootUrl()',
            'isPreview()',
            'isAndroidMobile()',
            'isAndroidTablet()',
            'isIpad()',
            'isIphone()',
            'isMobile()',
            'isTablet()',
            'sum()',
            'avg()',
            'max()',
            'min()',
            'clip()',
            'repeat()',
            'split()',
            'md5()',
            'sha()',
            'digest()',
            'toUnderlineName()',
            'toCamelName()',
            'toCapitalName()',
            'now()',
            'random()',
            'uuid()',
            'toLocale()',
            'toBoolean()',
            'toChar()',
            'toByte()',
            'toShort()',
            'toInt()',
            'toLong()',
            'toFloat()',
            'toDouble()',
            'toClass()',
            'escapeXml()',
            'escapeString()',
            'unescapeString()',
            'unescapeXml()',
            'escapeBase64()',
            'unescapeBase64()',
            'toCycle()',
            'sort()',
            'recursive()'
        ];
        [].concat(words).forEach(function (w) {
            if (!w.match(/^(#|\$\{).*/)) {
                words.push('${' + w + '}');
            }
        });
        words.sort(function (w1, w2) {
            return (w1.length > w2.length) ? 1 : (w2.length < w1.length ? -1 : 0);
        });
        statics.httlWords = words;
    }
});