/**
 * Select box/list attribute controller
 */
qx.Class.define("ncms.asm.am.SelectAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],


    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Select box");
        },

        getSupportedAttributeTypes : function() {
            return [ "select" ];
        }
    },

    members : {

        _form : null,

        activateOptionsWidget : function(attrSpec, asmSpec) {

            var form = new qx.ui.form.Form();

            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            el.add(new qx.ui.form.RadioButton(this.tr("table")).set({"model" : "table"}));
            el.add(new qx.ui.form.RadioButton(this.tr("selectbox")).set({"model" : "selectbox"}));
            el.setModelSelection(opts["display"] ? [opts["display"]] : ["selectbox"]);
            form.add(el, this.tr("Display as"), null, "display");

            var table = new ncms.asm.am.SelectAMTable();

            el = new qx.ui.form.CheckBox();
            el.addListener("changeValue", function(ev) {
                var val = ev.getData();
                table.setCheckMode(val ? "multiply" : "single");
            });
            if (opts["multiselect"] != null) {
                el.setValue(opts["multiselect"] == "true" || opts["multiselect"] === true);
            }
            form.add(el, this.tr("Multi select"), null, "multiselect");

            //---------- Table
            if (attrSpec["value"]) {
                try {
                    table.setData(JSON.parse(attrSpec["value"]));
                } catch (e) {
                    qx.log.Logger.error("Failed to apply table value", e);
                }
            }
            form.add(table, this.tr("Items"), null, "table");
            this._form = form;
            return new sm.ui.form.FlexFormRenderer(form);
        },

        optionsAsJSON : function() {
            var items = this._form.getItems();
            var table = items["table"];
            var value = table.toJSONValue();
            return {
                multiselect : items["multiselect"].getValue(),
                display : items["display"].getModelSelection().getItem(0),
                value : value
            };
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            //todo
            return new qx.ui.core.Widget();
        },

        valueAsJSON : function() {
            return {};
        }
    },

    destruct : function() {
        this._disposeObjects("_form");
    }
});