/**
 * News navigator
 */
qx.Class.define("ncms.news.NewsNav", {
    extend : qx.ui.core.Widget,

    statics : {

        NEWS_EDITOR_CLAZZ : "ncms.news.NewsEditor"
    },

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(4));

        //Register page editor
        var eclazz = ncms.news.NewsNav.NEWS_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.news.NewsEditor();
        }, null, this);

        this.addListener("disappear", function() {
            //Navigation side is inactive so hide page editor pane if it not done already
            if (app.getActiveWSAID() == eclazz) {
                app.showDefaultWSA();
            }
        }, this);
        this.addListener("appear", function() {
            if (app.getActiveWSAID() != eclazz) {
                if (this.getSelectedPage() != null) {
                    app.showWSA(eclazz);
                } else {
                    app.showDefaultWSA();
                }
            }
        }, this);

        this.__ps = new ncms.pgs.PagesSearchSelector({
            "type" : "page.news"
        }).set({searchIfEmpty : true});
        this._add(this.__ps, {flex : 1});

        this.__ps.addListener("itemSelected", function(ev) {
            var data = ev.getData();
            if (data == null) {
                app.showDefaultWSA();
            } else {
                app.getWSA(eclazz).setPageSpec({
                    id : data["id"],
                    name : data["label"]
                });
                app.showWSA(eclazz);
            }
        }, this);
    },

    members : {

        /**
         * Pages selector
         */
        __ps : null,

        getSelectedPage : function() {
            var sp = this.__ps.getSelectedPage();
            return (sp == null) ? null : {id : sp["id"], name : sp["label"]};
        }

    },

    destruct : function() {
        this.__ps = null;
        //this._disposeObjects("__field_name");                                
    }
});