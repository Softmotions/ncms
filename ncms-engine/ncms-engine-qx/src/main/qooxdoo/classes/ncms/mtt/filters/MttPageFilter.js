/**
 * Page filter.
 */
qx.Class.define("ncms.mtt.filters.MttPageFilter", {
    extend: qx.core.Object,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Page filter");
        },

        getType: function () {
            return "page";
        },

        specForHuman: function (spec) {
            return spec["pageref"] != null ? spec["pageref"] : "";
        }
    },

    members: {

        createWidget: function (spec) {
            var form = new qx.ui.form.Form();
            var bf = new sm.ui.form.ButtonField(this.tr("Page"), "ncms/icon/16/misc/chain-plus.png");
            bf.setShowResetButton(true);
            bf.setReadOnly(true);
            bf.setRequired(true);
            bf.setPlaceholder(this.tr("Please set a page link"));
            if (spec["pageref"] != null) {
                bf.setValue(spec["pageref"]);
            }
            bf.addListener("reset", function () {
                bf.resetValue();
            });
            bf.addListener("execute", function () {
                var dlg = new ncms.pgs.LinkSelectorDlg(this.tr("Please set a page link"), {
                    allowExternalLinks: "false"
                });
                dlg.addListener("completed", function (ev) {
                    var data = ev.getData();
                    //{"id":1,"name":"Лендинги","accessMask":"ownd","idPath":[1],"labelPath":["Лендинги"],
                    // "guidPath":["d76200ca883563e4d971e38951b332c0"],"linkText":"Лендинги","externalLink":null}
                    var val = [];
                    val.push("page:" + sm.lang.Array.lastElement(data["guidPath"]));
                    if (!sm.lang.String.isEmpty(data["linkText"])) {
                        val.push(data["linkText"]);
                    }
                    bf.setValue(val.join(" | "));
                    dlg.close();
                }, this);
                dlg.open();
            }, this);
            form.add(bf, this.tr("Page"), null, "pageref");
            return new sm.ui.form.FlexFormRenderer(form);
        },

        asSpec: function (w) {
            var form = w._form;
            if (form == null || !form.validate()) { // form is not valid
                return null;
            }
            var items = form.getItems();
            var spec = {};
            spec["pageref"] = items["pageref"].getValue();
            return spec;
        }
    },

    destruct: function () {
    }
});
