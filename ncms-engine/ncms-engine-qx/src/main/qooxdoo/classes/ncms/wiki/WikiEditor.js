/**
 * Wiki editor
 *
 * @asset(ncms/icon/16/wiki/*)
 */
qx.Class.define("ncms.wiki.WikiEditor", {
    extend: qx.ui.core.Widget,
    implement: [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include: [
        qx.ui.form.MForm,
        qx.ui.core.MChildrenHandling,
        sm.event.MForwardEvent
    ],


    statics: {

        SELECTION_START: {},

        SELECTION_END: {}
    },

    events: {

        /** Fired when the value was modified */
        "changeValue": "qx.event.type.Data"
    },

    properties: {

        "markup": {
            check: ["mediawiki", "markdown"],
            init: "mediawiki",
            apply: "_applyMarkup"
        },

        "helpSite": {
            check: "String",
            nullable: true,
            apply: "_applyHelpSite"
        }
    },

    construct: function (attrSpec, asmSpec) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(0));

        this.__controls = [];
        this.__asmSpec = asmSpec;
        this.__attrSpec = attrSpec;

        var toolbar = this.getChildControl("toolbar");
        this.__initToolbar(toolbar);

        var ta = this.getChildControl("textarea");

        //todo scary textselection hacks
        if (qx.core.Environment.get("engine.name") == "mshtml") {
            var getCaret = function (el) {
                if (el == null) {
                    return 0;
                }
                var start = 0;
                var range = el.createTextRange();
                var range2 = document.selection.createRange().duplicate();
                // get the opaque string
                var range2Bookmark = range2.getBookmark();
                range.moveToBookmark(range2Bookmark);
                while (range.moveStart("character", -1) !== 0) {
                    start++
                }
                return start;
            };
            var syncSel = function () {
                var tael = ta.getContentElement().getDomElement();
                this.__lastSStart = this.__lastSEnd = getCaret(tael);
            };
            ta.addListener("keyup", syncSel, this);
            ta.addListener("focus", syncSel, this);
            ta.addListener("click", syncSel, this);
        }
    },

    members: {

        __lastToolbarItem: null,

        __controls: null,

        __lastSStart: 0,

        __lastSEnd: 0,

        __helpControls: null,

        __mainPart: null,

        __attrSpec: null,

        __asmSpec: null,


        getMinimalLineHeight: function () {
            return this._getTextArea().getMinimalLineHeight();
        },

        setMinimalLineHeight: function (val) {
            this._getTextArea().setMinimalLineHeight(val);
        },


        getAutoSize: function () {
            return this._getTextArea().getAutoSize();
        },

        setAutoSize: function (val) {
            this._getTextArea().setAutoSize(val);
        },

        // overridden
        setValue: function (value) {
            this.getTextArea().setValue(value);
        },

        // overridden
        resetValue: function () {
            this.getTextArea().resetValue();
        },

        // overridden
        getValue: function () {
            return this.getTextArea().getValue();
        },

        getTextArea: function () {
            return this.getChildControl("textarea");
        },

        //overriden
        _applyEnabled: function (value, old) {
            this.base(arguments, value, old);
            this.getTextArea().setEnabled(value);
        },

        _applyMarkup: function (value, old) {
            this.__updateControls();
        },

        //overriden
        _createChildControlImpl: function (id) {
            var control;
            switch (id) {

                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar().set({overflowHandling: true, "show": "icon"});
                    this.__mainPart = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
                    control.add(this.__mainPart);
                    this._add(control, {flex: 0});
                    this.__lastToolbarItem = control.addSpacer();
                    var overflow = new qx.ui.toolbar.MenuButton(this.tr("More..."));
                    overflow.setShow("both");
                    overflow.setAppearance("wiki-editor-toolbar-menubutton");
                    overflow.setMenu(new qx.ui.menu.Menu());
                    overflow.setShowArrow(true);
                    control.add(overflow);
                    control.setOverflowIndicator(overflow);
                    control.setOverflowHandling(true);
                    break;

                case "textarea":
                    control = new qx.ui.form.TextArea();
                    control.setNativeContextMenu(true);
                    control.getContentElement().setAttribute("spellcheck", true);
                    control.setLiveUpdate(true);
                    control.addListener("changeValue", this.forwardEvent, this);
                    this._add(control, {flex: 1});
                    break;
            }

            return control || this.base(arguments, id);
        },

        /**
         * Add new toolbar control.
         * @param options {Object} control configuration:
         * {
         *  "id" : String, optional. Uses for showing/excluding controls
         *  "title" : String. optional. Title for control in toobar overflow menu
         *  "icon" : String. required. Icon for control in toobar and toolbar overflow menu
         *  "tooltipText" : String. optional. Text for control tooltip
         *  "prompt" : Function. optional. Callback for prompt additional params by user
         *          function(cb, editor, stext) {}
         *                  - cb : function(promptData) - callback for chain
         *                  - editor - current WikiEditor instance
         *                  - stext - selected text
         *  "insert<type>": Function. optional-required. Callback for processing prompt data and modifying editor text.
         *         function(cb, promptData) {}
         *                  - cb : function(text) - callback for chain. text will be inserted instead selected text
         *                  - promptData - data from prompt function execution (if specifyed) or selected text in other case
         *         <type> - captalized wiki editor type. If function for current editor type not specified, control for this type will not be shown.
         * }
         */
        addToolbarControl: function (options) {
            this._addToolbarControl(options);
        },

        /**
         * Set "excluded" state for all toolbar controls with given id.
         */
        excludeToolbarControl: function (id) {
            this.__updateControls(this.__controls.filter(function (bt) {
                var opts = bt.getUserData("opts");
                if (opts && opts["id"] === id) {
                    opts["excluded"] = true;
                    return true;
                } else {
                    return false;
                }
            }));
        },

        /**
         * Reset "excluded" state for all toolbar controls with given id.
         */
        showToolbarControl: function (id) {
            this.__updateControls(this.__controls.filter(function (bt) {
                var opts = bt.getUserData("opts");
                if (opts && opts["id"] === id) {
                    opts["excluded"] = false;
                    return true;
                } else {
                    return false;
                }
            }));
        },

        /**
         * Reset "excluded" state for all toolbar controls
         */
        resetToolbarControls: function () {
            this.__updateControls(this.__controls.filter(function (bt) {
                var opts = bt.getUserData("opts");
                if (opts) {
                    opts["excluded"] = false;
                    return true;
                } else {
                    return false;
                }
            }));
        },

        /**
         * Check for existing toolbar control with given id.
         */
        hasToolbarControl: function (id) {
            for (var i = 0; i < this.__controls.length; ++i) {
                var opts = this.__controls[i].getUserData();
                if (opts && opts["id"] === id) {
                    return true;
                }
            }
            return false;
        },

        setPlaceholder: function (value) {
            this.getTextArea().setPlaceholder(value);
        },

        _addToolbarControl: function (options) {
            var toolbar = this.getChildControl("toolbar");
            var editor = this.getTextArea();
            var callback = null;
            var bt = null;
            var bts = [];
            if (Array.isArray(options["menu"])) {
                bt = new qx.ui.toolbar.MenuButton(options["title"], options["icon"])
                .set({appearance: "wiki-editor-toolbar-menubutton", showArrow: true});
                bt.setMenu(new qx.ui.menu.Menu());
                options["menu"].forEach(function (mopts) {
                    callback = this.__buildToolbarControlAction(mopts);
                    var mbt = this.__registerToolbarControl(
                        bt.getMenu(), null,
                        new qx.ui.menu.Button(mopts["title"], mopts["icon"]),
                        callback, mopts);
                    mbt.setUserData("opts", mopts);
                    bts.push(mbt);

                    if (toolbar.getOverflowIndicator()) {
                        mbt = this.__registerToolbarControl(
                            toolbar.getOverflowIndicator().getMenu(), null,
                            new qx.ui.menu.Button(mopts["title"], mopts["icon"]),
                            callback, mopts);
                        mbt.setUserData("opts", mopts);
                        bts.push(mbt);
                    }
                }, this);
                if (this.__mainPart != null) {
                    this.__mainPart.add(bt);
                } else {
                    toolbar.add(bt);
                }
            } else {
                callback = this.__buildToolbarControlAction(options);
                bt = this.__registerToolbarControl(
                    toolbar, this.__mainPart,
                    new qx.ui.toolbar.Button(null, options["icon"])
                    .set({appearance: "wiki-editor-toolbar-button"}),
                    callback, options);
                bt.setUserData("opts", options);
                bts.push(bt);
                if (toolbar.getOverflowIndicator()) {
                    bt = this.__registerToolbarControl(
                        toolbar.getOverflowIndicator().getMenu(), null,
                        new qx.ui.menu.Button(options["title"], options["icon"]),
                        callback, options);
                    bt.setUserData("opts", options);
                    bts.push(bt);
                }
                if (options["shortcut"]) {
                    var shortcut = new sm.bom.ExtendedShortcut(options["shortcut"], false, editor);
                    shortcut.addListener("execute", callback, this);
                }
            }
            this.__controls.concat(bts);
            this.__updateControls(bts);
        },

        __updateControls: function (bts) {
            bts = bts || this.__controls;
            bts.forEach(function (bt) {
                var opts = bt.getUserData("opts");
                if (opts) {
                    var mfun = ("insert" + qx.lang.String.capitalize(this.getMarkup()));
                    if (opts[mfun] && !opts["excluded"]) {
                        bt.show();
                    } else {
                        bt.exclude();
                    }
                }
            }, this);
        },

        __updateHelpControls: function () {
            var ha = !sm.lang.String.isEmpty(this.getHelpSite());
            for (var i = 0; i < this.__helpControls.length; ++i) {
                if (ha) {
                    this.__helpControls[i].show();
                } else {
                    this.__helpControls[i].exclude();
                }
            }
        },

        __buildToolbarControlAction: function (options) {
            var me = this;
            return (function () {
                var icb = options[("insert" + qx.lang.String.capitalize(me.getMarkup()))];
                if (typeof icb !== "function") {
                    return;
                }
                var stext = me.getTextArea().getContentElement().getTextSelection();
                var fprompt = options["prompt"];
                if (typeof fprompt === "function") {
                    fprompt(stext, function (data) {
                        me._insertText(icb(data));
                    });
                } else {
                    me._insertText(icb(stext));
                }
            });
        },

        __registerToolbarControl: function (toolbar, part, bt, callback, options) {
            if (options["tooltipText"]) {
                bt.setToolTipText(options["tooltipText"]);
            }
            bt.addListener("execute", callback, this);
            if (part) {
                part.add(bt);
            } else {
                toolbar.add(bt);
            }
            return bt;
        },

        __initHelpControls: function (toolbar) {
            this.__helpControls = [];
            var helpCallback = function () {
                if (sm.lang.String.isEmpty(this.getHelpSite())) {
                    return;
                }
                qx.bom.Window.open(this.getHelpSite(), "NCMS:WikiHelp");
            };
            var hbm = this.__helpControls[this.__helpControls.length] =
                new qx.ui.toolbar.Button(this.tr("Help"),
                    "ncms/icon/16/help/help.png")
                .set({appearance: "wiki-editor-toolbar-button"});
            hbm.addListener("execute", helpCallback, this);
            hbm.setToolTip(new qx.ui.tooltip.ToolTip(this.tr("Help")));
            toolbar.addAfter(hbm, this.__lastToolbarItem);
            if (toolbar.getOverflowIndicator() && toolbar.getOverflowIndicator().getMenu()) {
                var hbo = this.__helpControls[this.__helpControls.length] = new qx.ui.menu.Button(this.tr(
                    "Help"), "ncms/icon/16/help/help.png");
                hbo.addListener("execute", helpCallback, this);
                hbo.setToolTipText(this.tr("Help"));
                toolbar.getOverflowIndicator().getMenu().addAt(hbo, 0);
            }
            this.__updateHelpControls();
        },

        wrap: function (func, ctx) {
            if (arguments.length == 1) {
                ctx = this;
            }
            var args = Array.prototype.slice.call(arguments, arguments.length > 1 ? 2 : 1);
            return function (data) {
                return func.apply(ctx, [].concat(data, args));
            }
        },

        textSurround: function (level, pattern, trails) {
            return this.wrap(this.__textSurround, this, level, pattern, trails);
        },

        __initToolbar: function (toolbar) {

            var cprompt = function (title) {
                return function (stext, cb) {
                    if (stext == null || stext.length == 0) {
                        stext = prompt(title);
                    }
                    if (stext != null) {
                        cb(stext);
                    }
                }
            };
            var surround = this.textSurround.bind(this);
            var wrap = this.wrap.bind(this);

            this.__initHelpControls(toolbar);

            this._addToolbarControl({
                id: "H1",
                icon: "ncms/icon/16/wiki/text_heading_1.png",
                tooltipText: this.tr("Heading 1"),
                prompt: cprompt(this.tr("Header text")),
                insertMediawiki: surround(1, "=", " "),
                insertMarkdown: surround(1, "#", " ")
            });
            this._addToolbarControl({
                id: "H2",
                icon: "ncms/icon/16/wiki/text_heading_2.png",
                tooltipText: this.tr("Heading 2"),
                prompt: cprompt(this.tr("Header text")),
                insertMediawiki: surround(2, "=", " "),
                insertMarkdown: surround(2, "#", " ")
            });
            this._addToolbarControl({
                id: "H3",
                icon: "ncms/icon/16/wiki/text_heading_3.png",
                tooltipText: this.tr("Heading 3"),
                prompt: cprompt(this.tr("Header text")),
                insertMediawiki: surround(3, "=", " "),
                insertMarkdown: surround(3, "#", " ")
            });
            this._addToolbarControl({
                id: "Bold",
                icon: "ncms/icon/16/wiki/text_bold.png",
                tooltipText: this.tr("Bold"),
                prompt: cprompt(this.tr("Bold text")),
                shortcut: "Ctrl+B",
                insertMediawiki: surround(1, "'''", ""),
                insertMarkdown: surround(2, "*", "")
            });
            this._addToolbarControl({
                id: "Italic",
                icon: "ncms/icon/16/wiki/text_italic.png",
                tooltipText: this.tr("Italic"),
                prompt: cprompt(this.tr("Italics text")),
                shortcut: "Ctrl+I",
                insertMediawiki: surround(2, "'", ""),
                insertMarkdown: surround(1, "*", "")
            });

            this._addToolbarControl({
                id: "UL",
                icon: "ncms/icon/16/wiki/text_list_bullets.png",
                tooltipText: this.tr("Bullet list"),
                shortcut: "Ctrl+U",
                insertMediawiki: wrap(this.__mediaWikiUL),
                insertMarkdown: wrap(this.__markdownUL)
            });
            this._addToolbarControl({
                id: "OL",
                icon: "ncms/icon/16/wiki/text_list_numbers.png",
                tooltipText: this.tr("Numbered list"),
                shortcut: "Ctrl+O",
                insertMediawiki: wrap(this.__mediaWikiOL, this),
                insertMarkdown: wrap(this.__markdownOL, this)
            });


            this._addToolbarControl({
                icon: "ncms/icon/16/wiki/link_add.png",
                title: this.tr("Insert link"),
                tooltipText: this.tr("Insert link"),
                prompt: this.__insertLinkPrompt.bind(this),
                insertMediawiki: this.__mediaWikiLink.bind(this)
            });

            this._addToolbarControl({
                icon: "ncms/icon/16/wiki/image_add.png",
                title: this.tr("Insert image"),
                prompt: this.__insertImagePrompt.bind(this),
                tooltipText: this.tr("Insert image"),
                insertMediawiki: wrap(this.__mediaWikiImage, this)
            });

            this._addToolbarControl({
                icon: "ncms/icon/16/wiki/document_add.png",
                title: this.tr("Insert file link"),
                prompt: this.__insertFilePrompt.bind(this),
                tooltipText: this.tr("Insert file link"),
                insertMediawiki: wrap(this.__mediaWikiFile, this)
            });

            this._addToolbarControl({
                id: "Table",
                icon: "ncms/icon/16/wiki/table_add.png",
                title: this.tr("Table"),
                tooltipText: this.tr("Insert table"),
                prompt: function (stext, cb) {
                    var dlg = new ncms.wiki.TableDlg();
                    dlg.addListener("completed", function (ev) {
                        dlg.close();
                        cb(ev.getData());
                    });
                    dlg.open();
                },
                insertMediawiki: wrap(this.__mediaWikiTable)
            });
            this._addToolbarControl({
                id: "Tree",
                icon: "ncms/icon/16/wiki/tree_add.png",
                title: this.tr("Tree"),
                tooltipText: this.tr("Insert tree"),
                prompt: function (stext, cb) {
                    var dlg = new ncms.wiki.TreeDlg();
                    dlg.addListener("completed", function (ev) {
                        dlg.close();
                        cb(ev.getData());
                    });
                    dlg.open();
                },
                insertMediawiki: wrap(this.__mediaWikiTree)
            });
            this._addToolbarControl({
                id: "Note",
                icon: "ncms/icon/16/wiki/note_add.png",
                tooltipText: this.tr("Create note"),
                menu: [
                    {
                        id: "NoteRegular",
                        icon: "ncms/icon/16/wiki/note.png",
                        title: this.tr("Simple note"),
                        tooltipText: this.tr("Simple note"),
                        insertMediawiki: wrap(this.__mediaWikiNote)
                    },
                    {
                        id: "NoteExclamation",
                        icon: "ncms/icon/16/wiki/note_exclamation.png",
                        title: this.tr("Warning note"),
                        tooltipText: this.tr("Warning note"),
                        insertMediawiki: wrap(this.__mediaWikiNote, this, "warning")
                    }
                ]
            });

            this._addToolbarControl({
                id: "Google",
                icon: "ncms/icon/16/wiki/google.png",
                tooltipText: this.tr("Google services"),
                menu: [
                    {
                        id: "Youtube",
                        icon: "ncms/icon/16/wiki/youtube.png",
                        title: this.tr("YouTube film"),
                        tooltipText: this.tr("Insert YouTube film"),
                        prompt: this.__insertYoutubePrompt.bind(this),
                        insertMediawiki: wrap(this.__mediaWikiYoutube, this)
                    },

                    {
                        id: "Gmap",
                        icon: "ncms/icon/16/wiki/gmap.png",
                        title: this.tr("Gmap location"),
                        tooltipText: this.tr("Insert gmap location"),
                        prompt: this.__insertGmapPrompt.bind(this),
                        insertMediawiki: wrap(this.__mediaWikiGmap, this)
                    }
                ]
            });

            this._addToolbarControl({
                id: "SlideShare",
                icon: "ncms/icon/16/wiki/slideshare.png",
                title: this.tr("SlideShare"),
                tooltipText: this.tr("Insert SlideShare presentation"),
                prompt: this.__insertSlideSharePresentationPrompt.bind(this),
                insertMediawiki: wrap(this.__mediaWikiSlideSharePresentation, this)
            });

            this.addToolbarControl({
                id: "Vimeo",
                icon: "ncms/icon/16/wiki/vimeo.png",
                title: this.tr("Vimeo video"),
                tooltipText: this.tr("Insert Vimeo video"),
                prompt: this.__insertVimeoPrompt.bind(this),
                insertMediawiki: wrap(this.__mediaWikiVimeo, this)
            });
        },

        _getSelectionStart: function () {
            var sStart = this.getTextArea().getTextSelectionStart();
            return (sStart == null || sStart == -1 || sStart == 0) ? this.__lastSStart : sStart;
        },

        _getSelectionEnd: function () {
            var sEnd = this.getTextArea().getTextSelectionEnd();
            return (sEnd == null || sEnd == -1 || sEnd == 0) ? this.__lastSEnd : sEnd;
        },

        _insertText: function (text) {
            if (text == null) {
                return;
            }
            var ta = this.getTextArea();
            var tel = ta.getContentElement();
            var scrollY = tel.getScrollY();

            var sStart = this._getSelectionStart();
            var sEnd = this._getSelectionEnd();
            var startPos = sStart;
            var endPos = sEnd;

            var nval = [];
            var value = ta.getValue();
            if (value == null) value = "";

            nval.push(value.substring(0, sStart));

            if (Array.isArray(text)) {
                var cpos = 0;
                text.forEach(function (chunk) {
                    if (chunk === ncms.wiki.WikiEditor.SELECTION_START) {
                        sStart = startPos + cpos;
                    } else if (chunk === ncms.wiki.WikiEditor.SELECTION_END) {
                        sEnd = startPos + cpos;
                        if (sEnd > sStart && sm.lang.String.lastChar(nval[nval.length - 1]) === "\n") {
                            sEnd -= 1;
                        }
                    } else {
                        nval.push(chunk);
                        cpos += nval[nval.length - 1].length;
                    }
                });
                if (sStart === startPos && sEnd === endPos) {
                    sStart = sStart + cpos;
                    sEnd = sStart;
                }
            } else {
                nval.push(text);
                sStart = sStart + text.length;
                sEnd = sStart;
            }
            nval.push(value.substring(endPos));
            this.setValue(nval.join(""));
            ta.setTextSelection(sStart, sEnd);
            tel.scrollToY(scrollY);
        },

        _applyHelpSite: function (value, old) {
            this.__updateHelpControls();
        },

        //////////////////////////////////////////////////////////////////////////
        /////////////////////////   Helpers    ///////////////////////////////////
        //////////////////////////////////////////////////////////////////////////

        __textSurround: function (text, level, pattern, trails) {
            var val = [];
            var hfix = qx.lang.String.repeat(pattern, level < 1 ? 1 : level);
            val.push(hfix);
            val.push(trails || "");
            val.push(ncms.wiki.WikiEditor.SELECTION_START);
            val.push(text);
            val.push(ncms.wiki.WikiEditor.SELECTION_END);
            val.push(trails || "");
            val.push(hfix);
            return val;
        },

        __insertLinkPrompt: function (stext, cb) {
            var dlg = new ncms.pgs.LinkSelectorDlg(
                this.tr("Insert link"),
                {
                    includeLinkName: true,
                    linkText: stext,
                    overrideLinktext: sm.lang.String.isEmpty(stext),
                    allowExternalLinks: true,
                    requireLinkName: false
                }
            );
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __insertImagePrompt: function (stext, cb) {
            var dlg = new ncms.wiki.InsertImageDlg(
                this.__asmSpec["id"],
                this.tr("Insert image"));
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __insertFilePrompt: function (stext, cb) {
            var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                this.__asmSpec["id"],
                this.tr("Insert link to file"),
                {
                    allowModify: true,
                    allowMove: false,
                    allowSubfoldersView: true,
                    smode: qx.ui.table.selection.Model.SINGLE_SELECTION
                }
            );
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __mediaWikiLink: function (data) {
            var val = [];
            val.push("[[");
            if (!sm.lang.String.isEmpty(data["externalLink"])) {
                val.push(data["externalLink"]);
            } else {
                val.push("Page:");
                val.push(data["guidPath"][data["guidPath"].length - 1]);
            }
            if (!sm.lang.String.isEmpty(data["linkText"])) {
                val.push("|");
                val.push(data["linkText"]);
            }
            val.push("]]");
            return val.join("")
        },

        __mediaWikiImage: function (data) {
            var val = [];
            val.push("[[Image:");
            val.push("/" + data["id"] + "/");
            val.push(data["name"]);
            switch (data["size"]) {
                case "small":
                    val.push("|100px");
                    break;
                case "medium":
                    val.push("|200px");
                    break;
                case "large":
                    val.push("|300px");
                    break;
            }
            if (data["position"] != null) {
                val.push("|" + data["position"]);
            }
            if (data["caption"] != null) {
                val.push("|frame");
                val.push("|" + data["caption"]);
            }
            if (data["link"] != null) {
                val.push("|link=" + data["link"]);
            }
            val.push("]]");
            return val.join("");
        },

        __mediaWikiFile: function (data) {
            var val = [];
            val.push("[[Media:/");
            val.push(data["id"]);
            val.push("/");
            val.push(data["name"]);
            val.push("|");
            if (sm.lang.String.isEmpty(data["linkText"])) {
                val.push(data["name"]);
            } else {
                val.push(data["linkText"]);
            }
            val.push("]]");
            return val.join("");
        },

        __mediaWikiUL: function () {
            var val = [];
            val.push("* ");
            val.push(ncms.wiki.WikiEditor.SELECTION_START);
            val.push(this.tr("First") + "\n");
            val.push(ncms.wiki.WikiEditor.SELECTION_END);
            val.push("* " + this.tr("Second") + "\n");
            val.push("** " + this.tr("First for second") + "\n");
            val.push("* " + this.tr("Third"));
            return val;
        },

        __markdownUL: function () {
            var val = [];
            val.push("* ");
            val.push(ncms.wiki.WikiEditor.SELECTION_START);
            val.push(this.tr("First") + "\n");
            val.push(ncms.wiki.WikiEditor.SELECTION_END);
            val.push("* " + this.tr("Second") + "\n");
            val.push("    * " + this.tr("First for second") + "\n");
            val.push("* " + this.tr("Third"));
            return val
        },

        __mediaWikiOL: function () {
            var val = [];
            val.push("# ");
            val.push(ncms.wiki.WikiEditor.SELECTION_START);
            val.push(this.tr("First") + "\n");
            val.push(ncms.wiki.WikiEditor.SELECTION_END);
            val.push("# " + this.tr("Second") + "\n");
            val.push("## " + this.tr("First for second") + "\n");
            val.push("# " + this.tr("Third"));
            return val;
        },

        __markdownOL: function () {
            var val = [];
            val.push("1. ");
            val.push(ncms.wiki.WikiEditor.SELECTION_START);
            val.push(this.tr("First") + "\n");
            val.push(ncms.wiki.WikiEditor.SELECTION_END);
            val.push("1. " + this.tr("Second") + "\n");
            val.push("    1. " + this.tr("First for second") + "\n");
            val.push("1. " + this.tr("Third"));
            return val;
        },

        __mediaWikiTree: function (data) {
            var val = [];
            val.push("");
            if (data["style"] === "dynamic") {
                val.push("<tree style=\"dynamic\" open=\"" + data["open"] + "\">");
            } else {
                val.push("<tree>");
            }
            val.push("- " + this.tr("Root"));
            val.push("-- " + this.tr("Child 1"));
            val.push("--- " + this.tr("Child level 3"));
            val.push("-- " + this.tr("Child 2"));
            val.push("</tree>");
            val.push("");
            return val.join("\n");
        },

        __mediaWikiNote: function (data, opts) {
            var val = [];
            val.push("");
            if (opts === "warning") {
                val.push("<note style=\"warning\">");
            } else {
                val.push("<note>");
            }
            val.push(ncms.wiki.WikiEditor.SELECTION_START);
            val.push(this.tr("Note text"));
            val.push(ncms.wiki.WikiEditor.SELECTION_END);
            val.push("</note>");
            val.push("");
            return val;
        },

        __insertGmapPrompt: function (stext, cb) {
            var dlg = new ncms.wiki.InsertGMapDlg();
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __mediaWikiGmap: function (data, opts) {
            var val = [];
            val.push("<gmap>");
            val.push(data);
            val.push("</gmap>");
            return val.join("\n");
        },

        __insertYoutubePrompt: function (stext, cb) {
            var dlg = new ncms.wiki.InsertYoutubeDlg();
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __mediaWikiYoutube: function (data, opts) {
            var val = [];
            val.push("<youtube videoId=\"" + data["code"] + "\"");
            if (data["custom"]) {
                val.push(" width=\"" + data["width"] + "\"");
                val.push(" height=\"" + data["height"] + "\"");
            }
            val.push("/>");
            return val.join("");
        },

        __insertSlideSharePresentationPrompt: function (stext, cb) {
            var dlg = new ncms.wiki.InsertSlideSharePresentationDlg();
            dlg.addListener("completed", function (event) {
                var data = event.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __mediaWikiSlideSharePresentation: function (data) {
            var val = [];
            val.push("<slideshare code=\"" + data["code"] + "\"");
            if (data["custom"]) {
                val.push(" width=\"" + data["width"] + "\"");
                val.push(" height=\"" + data["height"] + "\"");
            }
            val.push("/>");
            return val.join("");
        },

        __insertVimeoPrompt: function (stext, cb) {
            var dlg = new ncms.wiki.InsertVimeoDlg();
            dlg.addListener("completed", function (event) {
                var data = event.getData();
                dlg.close();
                cb(data);
            });
            dlg.open();
        },

        __mediaWikiVimeo: function (data) {
            var val = [];
            val.push("<vimeo code=\"" + data["code"] + "\"");
            if (data["custom"]) {
                val.push(" width=\"" + data["width"] + "\"");
                val.push(" height=\"" + data["height"] + "\"");
            }
            val.push("/>");
            return val.join("");
        },

        __mediaWikiTable: function (tm, isWide) {
            /*
             {| class="table01"
             |-
             ! Header 1
             ! Header 2
             ! Header 3
             |-
             | row 1, cell 1
             | row 1, cell 2
             | row 1, cell 3
             |-
             | row 2, cell 1
             | row 2, cell 2
             | row 2, cell 3
             |}
             */
            var tspec = [];
            tspec.push("");
            tspec.push("{| class=" + (isWide == true ? "'wide'" : "'short'"));
            var cc = tm.getColumnCount();
            var rc = tm.getRowCount();
            for (var i = 0; i < rc; ++i) {
                tspec.push("|-");
                var rdata = tm.getRowData(i);
                for (var j = 0; j < cc; ++j) {
                    var cval = (rdata != null && rdata[j] != null) ? rdata[j] : "";
                    tspec.push((i == 0 ? "! " : "| ") + cval);
                }
            }
            tspec.push("|}");
            tspec.push("");
            return tspec.join("\n");
        }
    },

    destruct: function () {
        this.__controls = null;
        this.__helpControls = null;
        this.__asmSpec = null;
        this.__attrSpec = null;
    }
});