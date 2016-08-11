/**
 * User agent mtt filter
 */
qx.Class.define("ncms.mtt.filters.MttUserAgentFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],


    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("User agent");
        },

        getType: function () {
            return "useragent";
        },

        specForHuman: function (spec) {
            var ret = [];
            if (spec["desktop"]) ret.push(qx.locale.Manager.tr("Desktop"));
            if (spec["mobile"]) ret.push(qx.locale.Manager.tr("Phone"));
            if (spec["tablet"]) ret.push(qx.locale.Manager.tr("Tablet"));
            if (spec["android"]) ret.push(qx.locale.Manager.tr("Android"));
            if (spec["ios"]) ret.push(qx.locale.Manager.tr("iOS"));
            if (spec["osx"]) ret.push(qx.locale.Manager.tr("OSX"));
            if (spec["windows"]) ret.push(qx.locale.Manager.tr("Windows"));
            if (spec["unix"]) ret.push(qx.locale.Manager.tr("Linux/Unix"));
            if (spec["webkit"]) ret.push(qx.locale.Manager.tr("WebKit"));
            if (spec["gecko"]) ret.push(qx.locale.Manager.tr("Gecko"));
            if (spec["trident"]) ret.push(qx.locale.Manager.tr("Trident"));
            if (spec["endge"]) ret.push(qx.locale.Manager.tr("Edge"));
            if (ret.length == 0) {
                ret.push(qx.locale.Manager.tr("All user agents"));
            }
            return ret.join(",");
        }
    },

    members: {

        createWidget: function (spec) {
            var form = new qx.ui.form.Form();

            // Device type
            form.addGroupHeader(this.tr("Device type"));
            var cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["desktop"]);
            form.add(cb, this.tr("Desktop"), null, "desktop");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["mobile"]);
            form.add(cb, this.tr("Phone"), null, "mobile");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["tablet"]);
            form.add(cb, this.tr("Tablet"), null, "tablet");

            // OS type
            form.addGroupHeader(this.tr("OS type"));
            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["android"]);
            form.add(cb, this.tr("Android"), null, "android");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["ios"]);
            form.add(cb, this.tr("iOS"), null, "ios");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["osx"]);
            form.add(cb, this.tr("OSX"), null, "osx");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["windows"]);
            form.add(cb, this.tr("Windows"), null, "windows");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["unix"]);
            form.add(cb, this.tr("Linux/Unix"), null, "unix");

            // Engine type
            form.addGroupHeader(this.tr("Renderer engine type"));
            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["webkit"]);
            form.add(cb, this.tr("WebKit"), null, "webkit");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["gecko"]);
            form.add(cb, this.tr("Gecko"), null, "gecko");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["trident"]);
            form.add(cb, this.tr("Trident"), null, "trident");

            cb = new qx.ui.form.CheckBox();
            cb.setValue(!!spec["edge"]);
            form.add(cb, this.tr("Edge"), null, "edge");

            return new sm.ui.form.ExtendedDoubleFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            console.log(JSON.stringify(Object.keys(items)));
            var spec = {};
            Object.keys(items).forEach(function(k) {
                spec[k] = items[k].getValue()
            });
            return spec;
        }
    },

    destruct: function () {
    }
});