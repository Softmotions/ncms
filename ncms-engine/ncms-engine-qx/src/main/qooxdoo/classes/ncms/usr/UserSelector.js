/**
 * Selector of users
 * which include search text box and table of users
 */
qx.Class.define("ncms.usr.UserSelector", {
    extend: qx.ui.core.Widget,

    events: {

        /**
         * Event fired if user was selected/deselected
         *
         * DATA: {
         *        "name" : {String} User name,
         *        "fullName" : {String} Full user name,
         *       };
         * or null if selection cleared
         */
        "userSelected": "qx.event.type.Data"
    },

    properties: {

        constViewSpec: {
            check: "Object",
            nullable: true,
            apply: "__applyConstViewSpec"
        }
    },

    construct: function (constViewSpec, smodel) {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var sf = this.__sf = new sm.ui.form.SearchField();
        sf.addListener("clear", function () {
            this.__search(null);
        }, this);
        sf.addListener("input", function (ev) {
            this.__search(ev.getData());
        }, this);
        sf.addListener("keypress", function (ev) {
            if ("Down" === ev.getKeyIdentifier()) {
                this.__table.handleFocus();
            }
        }, this);
        this.addListener("appear", function () {
            sf.focus();
        });
        
        this.__table = new ncms.usr.UsersTable();
        if (smodel != null) {
            this.__table.setSelectionModel(smodel);
        }
        this.__table.getSelectionModel().addListener("changeSelection", function () {
            var user = this.getSelectedUser();
            this.fireDataEvent("userSelected", user ? user : null);
        }, this);
        
        this._add(this.__sf);
        this._add(this.__table, {flex: 1});
        this.setConstViewSpec(constViewSpec || null);
    },

    members: {
        /**
         * Search field
         * @type {sm.ui.form.SearchField}
         */
        __sf: null,

        /**
         * Users virtual table
         * @type {ncms.usr.UsersTable}
         */
        __table: null,


        setViewSpec: function (vspec) {
            this.__table.getTableModel().setViewSpec(this.__createViewSpec(vspec));
        },

        updateViewSpec: function (vspec) {
            this.__table.getTableModel().updateViewSpec(this.__createViewSpec(vspec));
        },

        reloadData: function (vspec) {
            this.reload(vspec);
        },

        reload: function (vspec) {
            this.__table.getTableModel().reloadData();
        },

        resetSelection: function () {
            this.__table.resetSelection();
        },

        getTable: function () {
            return this.__table;
        },

        getSelectedUserInd: function () {
            return this.__table.getSelectedUserInd();
        },

        getSelectedUser: function () {
            return this.__table.getSelectedUser();
        },

        getSelectedUsers: function () {
            return this.__table.getSelectedUsers();
        },

        cleanup: function () {
            this.__table.cleanup();
        },

        getSearchField: function () {
            return this.__sf;
        },

        __createViewSpec: function (vspec) {
            if (this.getConstViewSpec() == null) {
                return vspec;
            }
            var nspec = {};
            qx.Bootstrap.objectMergeWith(nspec, this.getConstViewSpec(), false);
            qx.Bootstrap.objectMergeWith(nspec, vspec, false);
            return nspec;
        },

        __search: function (val) {
            this.updateViewSpec({stext: val || ""});
        },

        __applyConstViewSpec: function () {
            this.__search();
        }
    },

    destruct: function () {
        this.__sf = null;
        this.__table = null;
    }
});
