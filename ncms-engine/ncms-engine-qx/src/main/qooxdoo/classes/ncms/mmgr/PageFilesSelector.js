/**
 * Files selector dedicated to the specific page.
 *
 * @asset(ncms/icon/16/misc/document-import.png)
 */
qx.Class.define("ncms.mmgr.PageFilesSelector", {
    extend: ncms.mmgr.MediaFilesSelector,

    /**
     * <code>
     *  {
     *      allowModify : true|false,
     *      allowMove : true|false,
     *      allowSubfoldersView : true|fa;se
     *  }
     * </code>
     *
     * @param pageId {Number} The page GUI
     * @param opts {Object?} ncms.mmgr.MediaFilesSelector options.
     */
    construct: function (pageId, opts) {
        qx.core.Assert.assertNumber(pageId, "Page ID is not a number");
        opts = opts || {};
        var path = ncms.Utils.getPageLocalFolders(pageId);
        if (opts["allowMove"] === undefined) {
            opts["allowMove"] = false;
        }
        if (opts["allowSubfoldersView"] === undefined) {
            opts["allowSubfoldersView"] = false;
        }
        var item = {
            status: 1,
            path: path
        };
        var constViewSpec = this._resolveViewSpec(item);
        constViewSpec["inpages"] = true;
        this.base(arguments, !!opts["allowModify"], constViewSpec, opts);
        this.setItem(item);
    },

    members: {
    },

    destruct: function () {
        //this._disposeObjects("__field_name");
    }
});