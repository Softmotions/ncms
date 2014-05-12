/*
 * Copyright (c) 2011. Softmotions Ltd. (softmotions.com)
 * All Rights Reserved.
 */

qx.Class.define("ncms.usr.editor.UserRolesTable", {
    extend : sm.table.ToolbarLocalTable,

    construct : function() {
    	alert("UserRolesTable started");
        this.base(arguments);
        this._reload([]);
    },

    members :
    {
        __user : null,

        setUser : function(login) {
            if (login == null) {
                this._reload([]);
                this.__user = login;
                return;
            }
            var req = new sm.io.Request(sm.cms.Application.ACT.getUrl("security.user"), "GET", "application/json");
            req.setParameter("name", login, false);
            req.send(function(resp) {
                var roles = resp.getContent();
                this._reload(roles);
                this.__user = login;
            }, this);
        },

        ///////////////////////////////////////////////////////////////////////////
        //                         sm.table.ToolbarTable                         //
        ///////////////////////////////////////////////////////////////////////////

        // table.addListener("dataEdited", function(ev) {
        // edata.col, edata.row, edata.value

        //overriden
        _createToolbarItems : function(toolbar) {
            return toolbar;
        },

        //overriden
        _createTable : function(tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.set({statusBarVisible : false});

            var rr = new sm.table.renderer.CustomRowRenderer();
            rr.setBgColorInterceptor(qx.lang.Function.bind(function(rowInfo) {
                if (rowInfo.rowData[3] && rowInfo.rowData[2]) {
                    return "#FFFF99";
                }
                return "white";
            }, this));
            table.setDataRowRenderer(rr);
            
            return table;
        },

        //overriden
        _setJsonTableData : function(tm, items) {
            var data = {
                "title" : "",
                "columns" : [
                    {
                        "title" : this.tr("Role name").toString(),
                        "id" : "name",
                        "width" : "2*"
                    },
                    {
                        "title" : this.tr("Description").toString(),
                        "id" : "description",
                        "width" : "1*"
                    }

                ],
                "items" : items ? items : []
            };
            tm.setJsonData(data);
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});

