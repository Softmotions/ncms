/**
 *
 * TODO: REMOVE! ONLY FOR TESTS!
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 *
 * @asset(ncms/icon/16/misc/cross-script.png)
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
                [["mediaWiki"], {type: "mediaWiki"}],
                [["mediaWiki with help"], {type: "mediaWiki", helpSite: "http://nsu.ru/"}],
                [["mediaWiki with additional btn"], {type: "mediaWiki", additionalBtn : true}],
                [["mediaWiki with additional btn (with prompt)"], {type: "mediaWiki", additionalBtn : true, prompt : true}],
                [["mediaWiki exclude H1"], {type: "mediaWiki", exclude: "H1"}],
                [["mediaWiki exclude ABTN"], {type: "mediaWiki", exclude: "ABTN"}],
                [["mediaWiki exclude ABTNP"], {type: "mediaWiki", exclude: "ABTNP"}],
                [["markdown"], {type: "markdown"}],
                [["markdown with help"], {type: "markdown", helpSite: "http://nsu.ru/"}],
                [["markdown with additional btn"], {type: "markdown", additionalBtn : true}],
                [["markdown with additional btn (with prompt)"], {type: "markdown", additionalBtn : true, prompt : true}],
                [["markdown exclude H1"], {type: "markdown", exclude: "H1"}],
                [["markdown exclude ABTN"], {type: "markdown", exclude: "ABTN"}],
                [["markdown exclude ABTNP"], {type: "markdown", exclude: "ABTNP"}]
            ]
        });

        var table = new sm.table.Table(tm, tm.getCustom());
        this._add(table);

        var eclazz = "ncms.editor.wiki.WikiEditorTest";
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.editor.wiki.WikiEditorTest();
        }, null, this);

        table.getSelectionModel().addListener("changeSelection", function(ev){
            var data = table.getSelectedRowData();
            var title = table.getTableModel().getValue(0, table.getSelectionModel().getAnchorSelectionIndex());
            if (data == null) {
                app.showDefaultWSA();
                return;
            }

            var we = app.getWSA(eclazz);
            qx.log.Logger.debug(we.validate());
            we.setOptions(qx.lang.Object.mergeWith(data, {title: title}));
            app.showWSA(eclazz);
        }, this);
    },

    members : {
    },

    destruct : function() {
    }
});