/**
 * News navigator
 *
 * @asset(ncms/icon/16/misc/chain-plus.png)
 */
qx.Class.define("ncms.news.NewsNav", {
    extend: qx.ui.core.Widget,
    include: [ncms.cc.MCommands],

    statics: {

        NEWS_EDITOR_CLAZZ: "ncms.news.NewsEditor"
    },

    events: {},

    properties: {},

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox(4));
        this.setPadding([0, 0, 0, 10]);

        //Register page editor
        var eclazz = ncms.news.NewsNav.NEWS_EDITOR_CLAZZ;
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function () {
            return new ncms.news.NewsEditor();
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

        var bf = this.__bf = new sm.ui.form.ButtonField(null, "ncms/icon/16/misc/chain-plus.png");
        bf.setReadOnly(true);
        bf.getMainButton().setToolTipText(this.tr("Select the parent page for news"));
        bf.addListener("execute", this.__choosePage, this);
        bf.setPlaceholder(this.tr("Select the parent page for news"));
        this._add(bf);

        var ps = this.__ps = new ncms.pgs.PagesSearchSelector(
            {
                type: "news.page",
                sortDesc: "mdate"
            },
            ["icon", "label"],
            {
                label: {
                    sortable: false
                }
            }
        );
        this._add(ps, {flex: 1});

        ps.addListener("itemSelected", function (ev) {
            var data = ev.getData();
            if (data == null) {
                app.showDefaultWSA();
            } else {
                app.getWSA(eclazz).setPageSpec({
                    id: data["id"],
                    name: data["label"]
                });
                app.showWSA(eclazz);
            }
        }, this);

        ps.setContextMenu(new qx.ui.menu.Menu());
        ps.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

        this.__syncState();
        this.__loadCurrentNewsRoot();

        ncms.Events.getInstance().addListener("pageChangePublished", this.__onPagePublished, this);

        // Init shortcuts
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Alt+Insert"),
            this.__onNews, this);
        this._registerCommand(
            new sm.ui.core.ExtendedCommand("Delete"),
            this.__onDelete, this);
        this._registerCommandFocusWidget(ps.getTable());

    },

    members: {

        /**
         * Linked pages bf
         */
        __bf: null,

        /**
         * Pages selector
         */
        __ps: null,

        getSelectedPage: function () {
            var sp = this.__ps.getSelectedPage();
            return (sp == null) ? null : {id: sp["id"], name: sp["label"]};
        },

        __beforeContextmenuOpen: function (ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();
            var bt;
            var root = this.__getRootPage();
            if (root == null) {
                return;
            }
            var sp = this.__ps.getSelectedPage();
            bt = new qx.ui.menu.Button(this.tr("New"));
            bt.addListenerOnce("execute", this.__onNews, this);
            menu.add(bt);
            if (sp != null) {
                bt = new qx.ui.menu.Button(this.tr("Rename"));
                bt.addListenerOnce("execute", this.__onRename, this);
                menu.add(bt);

                bt = new qx.ui.menu.Button(this.tr("Delete"));
                bt.addListenerOnce("execute", this.__onDelete, this);
                menu.add(bt);
            }
        },

        __onRename: function (ev) {
            var sp = this.__ps.getSelectedPage();
            if (sp == null) {
                return;
            }
            var dlg = new ncms.news.NewsRenameDlg(sp["id"], sp["label"]);
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.__ps.refresh(true);
            }, this);
            dlg.placeToWidget(ev.getTarget(), false);
            dlg.open();
        },

        __onDelete: function (ev) {
            var sp = this.__ps.getSelectedPage();
            if (sp == null) {
                return;
            }
            ncms.Application.confirm(this.tr("Are you sure to remove page: %1", sp["label"]), function (yes) {
                if (!yes) return;
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("pages.delete", {id: sp["id"]}), "DELETE");
                req.send(function () {
                    this.__ps.refresh(true);
                }, this);
            }, this);
        },

        __onNews: function (ev) {
            var root = this.__getRootPage();
            var dlg = new ncms.news.NewsNewDlg(root["id"]);
            dlg.addListener("completed", function (ev) {
                dlg.close();
                this.__ps.refresh(true);
            }, this);
            if (ev.getTarget().getContentLocation) {
                dlg.placeToWidget(ev.getTarget(), false);
            } else {
                dlg.placeToWidget(this.__ps.getTable(), false);
            }
            dlg.open();
        },

        __onPagePublished: function (ev) {
            var data = ev.getData();
            var published = data["published"];
            var id = data["id"];
            var table = this.__ps.getTable();
            table.getTableModel().updateCachedRows(function (ind, rowdata) {
                if (rowdata["id"] === id) {
                    rowdata["icon"] = published ? "" : "ncms/icon/16/misc/exclamation.png";
                    return rowdata;
                }
            }, this);
        },

        __choosePage: function () {
            var dlg = new ncms.pgs.PagesCollectionDlg(this.tr("Please select the parent page for news"),
                {
                    collection: "news.root.pages",
                    accessAll: "n" //Required news editing access rights
                });
            dlg.addListener("completed", function (ev) {
                this.__updateNewsRoot(ev.getData());
                dlg.close();
            }, this);
            dlg.open();
        },

        __loadCurrentNewsRoot: function () {
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.single.get",
                {"collection": "news.root"}), "GET", "application/json");
            req.send(function (resp) {
                this.__setNewsRoot(resp.getContent());
            }, this);
        },

        __updateNewsRoot: function (page) {
            //rs/adm/pages/single/{collection}/{id}
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.single",
                {collection: "news.root", id: page["id"]}), "PUT", "application/json");
            req.send(function (resp) {
                this.__setNewsRoot(resp.getContent());
            }, this);
        },

        __setNewsRoot: function (page) {
            var bf = this.__bf;
            if (page == null || Object.keys(page).length == 0) {
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
                parentId: page["id"]
            });
            this.__syncState();
        },

        __getRootPage: function () {
            return this.__bf.getUserData("page");
        },

        __syncState: function () {
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

        _applyEnabled: function (value, old) {
            this.base(arguments, value, old);
            this.__bf.setEnabled(value);
            this.__ps.setEnabled(value);
        }
    },

    destruct: function () {
        ncms.Events.getInstance().removeListener("pageChangePublished", this.__onPagePublished, this);
        this.__bf = null;
        this.__ps = null;
        //this._disposeObjects("__field_name");                                
    }
});