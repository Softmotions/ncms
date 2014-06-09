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

        var eclazz = "ncms.editor.wiki.WikiEditor";
        var app = ncms.Application.INSTANCE;
        app.registerWSA(eclazz, function() {
            return new ncms.editor.wiki.WikiEditor();
        }, null, this);

        table.getSelectionModel().addListener("changeSelection", function(ev){
            var data = table.getSelectedRowData();
            var title = table.getTableModel().getValue(0, table.getSelectionModel().getAnchorSelectionIndex());
            if (data == null) {
                app.showDefaultWSA();
                return;
            }

            var we = app.getWSA(eclazz);
            we.setType(data["type"]);
            we.setHelpSite(data["helpSite"] ? data["helpSite"] : null);
            if (data["additionalBtn"]) {
                var bid = "ABTN" + (data["prompt"] ? "P" : "");
                if (!we.hasToolbarControl(bid)) {
                    we.addToolbarControl({
                        "id" : bid,
                        "tooltipText" : title,
                        "icon" : "ncms/icon/16/misc/cross-script.png",
                        "prompt" : data["prompt"] ? function(cb, editor, stext) {
                            cb.call(this, stext ? stext : prompt());
                        } : null,
                        "insertMediaWiki" : function(cb, data) {
                            cb.call(this, "\n--testW-- " + data + " --testW--\n")
                        },
                        "insertMarkdown" : function(cb, data) {
                            cb.call(this, "\n--testM-- " + data + " --testM--\n")
                        }
                    });
                }
            }
            we.resetToolbarControls();
            if (data["exclude"]) {
                we.excludeToolbarControl(data["exclude"]);
            }

            app.showWSA(eclazz);
        }, this);
    },

    members : {
    },

    destruct : function() {
    }
});