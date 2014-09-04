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
        this._action("app.state", "/rs/adm/ws/state");

        //Logout
        this._action("app.logout", "/rs/adm/ws/logout");

        //Workspace components
        this._action("nav.selectors", "/rs/adm/ui/widgets/navigation-selectors");

        //Asm selector
        this._action("asms.select", "/rs/adm/asms/select");
        this._action("asms.select.count", "/rs/adm/asms/select/count");

        //Asm editor

        //GET/DELETE assembly
        this._action("asms", "/rs/adm/asms/{id}");

        //GET Get basic assembly info
        // name: assembly name
        this._action("asms.basic", "/rs/adm/asms/basic/{name}");

        //PUT create new assembly with specified name
        this._action("asms.new", "/rs/adm/asms/new/{name}");

        //PUT rename existing assembly
        this._action("asms.rename", "/rs/adm/asms/rename/{id}/{name}");

        //PUT Assembly parents
        this._action("asms.parents", "/rs/adm/asms/{id}/parents");

        //PUT Assembly core
        //Payload: JSON core spec
        this._action("asms.core", "/rs/adm/asms/{id}/core");

        //GET|DELETE Assembly attribute
        // id: assembly id
        // name: attribute name
        this._action("asms.attribute", "/rs/adm/asms/{id}/attribute/{name}");

        //PUT Assembly attribute values/options
        // id: assembly id
        this._action("asms.attributes", "/rs/adm/asms/{id}/attributes");

        //PUT Save assembly
        // id: assembly id
        this._action("asms.props", "/rs/adm/asms/{id}/props");

        //PUT Exchange attributes ordinals
        this._action("asms.attributes.exchange", "/rs/adm/asms/attributes/reorder/{ordinal1}/{ordinal2}");

        //==================== MediaRS

        //GET files only /rs/media/files/<path to folder>
        this._action("media.files", "/rs/media/files");

        //GET sub-folders only
        this._action("media.folders", "/rs/media/folders");

        //GET files + sub-folders
        this._action("media.all", "/rs/media/all");

        //GET select
        this._action("media.select", "/rs/media/select");

        //GET select count
        this._action("media.select.count", "/rs/media/select/count");

        //PUT create new folder with specifed name
        this._action("media.folder.put", "/rs/media/folder");

        //PUT /rs/media/rename/<path to folder>
        this._action("media.move", "/rs/media/move");

        //PUT  /rs/media/copy-batch/<path to folde>
        // Payload: JSON array of file paths to copy into {target} dir
        this._action("media.copy-batch", "/rs/media/copy-batch");

        //DELETE media file/folder
        this._action("media.delete", "/rs/media/delete");

        //DELETE media file/folder batch mode
        this._action("media.delete-batch", "/rs/media/delete-batch");

        //PUT Upload media file /rs/media/file/<path to file>
        this._action("media.upload", "/rs/media/file");

        //GET Download file /rs/media/file/<path to file>
        this._action("media.file", "/rs/media/file");

        //GET Download file by id
        this._action("media.fileid", "/rs/media/fileid/{id}");

        //GET Thumbnail of image media
        this._action("media.thumbnail2", "/rs/media/thumbnail2/{id}");

        //GET Thumbnail of image media
        ///rs/media/thumbnail/<path to file>
        this._action("media.thumbnail", "/rs/media/thumbnail");

        //GET/POST Save extra file meta fields
        this._action("media.meta", "/rs/media/meta/{id}");

        //POST Save extra file meta fields (by path)
        this._action("media.path.meta", "/rs/media/meta/path");

        //GET media-entity path by specified ID
        this._action("media.path", "/rs/media/path/{id}");

        //================== NcmsSecurityRS

        // GET/PUT/DELETE
        this._action("security.user", "/rs/adm/security/user/{name}");

        //GET selects users count
        this._action("security.users.count", "/rs/adm/security/users/count");

        //GET selects users
        this._action("security.users", "/rs/adm/security/users");

        // PUT/DELETE role/group for user
        this._action("security.user.role", "/rs/adm/security/user/{name}/role/{role}");
        this._action("security.user.group", "/rs/adm/security/user/{name}/group/{group}");

        //GET selects roles/groups
        this._action("security.roles", "/rs/adm/security/roles");
        this._action("security.groups", "/rs/adm/security/groups");

        //================== PagesRS

        //GET Pages tree layer
        ///rs/adm/pages/layer/{path:.*}
        this._action("pages.layer", "/rs/adm/pages/layer");

        //PUT Create new page
        // {
        //  name : {String} Page name
        //  parent : {Number?null} Parent page ID
        //  type: {String} Page type
        // }
        this._action("pages.new", "/rs/adm/pages/new");

        //DELETE The specified page
        this._action("pages.delete", "/rs/adm/pages/{id}");

        //GET Get page data for page-info pane.
        this._action("pages.info", "/rs/adm/pages/info/{id}");

        //GET|PUT page data for page-edit pane.
        this._action("pages.edit", "/rs/adm/pages/edit/{id}");

        //PUT update basic page info (rename/change type)
        // {
        //  name : {String} Page name
        //  id :   {Number} Parent ID
        //  type: {String} Page type
        // }
        this._action("pages.update.basic", "/rs/adm/pages/update/basic");

        //PUT set page owner
        // id: Page ID
        // owner: Owner user ID
        this._action("pages.owner", "/rs/adm/pages/owner/{id}/{owner}");

        //PUT page set template
        // {id}: Page ID
        // {templateId} Template ID
        this._action("pages.set.template", "/rs/adm/pages/template/{id}/{templateId}");

        //PUT Publish/Unpublish page
        this._action("pages.publish", "/rs/adm/pages/publish/{id}");
        this._action("pages.unpublish", "/rs/adm/pages/unpublish/{id}");

        // GET page acl
        // pid: page id
        this._action("pages.acl", "/rs/adm/pages/acl/{pid}");

        // PUT add new user to acl
        // DELETE delete user from acls for this page
        // POST update user rights
        // pid: page id
        // user: user login
        this._action("pages.acl.user", "/rs/adm/pages/acl/{pid}/{user}");

        //GET JSON Object contains full label-path and id-path to the specified page
        // {id}: Page ID
        this._action("pages.path", "/rs/adm/pages/path/{id}");

        // GET search page
        this._action("pages.search", "/rs/adm/pages/search");
        this._action("pages.search.count", "/rs/adm/pages/search/count");

        // GET check user access
        this._action("pages.check.rights", "/rs/adm/pages/check/{pid}/{rights}");

        // PUT move page
        // Data:
        // {
        //   src : {Number} Source page ID,
        //   tgt : {Number} Target page ID
        // }
        this._action("pages.move", "/rs/adm/pages/move");

        //Page preview url
        this._action("pages.preview", "/adm/asm/{id}?preview=true");
        this._action("pages.preview.frame", "/adm/asm/{id}?preview=true&preview_frame=true");

        //PUT/DELETE page into user
        // {collection}: Collection name
        // {id}: Page ID
        this._action("pages.collection", "/rs/adm/pages/collection/{collection}/{id}");

        //PUT Put single page element into collection and delete others
        this._action("pages.single", "/rs/adm/pages/single/{collection}/{id}");

        //GET Single page element from named collection
        this._action("pages.single.get", "/rs/adm/pages/single/{collection}");

        //================== UserEnvRS

        /**
         * PUT Single user env value
         * {type} Env type
         * Data: env value
         */
        this._action("user.env.single", "/rs/adm/user/env/single/{type}");

        /**
         * GET|PUT|DELETE Add entry to the envset of {type}
         * Data: env value used for PUT|DELETE
         */
        this._action("user.env.set", "/rs/adm/user/env/set/{type}");

        /**
         * DELETE Delete all elements in envset of {type}
         */
        this._action("user.env.clear", "/rs/adm/user/env/clear/{type}");

    },

    members : {

        _testPrefix : null,

        _resourceManager : null,

        _action : function(id, path) {
            path = "/ncms" + path; //todo make it configurable
            if (qx.core.Environment.get("ncms.testing.urls")) {
                this._addAction(id, this._resourceManager.toUri(this._testPrefix + path));
            } else {
                this._addAction(id, this._resourceManager.toUri(path));
            }
        }
    }
});
