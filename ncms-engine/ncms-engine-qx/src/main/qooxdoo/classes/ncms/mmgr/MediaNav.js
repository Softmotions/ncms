/**
 * Media folders tree
 */
qx.Class.define("ncms.mmgr.MediaNav", {
    extend: ncms.mmgr.MediaItemTreeSelector,

    statics: {
        MMF_EDITOR_CLAZZ: "ncms.mmgr.MediaFolderEditor"
    },

    construct: function () {
        this.base(arguments, true);

        //Register media folder editor
        var eclazz = ncms.mmgr.MediaNav.MMF_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.mmgr.MediaFolderEditor();
        }, null, this);

        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz) {
                app.showWSA(eclazz);
            }
        }, this);

        this.addListener("itemSelected", function (ev) {
            var data = ev.getData();
            if (data == null) {
                app.showDefaultWSA();
            } else {
                app.getWSA(eclazz).setItem(data);
                app.showWSA(eclazz);
            }
        })
    }
});