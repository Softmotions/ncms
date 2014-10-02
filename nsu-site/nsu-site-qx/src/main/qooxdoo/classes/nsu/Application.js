/**
 * Nsu site application
 *
 *
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
            var bt = new qx.ui.menu.Button(this.tr("Import files from old nsu.ru"));
            cont.add(bt);
            bt.addListener("execute", function() {
                qx.log.Logger.info("Import file from old nsu.ru");
            });
        }

    }
});