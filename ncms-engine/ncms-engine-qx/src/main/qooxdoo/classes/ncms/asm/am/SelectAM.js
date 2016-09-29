/**
 * Select box/list attribute controller
 */
qx.Class.define("ncms.asm.am.SelectAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "select",
            hidden: false,
            requiredSupported: true
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Selectbox");
        },

        getMetaInfo: function () {
            return ncms.asm.am.SelectAM.__META;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {

            var form = new qx.ui.form.Form();

            //---------- Options
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var el = new qx.ui.form.RadioButtonGroup(new qx.ui.layout.HBox(4));
            el.add(new qx.ui.form.RadioButton(this.tr("list")).set({"model": "list"}));
            el.add(new qx.ui.form.RadioButton(this.tr("selectbox")).set({"model": "selectbox"}));
            el.setModelSelection(opts["display"] ? [opts["display"]] : ["selectbox"]);
            el.addListener("changeSelection", this.__syncState, this);
            form.add(el, this.tr("Display as"), null, "display");

            var table = new ncms.asm.am.SelectAMTable();

            el = new qx.ui.form.CheckBox();
            if (opts["multiselect"] != null) {
                el.setValue(opts["multiselect"] == "true" || opts["multiselect"] === true);
            }
            el.addListener("changeValue", this.__syncState, this);
            form.add(el, this.tr("Multi select"), null, "multiselect");

            el = new qx.ui.form.TextField();
            if (opts["fetchfrom"] != null) {
                el.setValue(opts["fetchfrom"]);
            }
            el.addListener("input", this.__syncState, this);
            form.add(el, this.tr("Fetch from"), null, "fetchfrom");

            //---------- Table
            this._fetchAttributeValue(attrSpec, function (val) {
                try {
                    table.setData(JSON.parse(val));
                } catch (e) {
                    qx.log.Logger.error("Failed to apply table value", e);
                }
            });
            form.add(table, this.tr("Items"), null, "table");
            this._form = form;

            this.__syncState();
            return new sm.ui.form.FlexFormRenderer(form);
        },

        __syncState: function () {
            if (this._form == null) {
                return;
            }
            var items = this._form.getItems();
            var table = items["table"];
            var multiselect = items["multiselect"];
            var fetchfrom = items["fetchfrom"];
            var display = items["display"].getModelSelection().getItem(0);
            multiselect.setEnabled(display == "list");
            table.setCheckMode((multiselect.getEnabled() && multiselect.getValue()) ? "multiply" : "single");
            table.setEnabled(sm.lang.String.isEmpty(fetchfrom.getValue()));
        },

        optionsAsJSON: function () {
            var items = this._form.getItems();
            var table = items["table"];
            var value = (table.isEnabled() ? table.toJSONValue() : null);
            return {
                multiselect: items["multiselect"].getEnabled() ? items["multiselect"].getValue() : false,
                display: items["display"].getModelSelection().getItem(0),
                fetchfrom: items["fetchfrom"].getValue(),
                value: value
            };
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            var opts = ncms.Utils.parseOptions(attrSpec["options"]);
            var display = opts["display"];
            var w = null;
            if (display === "selectbox") {
                w = this.__createSingleSelectVW(attrSpec, asmSpec, opts);
            } else if (display === "list") {
                w = this.__createListVW(attrSpec, asmSpec, opts);
            }
            if (w) {
                w.setUserData("options", opts);
            }
            this._valueWidget = w;
            return w;
        },

        __createSingleSelectVW: function (attrSpec, asmSpec, opts) {
            var w = new qx.ui.form.SelectBox();
            this._fetchAttributeValue(attrSpec, function (val) {
                var items = JSON.parse(val);
                if (items != null) {
                    var selected = null;
                    items.forEach(function (el) {
                        var li = new qx.ui.form.ListItem(el[1], null, el[2]);
                        if (el[0] === true && selected == null) {
                            selected = li;
                        }
                        w.add(li);
                    });
                    if (selected) {
                        w.setSelection([selected]);
                    }
                }
                w.setUserData("value", items);
            }, this);
            return w;
        },

        __applySingleSelectVW: function (w) {
            var value = w.getUserData("value");
            var sel = w.getSelection()[0];
            if (sel == null) {
                return {
                    "value": value
                };
            }
            value.forEach(function (v) {
                v[0] = false;
            });
            var sval = sel.getModel();
            for (var i = 0; i < value.length; ++i) {
                var v = value[i];
                if (v[2] == sval) {
                    v[0] = true;
                    break;
                }
            }
            return {
                "value": value
            };
        },

        __createListVW: function (attrSpec, asmSpec, opts) {
            var w = new qx.ui.form.List();
            w.setHeight(100);
            if (opts["multiselect"] == "true") {
                w.setSelectionMode("multi");
            } else {
                w.setSelectionMode("single");
            }
            this._fetchAttributeValue(attrSpec, function (val) {
                var items = JSON.parse(val);
                if (items != null) {
                    var selection = [];
                    items.forEach(function (el) {
                        var li = new qx.ui.form.ListItem(el[1], null, el[2]);
                        if (el[0] === true) {
                            selection.push(li);
                        }
                        w.add(li);
                    });
                    w.setSelection(selection);
                }
                w.setUserData("value", items);
            }, this);
            return w;
        },


        __applyListVW: function (w) {
            var value = w.getUserData("value");
            var vmap = {};
            value.forEach(function (v) {
                v[0] = false;
                vmap[v[2]] = v;
            });
            var selection = w.getSelection();
            selection.forEach(function (li) {
                var v = vmap[li.getModel()];
                if (v) {
                    v[0] = true;
                }
            });
            return {
                "value": value
            };
        },

        valueAsJSON: function () {
            var w = this._valueWidget;
            var opts = w.getUserData("options");
            var display = opts["display"];
            if (display === "selectbox") {
                return this.__applySingleSelectVW(w);
            } else if (display === "list") {
                return this.__applyListVW(w);
            }
            return {};
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }
});