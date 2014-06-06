/**
 *
 * TODO: REMOVE! ONLY FOR TESTS!
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.editor.wiki.WikiNav", {
    extend : qx.ui.core.Widget,

    events : {
    },

    properties : {
    },

    construct : function() {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());

        var tm = new sm.model.JsonTableModel();
        tm.setJsonData({
            "title" : "",
            "columns" : [
                {
                    "title" : "Id",
                    "id" : "id",
                    "width" : "1*"
                }
            ],
            "items" : [
                [["mediaWiki"], {id: "mediaWiki"}],
                [["markdown"], {id: "markdown"}]
            ]
        });

        var table = new sm.table.Table(tm, tm.getCustom());
        this._add(table);

        var eclazz = "ncms.editor.wiki.WikiEditor";
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.editor.wiki.WikiEditor();
        }, null, this);

        table.getSelectionModel().addListener("changeSelection", function(ev){
            var data = table.getSelectedRowData();
            if (data == null) {
                app.showDefaultWSA();
                return;
            }
            app.getWSA(eclazz).setType(data["id"]);
            app.showWSA(eclazz);
        }, this);
    },

    members : {
    },

    destruct : function() {
    }
});