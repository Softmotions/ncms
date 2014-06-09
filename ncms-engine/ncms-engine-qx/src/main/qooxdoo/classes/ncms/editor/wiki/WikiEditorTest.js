/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.editor.wiki.WikiEditorTest", {
    extend : qx.ui.core.Widget,

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(5));

        var form = this.__form = new qx.ui.form.Form();
        var vmgr = form.getValidationManager();
        vmgr.setRequiredFieldMessage(this.tr("This field is required"));

        var el;

        el = new qx.ui.form.TextField();
        el.setRequired(true);
        form.add(el, this.tr("Test"), null, "test");

        el = new ncms.editor.wiki.WikiEditor();
        form.add(el, this.tr("Second Wiki"), null, "second-wiki");

        el = new ncms.editor.wiki.WikiEditor();
        el.setRequired(true);
        el.tabFocus();
        form.add(el, this.tr("Wiki"), null, "wiki");

        var fr = new sm.ui.form.FlexFormRenderer(form);
        fr.setLastRowFlexible();
        this._add(fr, {flex : 1});
    },

    members : {
        __form : null,

        setOptions : function(options) {
            var items = this.__form.getItems();
            var we = items["wiki"];
            we.setType(options["type"]);
            we.setHelpSite(options["helpSite"] ? options["helpSite"] : null);
            if (options["additionalBtn"]) {
                var bid = "ABTN" + (options["prompt"] ? "P" : "");
                if (!we.hasToolbarControl(bid)) {
                    we.addToolbarControl({
                        "id" : bid,
                        "tooltipText" : options["title"],
                        "icon" : "ncms/icon/16/misc/cross-script.png",
                        "prompt" : options["prompt"] ? function(cb, editor, stext) {
                            cb.call(this, stext ? stext : prompt());
                        } : null,
                        "insertMediaWiki" : function(cb, data) {
                            cb.call(this, "\n--testW-- " + data + " --testW--\n")
                        },
                        "insertMarkdown" : function(cb, data) {
                            cb.call(this, "\n--testM-- " + data + " --testM--\n")
                        }
                    });
                }
            }
            we.resetToolbarControls();
            if (options["exclude"]) {
                we.excludeToolbarControl(options["exclude"]);
            }

            var swe = items["second-wiki"];
            swe.setType(we.getType() == "mediaWiki" ? "markdown" : "mediaWiki");
        },

        validate : function() {
            return this.__form.validate();
        }
    },

    destruct : function() {
    }
});