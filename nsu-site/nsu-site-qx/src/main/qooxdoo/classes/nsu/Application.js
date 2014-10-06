/**
 * Nsu site application.
 */
qx.Class.define("nsu.Application", {
    extend : ncms.Application,

    members : {

        main : function() {
            this.base(arguments);
            //Extension point for legacy
            this.registerExtensionPoint(
                    "ncms.pgs.PageEditorEditPage.HEADER_BUTTONS",
                    this.__initLegacyEP.bind(this));
        },

        createActions : function() {
            return new nsu.Actions();
        },

        __initLegacyEP : function(ep, cont) {
            var bt = new qx.ui.menu.Button(this.tr("Import data from old nsu.ru"));
            cont.add(bt);
            bt.addListener("execute", function(ev) {
                var dlg = new nsu.legacy.ImportLegacyDataDlg(ep.getPageSpec()["id"]);
                dlg.addListener("completed", function(ev) {
                    dlg.close();
                    var spec = ev.getData();
                    var items = ep.getForm().getItems();
                    if (spec && typeof spec["wiki"] === "string" &&
                            (items["content"] != null || items["structure_wiki"] != null)) {
                        var w = items["content"] || items["structure_wiki"];
                        w.setValue(spec["wiki"]);
                    }
                    ncms.Application.infoPopup(this.tr("Import completed successfully"));
                }, this);
                dlg.open();
            }, this);
        }
    }
});