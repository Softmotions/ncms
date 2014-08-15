/**
 * News navigator
 *
 * @asset(ncms/icon/16/misc/chain-plus.png)
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
        this.setPadding([0, 0, 0, 10]);

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

        var bf = this.__bf = new sm.ui.form.ButtonField(null, "ncms/icon/16/misc/chain-plus.png");
        bf.setReadOnly(true);
        bf.getMainButton().setToolTipText(this.tr("Select the parent page for news"));
        bf.addListener("execute", this.__choosePage, this);
        bf.setPlaceholder(this.tr("Select the parent page for news"));
        this._add(bf);

        var ps = this.__ps = new ncms.pgs.PagesSearchSelector({
            "type" : "news.page"
        }).set({searchIfEmpty : true});
        this._add(ps, {flex : 1});

        ps.addListener("itemSelected", function(ev) {
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

        ps.getTable().setContextMenu(new qx.ui.menu.Menu());
        ps.getTable().addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        this.__syncState();
        this.__loadCurrentNewsRoot();
    },

    members : {

        /**
         * Linked pages bf
         */
        __bf : null,

        /**
         * Pages selector
         */
        __ps : null,

        getSelectedPage : function() {
            var sp = this.__ps.getSelectedPage();
            return (sp == null) ? null : {id : sp["id"], name : sp["label"]};
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt;
            var root = this.__getRootPage();
            if (root == null) {
                return;
            }
            bt = new qx.ui.menu.Button(this.tr("New"));
            bt.addListener("execute", this.__onNews, this);
            menu.add(bt);
        },

        __onNews : function(ev) {
            var root = this.__getRootPage();
            var dlg = new ncms.news.NewsNewDlg(root["id"]);
            dlg.addListener("completed", function(ev) {
                dlg.close();
                this.__ps.refresh();
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __choosePage : function() {
            var dlg = new ncms.pgs.PagesCollectionDlg(this.tr("Please select the parent page for news"),
                    {
                        collection : "news.root.pages",
                        accessAll : "n" //Required news editing access rights
                    });
            dlg.addListener("completed", function(ev) {
                this.__updateNewsRoot(ev.getData());
                dlg.close();
            }, this);
            dlg.open();
        },

        __loadCurrentNewsRoot : function() {
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.single.get",
                    {"collection" : "news.root"}), "GET", "application/json");
            req.send(function(resp) {
                this.__setNewsRoot(resp.getContent());
            }, this);
        },

        __updateNewsRoot : function(page) {
            //rs/adm/pages/single/{collection}/{id}
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.single",
                    {collection : "news.root", id : page["id"]}), "PUT", "application/json");
            req.send(function(resp) {
                this.__setNewsRoot(resp.getContent());
            }, this);
        },

        __setNewsRoot : function(page) {
            var bf = this.__bf;
            if (page == null) {
                bf.resetValue();
                bf.setUserData("page", null);
                this.__syncState();
                return;
            }
            bf.setUserData("page", page);
            if (page["labelPath"].length > 1) {
                bf.setValue(page["name"] + " | " + page["labelPath"].join("/"));
            } else {
                bf.setValue(page["name"]);
            }
            this.__ps.updateViewSpec({
                parentId : page["id"]
            });
            this.__syncState();
        },

        __getRootPage : function() {
            return this.__bf.getUserData("page");
        },

        __syncState : function() {
            var page = this.__getRootPage();
            if (page == null) {
                this.__ps.setEnabled(false);
                ncms.Application.INSTANCE.showDefaultWSA();
                this.__bf.getMainButton().addState("invalid");
            } else {
                this.__ps.setEnabled(true);
                this.__bf.getMainButton().removeState("invalid");
            }
        },

        _applyEnabled : function(value, old) {
            this.base(arguments, value, old);
            this.__bf.setEnabled(value);
            this.__ps.setEnabled(value);
        }
    },

    destruct : function() {
        this.__bf = null;
        this.__ps = null;
        //this._disposeObjects("__field_name");                                
    }
});