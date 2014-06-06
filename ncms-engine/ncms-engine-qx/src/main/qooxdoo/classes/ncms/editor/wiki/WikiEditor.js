/**
 * Wiki editor
 *
 * @asset(ncms/icon/16/wiki/note_add.png)
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
qx.Class.define("ncms.editor.wiki.WikiEditor", {
    extend : qx.ui.core.Widget,
    implement : [
        qx.ui.form.IStringForm,
        qx.ui.form.IForm
    ],
    include : [
        qx.ui.form.MForm,
        qx.ui.core.MChildrenHandling
    ],


    statics : {
        createTextSurround : function(text, level, pattern, trails) {
            var nval = [];

            var hfix = qx.lang.String.repeat(pattern, level < 1 ? 1 : level);
            nval.push(hfix);
            nval.push(trails||"");
            nval.push(text);
            nval.push(trails||"");
            nval.push(hfix);

            return nval.join("");
        }
    },

    events : {
    },

    properties : {
        "type" : {
            check: ["mediaWiki", "markdown"],
            init : "mediaWiki",
            apply : "_applyType"
        }
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(4));

        this.getChildControl("toolbar");
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

        __lastSStart : 0,

        __lastSEnd : 0,

        addListener : function(type, listener, self, capture) {
            switch (type) {
                default:
                    //todo scary hack
                    this.getChildControl("textarea").addListener(type, listener, self, capture);
                    break;
            }
        },

        // overridden
        setValue : function(value) {
            this.getChildControl("textarea").setValue(value);
        },

        // overridden
        resetValue : function() {
            this.getChildControl("textarea").resetValue();
        },

        // overridden
        getValue : function() {
            return this.getChildControl("textarea").getValue();
        },

        //overriden
        _applyEnabled : function(value, old) {
            this.base(arguments, value, old);
            this.getChildControl("textarea").setEnabled(value);
        },

        _applyType : function(value, old) {
            // TODO change wiki edtor type. may be hide/show controls applied only for selected editor type?
        },

        //overriden
        _createChildControlImpl : function(id) {
            var control;
            switch (id) {
                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar().set({overflowHandling : true, "show" : "icon"});
                    this._add(control, {flex : 0});
                    this.__lastToolbarItem = control.addSpacer();
                    var overflow = new qx.ui.toolbar.MenuButton(this.tr("More..."));
                    overflow.setMenu(new qx.ui.menu.Menu());
                    control.add(overflow);
                    control.setOverflowIndicator(overflow);
                    this.__initToolbar(control);
                    break;

                case "textarea":
                    control = new qx.ui.form.TextArea();
                    this._add(control, {flex : 1});
                    break;
            }

            return control || this.base(arguments, id);
        },

        addToolbarControl : function(options) {
            this._addToolbarControl(this.getChildControl("toolbar"), options);
        },

        _addToolbarControl : function(toolbar, options) {
            var callback = this.__buildToolbarControlAction(options);

            this.__createToolbarControl(toolbar, this.__lastToolbarItem, qx.ui.toolbar.Button, callback, options);
            if (toolbar.getOverflowIndicator()) {
                this.__createToolbarControl(toolbar.getOverflowIndicator().getMenu(), null, qx.ui.menu.Button, callback, options);
            }
        },

        __buildToolbarControlAction : function(options) {
            var me = this;
            var callback = function(data) {
                var cbname = "insert" + qx.lang.String.capitalize(me.getType());
                if (options[cbname]) {
                    options[cbname].call(me, me.__insertText, data);
                }
            };
            return function() {
                var selectedText = this.getChildControl("textarea").getContentElement().getTextSelection();
                if (options["prompt"]) {
                    options["prompt"].call(this, this, selectedText, callback);
                } else {
                    callback.call(this, selectedText);
                }
            };
        },

        __createToolbarControl : function(toolbar, before, btclass, callback, options) {
            var bt = new btclass(options["title"], options["icon"]);
            if (options["tooltip"]) {
                bt.setToolTip(new qx.ui.tooltip.ToolTip(options["tooltipText"]));
            }
            bt.addListener("execute", callback, this);

            if (before) {
                toolbar.addBefore(bt, before);
            } else {
                toolbar.add(bt);
            }

            return bt;
        },

        __initToolbar : function(toolbar) {
            if (true/* TODO: wiki help */) {
                var helpButton = new qx.ui.toolbar.Button("Help", "ncms/icon/16/help/help.png");
                toolbar.add(helpButton);
                helpButton.addListener("execute", function() {
                    ncms.Application.alert("TODO");
                });
            }

            var cprompt = function(title) {
                return function(editor, sText, cb) {
                    cb.call(this, sText ? sText : prompt(title));
                }
            };
            var csurround = function(level, pattern, trails) {
                return function(cb, data) {
                    cb.call(this, ncms.editor.wiki.WikiEditor.createTextSurround(data, level, pattern, trails));
                }
            };

            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_heading_1.png",
                tooltipText : this.tr("Heading 1"),
                prompt : cprompt(this.tr("Header text")),
                insertMediaWiki : csurround(1, "=", " "),
                insertMarkdown : csurround(1, "#", " ")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_heading_2.png",
                tooltipText : this.tr("Heading 2"),
                prompt : cprompt(this.tr("Header text")),
                insertMediaWiki : csurround(2, "=", " "),
                insertMarkdown : csurround(2, "#", " ")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_heading_3.png",
                tooltipText : this.tr("Heading 3"),
                prompt : cprompt(this.tr("Header text")),
                insertMediaWiki : csurround(3, "=", " "),
                insertMarkdown : csurround(3, "#", " ")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_bold.png",
                tooltipText : this.tr("Bold"),
                prompt : cprompt(this.tr("Bold text")),
                insertMediaWiki : csurround(1, "'", ""),
                insertMarkdown : csurround(2, "*", "")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_italic.png",
                tooltipText : this.tr("Italic"),
                prompt : cprompt(this.tr("Italics text")),
                insertMediaWiki : csurround(2, "'", ""),
                insertMarkdown : csurround(1, "*", "")
            });

            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_list_bullets.png",
                tooltipText : this.tr("Bullet list"),
                insertMediaWiki : function(cb, data) {
                    var val = [];
                    val.push("");
                    val.push("* Один");
                    val.push("* Два");
                    val.push("** Первый у второго");
                    val.push("* Три");
                    val.push("");
                    cb.call(this, val.join("\n"))
                },
                insertMarkdown : function(cb, data) {
                    var val = [];
                    val.push("");
                    val.push("* Один");
                    val.push("* Два");
                    val.push("    * Первый у второго");
                    val.push("* Три");
                    val.push("");

                    cb.call(this, val.join("\n"))
                }
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/text_list_numbers.png",
                tooltipText : this.tr("Numbered list"),
                insertMediaWiki : function(cb, data) {
                    var val = [];
                    val.push("");
                    val.push("# Один");
                    val.push("# Два");
                    val.push("## Первый у второго");
                    val.push("# Три");
                    val.push("");
                    cb.call(this, val.join("\n"))
                },
                insertMarkdown : function(cb, data) {
                    var val = [];
                    val.push("");
                    val.push("1. Один");
                    val.push("1. Два");
                    val.push("    1. Первый у второго");
                    val.push("1. Три");
                    val.push("");

                    cb.call(this, val.join("\n"))
                }
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/link_add.png",
                tooltipText : this.tr("Link to another page")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/image_add.png",
                tooltipText : this.tr("Add image|link to file")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/table_add.png",
                tooltipText : this.tr("Add table")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/tree_add.png",
                tooltipText : this.tr("Add tree")
            });
            this._addToolbarControl(toolbar, {
                icon : "ncms/icon/16/wiki/note_add.png",
                tooltipText : this.tr("Create note")
            });
        },

        // TODO: copied
        _getSelectionStart : function() {
            var sStart = this.getChildControl("textarea").getTextSelectionStart();
            return (sStart == null || sStart == -1 || sStart == 0) ? this.__lastSStart : sStart;
        },

        _getSelectionEnd : function() {
            var sEnd = this.getChildControl("textarea").getTextSelectionEnd();
            return (sEnd == null || sEnd == -1 || sEnd == 0) ? this.__lastSEnd : sEnd;
        },


        __insertText : function(text) {
            var ta = this.getChildControl("textarea");
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
            ta.setValue(nval.join(""));

            var finishPos = sStart + text.length;
            ta.setTextSelection(finishPos, finishPos);
            tel.scrollToY(scrollY);
        }
    },

    destruct : function() {
    }
});