/**
 * Actions repository
 */
qx.Class.define("ncms.Actions", {
    extend : sm.conn.Actions,

    construct : function() {
        this.base(arguments);
        this._testPrefix = "http://localhost:8080";
        this._resourceManager = qx.util.ResourceManager.getInstance();

        //Starting GUI state
        this._action("app.state", "/ncms/rs/adm/ws/state");

        //Logout
        this._action("app.logout", "/ncms/rs/adm/ws/logout");

        //Workspace components
        this._action("nav.selectors", "/ncms/rs/adm/ui/widgets/navigation-selectors");

        //Asm selector
        this._action("asms.select", "/ncms/rs/adm/asms/select");
        this._action("asms.select.count", "/ncms/rs/adm/asms/select/count");

        //Asm editor

        //GET/PUT/DELETE assembly
        this._action("asms", "/ncms/rs/adm/asms/{id}");

        //PUT create new assembly with specified name
        this._action("asms.new", "/ncms/rs/adm/asms/new/{name}");

        //PUT rename existing assembly
        this._action("asms.rename", "/ncms/rs/adm/asms/rename/{id}/{name}");

        //PUT Assembly parents
        this._action("asms.parents", "/ncms/rs/adm/asms/{id}/parents");

        //MediaRS
        //GET files only /ncms/rs/media/files/<path to folder>
        this._action("media.files", "/ncms/rs/media/files");
        //GET sub-folders only
        this._action("media.folders", "/ncms/rs/media/folders");
        //GET files + sub-folders
        this._action("media.all", "/ncms/rs/media/all");
        //GET select
        this._action("media.select", "/ncms/rs/media/select");
        //GET select count
        this._action("media.select.count", "/ncms/rs/media/select/count");

    },

    members : {

        _testPrefix : null,

        _resourceManager : null,

        _action : function(id, path) {
            if (qx.core.Environment.get("ncms.testing.urls")) {
                this._addAction(id, this._resourceManager.toUri(this._testPrefix + path));
            } else {
                this._addAction(id, this._resourceManager.toUri(path));
            }
        }
    }
});