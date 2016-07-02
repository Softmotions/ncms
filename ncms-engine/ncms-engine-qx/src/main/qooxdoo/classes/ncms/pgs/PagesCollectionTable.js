/**
 * Pages collection table
 */
qx.Class.define("ncms.pgs.PagesCollectionTable", {
    extend: qx.ui.core.Widget,
    include: [
        sm.event.MForwardEvent
    ],

    events: {

        "cellDbltap": "qx.ui.table.pane.CellEvent",

        "syncState": "qx.event.type.Event"
    },

    properties: {

        appearance: {
            refine: true,
            init: "toolbar-table"
        }
    },

    construct: function (options) {
        this.__options = options || {};
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
        this.getChildControl("toolbar");
        this.getChildControl("selector");
    },

    members: {

        __options: null,

        getSelectedPage: function () {
            return this.getChildControl("selector").getSelectedPage();
        },

        __onAddPage: function () {
            var dlg = new ncms.pgs.PagesSelectorDlg(null, false,
                {accessAll: this.__options["accessAll"]});
            dlg.addListener("completed", function (ev) {
                var data = ev.getData();
                //{"id":23,"name":"Разметка",
                // "accessMask":"wnd",
                // "idPath":[22,23],"labelPath":["Помощь","Разметка"],
                // "guidPath":["982f9f908e27d0b95e59f9d2af6ad66a","9cc2df6307dbb7821d53641b9dd81338"]}
                //qx.log.Logger.info("onAddPage data=" + JSON.stringify(data) + " collection=" + this.__options["collection"]);
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("pages.collection",
                        {
                            "collection": this.__options["collection"],
                            "id": data["id"]
                        }), "PUT");
                req.send(function () {
                    dlg.close();
                    this.getChildControl("selector").refresh(true);
                }, this);
            }, this);
            dlg.open();
        },

        __onRemovePage: function () {
            var selector = this.getChildControl("selector");
            var page = this.getSelectedPage();
            if (page == null) {
                return;
            }
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("pages.collection",
                    {
                        "collection": this.__options["collection"],
                        "id": page["id"]
                    }), "DELETE");
            req.send(function () {
                selector.refresh(true);
            }, this);
        },

        _createToolbarItems: function (toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance": "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/actions/add.png");
            bt.setToolTipText(this.tr("Add page"));
            bt.addListener("execute", this.__onAddPage, this);
            part.add(bt);


            bt = this.__delBt = this._createButton(null, "ncms/icon/16/actions/delete.png").set({enabled: false});
            bt.setToolTipText(this.tr("Remove page"));
            bt.addListener("execute", this.__onRemovePage, this);
            part.add(bt);

            return toolbar;
        },

        _createChildControlImpl: function (id) {
            var control;
            switch (id) {
                case "toolbar":
                    control = new qx.ui.toolbar.ToolBar();
                    this._createToolbarItems(control);
                    this._add(control);
                    break;
                case "selector":
                    control = new ncms.pgs.PagesSearchSelector({
                        "collection": this.__options["collection"]
                    }, ["icon", "label", "path"]);
                    control.set({searchIfEmpty: true});
                    control.addListener("itemSelected", this._syncState, this);
                    control.getTable().addListener("cellDbltap", this.forwardEvent, this);
                    control.refresh(true);
                    this._add(control, {flex: 1});
                    break;
            }
            return control || this.base(arguments, id);
        },

        _createButton: function (label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance": "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        _syncState: function () {
            var page = this.getSelectedPage();
            this.__delBt.setEnabled(page != null);
            if (this.hasListener("syncState")) {
                this.fireEvent("syncState");
            }
        }
    },

    destruct: function () {
        this.__delBt = null;
    }
});
