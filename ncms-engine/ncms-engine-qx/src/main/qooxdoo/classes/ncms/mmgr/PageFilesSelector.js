/**
 * Files selector dedicated to the specific page.
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
        this.base(arguments, !!opts["allowModify"], null, opts);
        this.setItem({
            status : 1,
            path : path
        });
    },

    members : {

    },

    destruct : function() {
        //this._disposeObjects("__field_name");
    }
});