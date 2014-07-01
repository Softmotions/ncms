/**
 * Files selector dedicated to the specific page.
 *
 * @asset(ncms/icon/16/misc/document-import.png)
 */
qx.Class.define("ncms.mmgr.PageFilesSelector", {
    extend : ncms.mmgr.MediaFilesSelector,

    properties : {
    },

    /**
     * @param pageGuid {String} The page GUI
     * @param opts {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct : function(pageGuid, opts) {
        if (typeof pageGuid !== "string" || pageGuid.length != 32) {
            throw new Error("Invalid page guid: " + pageGuid);
        }
        opts = opts || {};
        var path = ["pages"];
        for (var i = 0; i < 32; i += 4) {
            path.push(pageGuid.substring(i, i + 4));
        }
        if (opts["allowMove"] === undefined) {
            opts["allowMove"] = false;
        }
        if (opts["allowSubfoldersView"] === undefined) {
            opts["allowSubfoldersView"] = false;
        }
        this.base(arguments, !!opts["allowModify"], null, opts);
        this.setItem({
            status : 1,
            path : path
        });
    },

    members : {

        _setupToolbarEditDelegate : function(part) {
            var bt = new qx.ui.toolbar.Button(null, "ncms/icon/16/misc/document-import.png")
                    .set({"appearance" : "toolbar-table-button"});
            bt.setToolTipText(this.tr("Import from media repository"));
            bt.addListener("execute", this.__importFile, this);
            part.add(bt);
        },

        _setupContextMenuDelegate : function(menu) {
            menu.add(new qx.ui.menu.Separator());
            var bt = new qx.ui.menu.Button(this.tr("Import from media repository"));
            bt.addListenerOnce("execute", this.__importFile, this);
            menu.add(bt);
        },

        __importFile : function() {
            var dlg = new ncms.mmgr.MediaSelectFileDlg(true, this.tr("Import from media repository"));
            dlg.addListener("completed", function(ev) {
                var files = ev.getData();
                //data=[{"id":9,"name":"dodge-to.txt","folder":"/",
                // "content_type":"text/plain; charset=UTF-8",
                // "owner":"admin","owner_fullName":"Антон Адаманский",
                // "content_length":933,"description":"sdsds","tags":null},]
                var paths = [];
                files.forEach(function(f) {
                    paths.push(f["folder"] + f["name"]);
                });
                var target = this.getItem()["path"];
                var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("media.copy-batch", target), "PUT");
                req.setRequestContentType("application/json");
                req.setData(JSON.stringify(paths));
                req.send(function(resp) {
                    dlg.close();
                    this.reload();
                }, this);
            }, this);
            dlg.open();
        }
    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});