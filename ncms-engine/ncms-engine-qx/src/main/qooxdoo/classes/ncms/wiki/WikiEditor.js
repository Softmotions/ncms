/**
 * Wiki editor
 *
 * @asset(ncms/icon/16/wiki/text_heading_1.png)
 * @asset(ncms/icon/16/wiki/text_heading_2.png)
 * @asset(ncms/icon/16/wiki/text_heading_3.png)
 * @asset(ncms/icon/16/wiki/text_bold.png)
 * @asset(ncms/icon/16/wiki/text_italic.png)
 * @asset(ncms/icon/16/wiki/text_list_bullets.png)
 * @asset(ncms/icon/16/wiki/text_list_numbers.png)
 * @asset(ncms/icon/16/wiki/link_add.png)
 * @asset(ncms/icon/16/wiki/image_add.png)
 * @asset(ncms/icon/16/wiki/table_add.png)
 * @asset(ncms/icon/16/wiki/tree_add.png)
 * @asset(ncms/icon/16/wiki/note_add.png)
 */
qx.Class.define("ncms.wiki.WikiEditor", {
    extend : qx.ui.core.Widget,
    implement : [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include : [
        qx.ui.form.MForm,
        qx.ui.core.MChildrenHandling,
        sm.event.MForwardEvent
    ],


    statics : {

        createTextSurround : function(text, level, pattern, trails) {
            var nval = [];
            var hfix = qx.lang.String.repeat(pattern, level < 1 ? 1 : level);
            nval.push(hfix);
            nval.push(trails || "");
            nval.push(text);
            nval.push(trails || "");
            nval.push(hfix);
            return nval.join("");
        }
    },

    events : {

        /** Fired when the value was modified */
        "changeValue" : "qx.event.type.Data"
    },

    properties : {

        "markup" : {
            check : ["mediawiki", "markdown"],
            init : "mediawiki",
            apply : "_applyMarkup"
        },

        "helpSite" : {
            check : "String",
            nullable : true,
            apply : "_applyHelpSite"
        }
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(0));
        this.__editorControls = []; // cache for controls

        var toolbar = this.getChildControl("toolbar");
        this.__initToolbar(toolbar);

        var ta = this.getChildControl("textarea");

        //todo scary textselection hacks
        if (qx.core.Environment.get("engine.name") == "mshtml") {
            var getCaret = function(el) {
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
            var syncSel = function() {
                var tael = ta.getContentElement().getDomElement();
                this.__lastSStart = this.__lastSEnd = getCaret(tael);
            };
            ta.addListener("keyup", syncSel, this);
            ta.addListener("focus", syncSel, this);
            ta.addListener("click", syncSel, this);
        }
    },

    members : {

        __lastToolbarItem : null,

        __editorControls : null,

        __lastSStart : 0,

        __lastSEnd : 0,

        __helpControls : null,

        __mainPart : null,


        getMinimalLineHeight : function() {
            return this._getTextArea().getMinimalLineHeight();
        },

        setMinimalLineHeight : function(val) {
            this._getTextArea().setMinimalLineHeight(val);
        },


        getAutoSize : function() {
            return this._getTextArea().getAutoSize();
        },

        setAutoSize : function(val) {
            this._getTextArea().setAutoSize(val);
        },

        // overridden
        setValue : function(value) {
            this.getTextArea().setValue(value);
        },

        // overridden
        resetValue : function() {
            this.getTextArea().resetValue();
        },

        // overridden
        getValue : function() {
            return this.getTextArea().getValue();
        },

        getTextArea : function() {
            return this.getChildControl("textarea");
        },

        //overriden
        _applyEnabled : function(value, old) {
            this.base(arguments, value, old);
            this.getTextArea().setEnabled(value);
        },

        _applyMarkup : function(value, old) {
            for (var i = 0; i < this.__editorControls.length; ++i) {
                this.__updateControl(this.__editorControls[i]);
            }
        },

        //overriden
        _createChildControlImpl : function(id) {
            var control;
            switch (id) {

                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar().set({overflowHandling : true, "show" : "icon"});
                    this.__mainPart = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
                    control.add(this.__mainPart);
                    this._add(control, {flex : 0});
                    this.__lastToolbarItem = control.addSpacer();
                    var overflow = new qx.ui.toolbar.MenuButton(this.tr("More..."));
                    overflow.setMenu(new qx.ui.menu.Menu());
                    control.add(overflow);
                    control.setOverflowIndicator(overflow);
                    break;

                case "textarea":
                    control = new qx.ui.form.TextArea();
                    control.setLiveUpdate(true);
                    control.addListener("changeValue", this.forwardEvent, this);
                    this._add(control, {flex : 1});
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
        addToolbarControl : function(options) {
            this._addToolbarControl(options);
        },

        /**
         * Set "excluded" state for all toolbar controls with given id.
         */
        excludeToolbarControl : function(id) {
            for (var i = 0; i < this.__editorControls.length; ++i) {
                var cmeta = this.__editorControls[i];
                if (cmeta.options["id"] == id) {
                    cmeta.options["excluded"] = true;
                    this.__updateControl(cmeta);
                }
            }
        },

        /**
         * Reset "excluded" state for all toolbar controls with given id.
         */
        showToolbarControl : function(id) {
            for (var i = 0; i < this.__editorControls.length; ++i) {
                var cmeta = this.__editorControls[i];
                if (cmeta.options["id"] == id) {
                    cmeta.options["excluded"] = false;
                    this.__updateControl(cmeta);
                }
            }
        },

        /**
         * Reset "excluded" state for all toolbar controls
         */
        resetToolbarControls : function() {
            for (var i = 0; i < this.__editorControls.length; ++i) {
                this.__editorControls[i].options["excluded"] = false;
                this.__updateControl(this.__editorControls[i]);
            }
        },

        /**
         * Check for existing toolbar control with given id.
         */
        hasToolbarControl : function(id) {
            for (var i = 0; i < this.__editorControls.length; ++i) {
                var cmeta = this.__editorControls[i];
                if (cmeta.options["id"] == id) {
                    return true;
                }
            }
            return false;
        },

        setPlaceholder : function(value) {
            this.getTextArea().setPlaceholder(value);
        },

        _addToolbarControl : function(options) {
            var toolbar = this.getChildControl("toolbar");
            var callback = this.__buildToolbarControlAction(options);
            var cmeta = this.__editorControls[this.__editorControls.length] = {
                options : options,
                buttons : []
            };
            cmeta.buttons[0] = this.__createToolbarControl(toolbar, this.__mainPart,
                    qx.ui.toolbar.Button, callback, options, "wiki-editor-toolbar-button");
            if (toolbar.getOverflowIndicator()) {
                cmeta.buttons[1] = this.__createToolbarControl(toolbar.getOverflowIndicator().getMenu(), null,
                        qx.ui.menu.Button, callback, options);
            }
            this.__updateControl(cmeta);
        },

        __updateControl : function(cmeta) {
            var applied = !!cmeta.options[("insert" + qx.lang.String.capitalize(this.getMarkup()))] && !cmeta.options["excluded"];
            for (var i = 0; i < cmeta.buttons.length; ++i) {
                if (applied) {
                    cmeta.buttons[i].show();
                } else {
                    cmeta.buttons[i].exclude();
                }
            }
        },

        __updateHelpControls : function() {
            var ha = !sm.lang.String.isEmpty(this.getHelpSite());
            for (var i = 0; i < this.__helpControls.length; ++i) {
                if (ha) {
                    this.__helpControls[i].show();
                } else {
                    this.__helpControls[i].exclude();
                }
            }
        },

        __buildToolbarControlAction : function(options) {
            var me = this;
            return function() {
                var icb = options[("insert" + qx.lang.String.capitalize(me.getMarkup()))];
                if (!icb) {
                    return;
                }
                var selectedText = this.getTextArea().getContentElement().getTextSelection();
                if (options["prompt"]) {
                    options["prompt"].call(me, function(text) {
                        icb.call(me, me._insertText, text);
                    }, this, selectedText);
                } else {
                    icb.call(me, me._insertText, selectedText);
                }
            };
        },

        __createToolbarControl : function(toolbar, part, btclass, callback, options, appearance) {
            var bt = new btclass(options["title"], options["icon"]);
            if (appearance) {
                bt.setAppearance(appearance);
            }
            if (options["tooltipText"]) {
                bt.setToolTip(new qx.ui.tooltip.ToolTip(options["tooltipText"]));
            }
            bt.addListener("execute", callback, this);
            if (part) {
                part.add(bt);
            } else {
                toolbar.add(bt);
            }
            return bt;
        },

        __initHelpControls : function(toolbar) {
            this.__helpControls = [];
            var helpCallback = function() {
                if (sm.lang.String.isEmpty(this.getHelpSite())) {
                    return;
                }
                qx.bom.Window.open(this.getHelpSite(), "NCMS:WikiHelp");
            };
            var hbm = this.__helpControls[this.__helpControls.length] =
                    new qx.ui.toolbar.Button(this.tr("Help"),
                            "ncms/icon/16/help/help.png")
                            .set({appearance : "wiki-editor-toolbar-button"});
            hbm.addListener("execute", helpCallback, this);
            hbm.setToolTip(new qx.ui.tooltip.ToolTip(this.tr("Help")));
            toolbar.addAfter(hbm, this.__lastToolbarItem);
            if (toolbar.getOverflowIndicator() && toolbar.getOverflowIndicator().getMenu()) {
                var hbo = this.__helpControls[this.__helpControls.length] = new qx.ui.menu.Button(this.tr("Help"), "ncms/icon/16/help/help.png");
                hbo.addListener("execute", helpCallback, this);
                hbo.setToolTip(new qx.ui.tooltip.ToolTip(this.tr("Help")));
                toolbar.getOverflowIndicator().getMenu().addAt(hbo, 0);
            }

            this.__updateHelpControls();
        },

        __initToolbar : function(toolbar) {
            var self = this.self(arguments);
            var cprompt = function(title) {
                return function(cb, editor, sText) {
                    if (!sText) {
                        sText = prompt(title);
                    }
                    if (sText != null && sText != undefined) {
                        cb.call(this, sText);
                    }
                }
            };
            var csurround = function(level, pattern, trails) {
                return function(cb, data) {
                    cb.call(this, self.createTextSurround(data, level, pattern, trails));
                }
            };
            var cscall = function(func) {
                return function(cb, data) {
                    cb.call(this, func.call(this, data));
                }
            };

            this.__initHelpControls(toolbar);

            this._addToolbarControl({
                id : "H1",
                icon : "ncms/icon/16/wiki/text_heading_1.png",
                tooltipText : this.tr("Heading 1"),
                prompt : cprompt(this.tr("Header text")),
                insertMediawiki : csurround(1, "=", " "),
                insertMarkdown : csurround(1, "#", " ")
            });
            this._addToolbarControl({
                id : "H2",
                icon : "ncms/icon/16/wiki/text_heading_2.png",
                tooltipText : this.tr("Heading 2"),
                prompt : cprompt(this.tr("Header text")),
                insertMediawiki : csurround(2, "=", " "),
                insertMarkdown : csurround(2, "#", " ")
            });
            this._addToolbarControl({
                id : "H3",
                icon : "ncms/icon/16/wiki/text_heading_3.png",
                tooltipText : this.tr("Heading 3"),
                prompt : cprompt(this.tr("Header text")),
                insertMediawiki : csurround(3, "=", " "),
                insertMarkdown : csurround(3, "#", " ")
            });
            this._addToolbarControl({
                id : "Bold",
                icon : "ncms/icon/16/wiki/text_bold.png",
                tooltipText : this.tr("Bold"),
                prompt : cprompt(this.tr("Bold text")),
                insertMediawiki : csurround(1, "'", ""),
                insertMarkdown : csurround(2, "*", "")
            });
            this._addToolbarControl({
                id : "Italic",
                icon : "ncms/icon/16/wiki/text_italic.png",
                tooltipText : this.tr("Italic"),
                prompt : cprompt(this.tr("Italics text")),
                insertMediawiki : csurround(2, "'", ""),
                insertMarkdown : csurround(1, "*", "")
            });

            this._addToolbarControl({
                id : "UL",
                icon : "ncms/icon/16/wiki/text_list_bullets.png",
                tooltipText : this.tr("Bullet list"),
                insertMediawiki : cscall(this.__mediaWikiUL),
                insertMarkdown : cscall(this.__markdownUL)
            });
            this._addToolbarControl({
                id : "OL",
                icon : "ncms/icon/16/wiki/text_list_numbers.png",
                tooltipText : this.tr("Numbered list"),
                insertMediawiki : cscall(this.__mediaWikiOL),
                insertMarkdown : cscall(this.__markdownOL)
            });
            // TODO: init buttons: link, image
            this._addToolbarControl({
                icon : "ncms/icon/16/wiki/link_add.png",
                tooltipText : this.tr("Link to another page")
            });
            this._addToolbarControl({
                icon : "ncms/icon/16/wiki/image_add.png",
                tooltipText : this.tr("Add image|link to file")
            });
            this._addToolbarControl({
                id : "Table",
                icon : "ncms/icon/16/wiki/table_add.png",
                tooltipText : this.tr("Add table"),
                prompt : function(cb, editor, stext) {
                    var dlg = new ncms.wiki.TableDlg();
                    dlg.addListener("insertTable", function(ev) {
                        dlg.close();
                        cb.call(this, ev.getData());
                    }, this);
                    dlg.open();
                },
                insertMediawiki : cscall(this.__mediaWikiTable)
            });
            this._addToolbarControl({
                id : "Tree",
                icon : "ncms/icon/16/wiki/tree_add.png",
                tooltipText : this.tr("Add tree"),
                insertMediawiki : cscall(this.__mediaWikiTree)
            });
            this._addToolbarControl({
                id : "Note",
                icon : "ncms/icon/16/wiki/note_add.png",
                tooltipText : this.tr("Create note"),
                insertMediawiki : cscall(this.__mediaWikiNote),
                insertMarkdown : cscall(this.__markdownNote)
            });
        },

        _getSelectionStart : function() {
            var sStart = this.getTextArea().getTextSelectionStart();
            return (sStart == null || sStart == -1 || sStart == 0) ? this.__lastSStart : sStart;
        },

        _getSelectionEnd : function() {
            var sEnd = this.getTextArea().getTextSelectionEnd();
            return (sEnd == null || sEnd == -1 || sEnd == 0) ? this.__lastSEnd : sEnd;
        },

        _insertText : function(text) {
            var ta = this.getTextArea();
            var tel = ta.getContentElement();
            var scrollY = tel.getScrollY();

            var sStart = this._getSelectionStart();
            var sEnd = this._getSelectionEnd();

            var nval = [];
            var value = ta.getValue();
            if (value == null) value = "";

            nval.push(value.substring(0, sStart));
            nval.push(text);
            nval.push(value.substring(sEnd));
            this.setValue(nval.join(""));

            var finishPos = sStart + text.length;
            ta.setTextSelection(finishPos, finishPos);
            tel.scrollToY(scrollY);
        },

        _applyHelpSite : function(value, old) {
            this.__updateHelpControls();
        },

        //////////////////////////////////////////////////////////////////////////
        /////////////////////////   Helpers    ///////////////////////////////////
        //////////////////////////////////////////////////////////////////////////
        __mediaWikiUL : function(data) {
            var val = [];
            val.push("");
            val.push("* " + this.tr("First"));
            val.push("* " + this.tr("Second"));
            val.push("** " + this.tr("First for second"));
            val.push("* " + this.tr("Third"));
            val.push("");
            return val.join("\n");
        },

        __markdownUL : function(data) {
            var val = [];
            val.push("");
            val.push("* " + this.tr("First"));
            val.push("* " + this.tr("Second"));
            val.push("    * " + this.tr("First for second"));
            val.push("* " + this.tr("Third"));
            val.push("");
            return val.join("\n");
        },

        __mediaWikiOL : function(data) {
            var val = [];
            val.push("");
            val.push("# " + this.tr("First"));
            val.push("# " + this.tr("Second"));
            val.push("## " + this.tr("First for second"));
            val.push("# " + this.tr("Third"));
            val.push("");
            return val.join("\n");
        },

        __markdownOL : function(data) {
            var val = [];
            val.push("");
            val.push("1. " + this.tr("First"));
            val.push("1. " + this.tr("Second"));
            val.push("    1. " + this.tr("First for second"));
            val.push("1. " + this.tr("Third"));
            val.push("");
            return val.join("\n");
        },

        __mediaWikiTree : function(data) {
            var val = [];
            val.push("");
            val.push("<tree open=\"true\">");
            val.push("- " + this.tr("Root"));
            val.push("-- " + this.tr("Child 1"));
            val.push("--- " + this.tr("Child level 3"));
            val.push("-- " + this.tr("Child 2"));
            val.push("</tree>");
            val.push("");
            return val.join("\n");
        },

        __mediaWikiNote : function(data) {
            var val = [];
            val.push("");
            val.push("<note>");
            val.push(this.tr("Note text"));
            val.push("</note>");
            val.push("");
            return val.join("\n");
        },

        __markdownNote : function(data) {
            var val = [];
            val.push("");
            val.push("<note>");
            val.push(this.tr("Note text"));
            val.push("</note>");
            val.push("");
            return val.join("\n");
        },

        __mediaWikiTable : function(data) {
            var tm = data[0];
            var isWide = data[1];
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
            tspec.push("{| class=" + (isWide == true ? "'tableWide'" : "'tableShort'"));
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

    destruct : function() {
        this.__editorControls = null;
        this.__helpControls = null;
    }
});