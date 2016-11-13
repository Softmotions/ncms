/**
 * Pages selector.
 */
qx.Class.define("ncms.pgs.PagesNav", {
    extend: ncms.pgs.PagesSelector,

    statics: {
        PAGE_EDITOR_CLAZZ: "ncms.pgs.PageEditor"
    },

    events: {},

    properties: {},

    construct: function () {
        this.base(arguments, true);
        this.set({paddingTop: 5, paddingBottom: 5});

        //Register page editor
        var eclazz = ncms.pgs.PagesNav.PAGE_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.pgs.PageEditor();
        }, null, this);

        this.addListener("appear", function () {
            if (app.getActiveWSAID() != eclazz) {
                if (this.getSelectedPage() != null) {
                    app.showWSA(eclazz);
                } else {
                    app.showDefaultWSA();
                }
            }
        }, this);

        this.addListener("pageSelected", function (ev) {
            var data = ev.getData();
            if (data == null) {
                app.showDefaultWSA();
            } else {
                app.getWSA(eclazz).setPageSpec(data);
                app.showWSA(eclazz);
            }
        })
    },

    members: {},

    destruct: function () {
    }
});