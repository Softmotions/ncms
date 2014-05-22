/**
 * Users selector pane.
 */
qx.Class.define("ncms.usr.UsersNav", {
    extend : qx.ui.core.Widget,

    statics : {
    },

    events : {

        /**
         * Event fired if assembly was selected/deselected
         *
         * DATA: var item = {
         *        "id" : {Number} Assembly id.
         *        "name" : {String} User name,
         *        "fullName" : {String} Full user name,
         *       };
         * or null if selection cleared
         */
        "userSelected" : "qx.event.type.Data"
    },

    properties : {

        constViewSpec : {
            check : "Object",
            nullable : true,
            apply : "__applyConstViewSpec"
        }
    },

    construct : function(constViewSpec, smodel) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.Dock());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function() {
            this.__search(null);
        }, this);
        sf.addListener("input", function(ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("changeValue", function(ev) {
            this.__search(ev.getData());
        }, this);


        this.__table = new ncms.usr.UsersTable().set({
            "statusBarVisible" : false,
            "showCellFocusIndicator" : false});

        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function() {
            var user = this.getSelectedUser();
            this.fireDataEvent("userSelected", user ? user : null);
        }, this);

        this._add(this.__sf, {edge : "north"});
        this._add(this.__table, {edge : "center"});

        this.setConstViewSpec(constViewSpec || null);

        this.setContextMenu(new qx.ui.menu.Menu());
        this.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);

    },

    members : {

        /**
         * Search field
         * @type {sm.ui.form.SearchField}
         */
        __sf : null,

        /**
         * Users virtual table
         * @type {ncms.usr.UsersTable}
         */
        __table : null,


        setViewSpec : function(vspec) {
            this.__table.getTableModel().setViewSpec(this.__createViewSpec(vspec));
        },

        updateViewSpec : function(vspec) {
            this.__table.getTableModel().updateViewSpec(this.__createViewSpec(vspec));
        },

        reload : function(vspec) {
            this.__table.getTableModel().reloadData();
            this.__table.resetSelection();
        },

        resetSelection : function() {
            this.__table.resetSelection();
        },

        getTable : function() {
            return this.__table;
        },

        getSelectedUserInd : function() {
            return this.__table.getSelectedUserInd();
        },

        getSelectedUser : function() {
            return this.__table.getSelectedUser();
        },

        getSelectedUsers : function() {
            return this.__table.getSelectedUsers();
        },

        cleanup : function() {
            this.__table.cleanup();
        },

        getSearchField : function() {
            return this.__sf;
        },

        __createViewSpec : function(vspec) {
            if (this.getConstViewSpec() == null) {
                return vspec;
            }
            var nspec = {};
            qx.Bootstrap.objectMergeWith(nspec, this.getConstViewSpec(), false);
            qx.Bootstrap.objectMergeWith(nspec, vspec, false);
            return nspec;
        },

        __search : function(val) {
            this.__table.resetSelection();
            var vspec = (val != null && val != "" ? {stext : val} : {});
            this.setViewSpec(this.__createViewSpec(vspec));
        },

        __applyConstViewSpec : function() {
            this.__search();
        },

        __beforeContextmenuOpen : function(ev) {
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("New user"));
            bt.addListenerOnce("execute", this.__onNewUser, this);
            menu.add(bt);

            var user = this.getSelectedUser();
            if (user !== null) {
                bt = new qx.ui.menu.Button(this.tr("Remove"));
                bt.addListenerOnce("execute", this.__onRemoveUser, this);
                menu.add(bt);
            }
        },

        __onNewUser : function(ev) {
            var d = new ncms.usr.UserNewDlg();
            d.addListenerOnce("completed", function(ev) {
                d.close();
                this.reload();
            }, this);
            d.show();
        },

        __onRemoveUser : function(ev) {
            var user = this.getSelectedUser();
            if (user == null) {
                return;
            }
            ncms.Application.confirm(
                    this.tr("Are you sure to remove user: \"%1\"?", user["name"]),
                    function(yes) {
                        if (!yes) return;
                        var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("security.user", {name : user["name"]}), "DELETE");
                        req.send(function(resp) {
                            this.reload();
                        }, this);
                    }, this);
        }

    },

    destruct : function() {
        this.__sf = null;
        this.__table = null;
    }
});