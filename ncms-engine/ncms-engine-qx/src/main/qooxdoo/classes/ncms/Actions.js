/**
 * Actions repository
 */
qx.Class.define("ncms.Actions", {
    extend: sm.conn.Actions,

    construct: function (prefix) {

        function appRooRoot() {
            var pn = window.location.pathname;
            var ind = pn.indexOf("/adm/index.html");
            if (ind == -1) {
                ind = pn.indexOf("/adm/")
            }
            if (ind == -1) {
                qx.log.Logger.error("Failed to find app root. window.location.pathname=" + window.location.pathname);
                return null;
            }
            pn = pn.substring(0, ind);
            return pn;
        }

        this._prefix = (typeof prefix === "string") ? prefix : appRooRoot();

        this.base(arguments);

        qx.log.Logger.info("ACTIONS PREFIX: '" + this._prefix + "'");
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

        //GET
        this._action("asms", "/rs/adm/asms/get/{id}");

        //DELETE assembly
        this._action("asms.delete", "/rs/adm/asms/delete/{id}");

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

        //GET Public link by file by id + name
        this._action("media.public", "/rs/media/public/{id}/{name}");

        //GET Thumbnail of image media
        this._action("media.thumbnail2", "/rs/media/thumbnail2/{id}");

        //GET Thumbnail of image media
        ///rs/media/thumbnail/<path to file>
        this._action("media.thumbnail", "/rs/media/thumbnail");

        //GET/POST Save extra file meta fields
        this._action("media.meta", "/rs/media/meta/{id}");

        //GET Get file meta info by path
        this._action("media.meta.by.path", "/rs/media/path/meta/{path}");

        //POST Save extra file meta fields (by path)
        this._action("media.path.meta", "/rs/media/meta/path");

        //GET media-entity path by specified ID
        this._action("media.path", "/rs/media/path/{id}");

        //================== NcmsSecurityRS

        // GET settings
        this._action("security.settings", "/rs/adm/security/settings");

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

        //PUT Duplicate a page
        this._action("pages.clone", "/rs/adm/pages/clone");

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
        this._action("pages.check.rights", "/rs/adm/pages/rights/{pid}/{rights}");
        this._action("pages.rights", "/rs/adm/pages/rights/{pid}");

        // PUT move page
        // Data:
        // {
        //   src : {Number} Source page ID,
        //   tgt : {Number} Target page ID
        // }
        this._action("pages.move", "/rs/adm/pages/move");

        //Page preview url
        this._action("pages.preview", "/adm/asm/{id}?preview=1");
        this._action("pages.preview.frame", "/adm/asm/{id}?preview=1&preview_frame=1");

        //PUT/DELETE page into user
        // {collection}: Collection name
        // {id}: Page ID
        this._action("pages.collection", "/rs/adm/pages/collection/{collection}/{id}");

        //PUT Put single page element into collection and delete others
        this._action("pages.single", "/rs/adm/pages/single/{collection}/{id}");

        //GET Single page element from named collection
        this._action("pages.single.get", "/rs/adm/pages/single/{collection}");

        //GET The list of referers
        this._action("pages.referrers", "/rs/adm/pages/referrers/{guid}");

        //GET The count of referers
        this._action("pages.referrers.count", "/rs/adm/pages/referrers/count/{id}");

        //GET The list of pages that are referenced
        this._action("pages.referrers.to", "/rs/adm/pages/referrers/to/{id}");

        //GET The count of pages that are referenced
        this._action("pages.referrers.to.count", "/rs/adm/pages/referrers/to/count/{id}");

        //GET The list of referrer page's attributes
        this._action("pages.referrers.attributes", "/rs/adm/pages/referrers/attributes/{guid}/{asmid}");

        //GET The number of referrer page's attributes
        this._action("pages.referrers.attributes.count", "/rs/adm/pages/referrers/attributes/count/{guid}");

        //PUT Acquire lock on the specified page
        // {id}: Page ID
        this._action("pages.lock", "/rs/adm/pages/lock/{id}");

        //PUT Release lock on the specified page
        // {id}: Page ID
        this._action("pages.unlock", "/rs/adm/pages/unlock/{id}");

        //================== UserEnvRS

        // PUT Single user env value
        // {type} Env type
        // Data: env value
        this._action("user.env.single", "/rs/adm/user/env/single/{type}");


        // GET|PUT|DELETE Add entry to the envset of {type}
        // Data: env value used for PUT|DELETE
        this._action("user.env.set", "/rs/adm/user/env/set/{type}");


        // DELETE Delete all elements in envset of {type}
        this._action("user.env.clear", "/rs/adm/user/env/clear/{type}");


        //================== Attribute managers

        // PUT Sync asm attribute.
        // Data: {
        //   src: {Number} Page ID
        //   tgt: {Number} Target page ID
        //   attr: {String} Attribute name
        // }
        this._action("am.tree.sync", "/rs/adm/am/tree/sync");

        //================= MTT rules

        this._action("mtt.rules.select", "/rs/adm/mtt/rules/select");
        this._action("mtt.rules.select.count", "/rs/adm/mtt/rules/select/count");

        // PUT new mtt rule
        // {name} MTT rule name
        this._action("mtt.rules.new", "/rs/adm/mtt/rules/rule/{name}");

        // DELETE existing mtt rule
        // {id} MTT rule ID
        this._action("mtt.rules.delete", "/rs/adm/mtt/rules/rule/{id}");

        // PUT rename existing mtt rule
        // {id} MTT rule ID
        // {name} New rule name
        this._action("mtt.rules.rename", "/rs/adm/mtt/rules/rule/rename/{id}/{name}");

        // POST move rule up
        // {id} MTT rule ID
        this._action("mtt.rule.up", "/rs/adm/mtt/rules/rule/{id}/move/up");

        // POST move rule down
        // {id} MTT rule ID
        this._action("mtt.rule.down", "/rs/adm/mtt/rules/rule/{id}/move/down");

        // POST Enable rule
        // {id} MTT rule ID
        this._action("mtt.rule.enable", "/rs/adm/mtt/rules/rule/{id}/enable");

        // POST Disable rule
        // {id} MTT rule ID
        this._action("mtt.rule.disable", "/rs/adm/mtt/rules/rule/{id}/disable");

        //================ MTT filters

        // GET rule filters
        // {id} MTT rule ID
        this._action("mtt.filters.select", "/rs/adm/mtt/rules/rule/{id}/filters/select");

        // GET rule filters count
        // {id} MTT rule ID
        this._action("mtt.filters.select.count", "/rs/adm/mtt/rules/rule/{id}/filters/select/count");

        // PUT New MTT filter
        // {id} MTT rule ID
        // Data: {
        //   type: Filter type
        //   description: Filter description
        //   spec: Filter JSON specification
        // }
        this._action("mtt.filter.new", "/rs/adm/mtt/rules/rule/{id}/filter");

        // POST Update MTT filter
        // id:  MTT filter ID
        // Data: {
        //   type: Filter type
        //   description: Filter description
        //   spec: Filter JSON specification
        // }
        this._action("mtt.filter.update", "/rs/adm/mtt/rules/filter/{id}");

        // DELETE The filter specified by {id}
        // id: MTT filter ID
        this._action("mtt.filter.delete", "/rs/adm/mtt/rules/filter/{id}");

        //================ MTT actions

        // GET rule actions
        // {id} MTT rule ID
        this._action("mtt.actions.select", "/rs/adm/mtt/rules/rule/{id}/actions/select");

        // GET rule actions count
        // {id} MTT rule ID
        this._action("mtt.actions.select.count", "/rs/adm/mtt/rules/rule/{id}/actions/select/count");

        // PUT New MTT action
        // {id} MTT rule ID
        // Data: {
        //   type: Action type
        //   description: Action description
        //   spec: Action JSON specification
        // }
        this._action("mtt.action.new", "/rs/adm/mtt/rules/rule/{id}/action");

        // PUT New MTT action group
        // {id}     MTT rule ID
        this._action("mtt.action.group.new", "/rs/adm/mtt/rules/rule/{id}/group");


        // POST Update MTT action group weight
        // {id}     MTT action ID
        // {weight}  MTT action width value
        this._action("mtt.action.group.weight.update", "/rs/adm/mtt/rules/weight/{id}/{weight}");

        // PUT New MTT action composite
        // {id}     MTT rule ID
        this._action("mtt.action.composite.new", "/rs/adm/mtt/rules/rule/{id}/composite");

        // POST Update MTT action
        // {id}  MTT action ID
        // Data: {
        //   type: Action type
        //   description: Action description
        //   spec: Action JSON specification
        // }
        this._action("mtt.action.update", "/rs/adm/mtt/rules/action/{id}");

        // DELETE The action specified by {id}
        // {id} MTT action ID
        this._action("mtt.action.delete", "/rs/adm/mtt/rules/action/{id}");

        // POST move action up
        // {id}  MTT action ID
        this._action("mtt.action.up", "/rs/adm/mtt/rules/action/{id}/move/up");

        // POST move action down
        // {id}  MTT action ID
        this._action("mtt.action.down", "/rs/adm/mtt/rules/action/{id}/move/down");

        //================== Mtt tracking pixels

        this._action("mtt.tps.select", "/rs/adm/mtt/tp/select");
        this._action("mtt.tps.select.count", "/rs/adm/mtt/tp/select/count");

        // DELETE existing mtt tracking pixel
        // {id} Tp id
        this._action("mtt.tp.delete", "/rs/adm/mtt/tp/tp/{id}");

        // POST update existing mtt tracking pixel
        // {id} Tp id
        // Data: { tracking pixels JSON data }
        this._action("mtt.tp.update", "/rs/adm/mtt/tp/tp/{id}");

        // PUT new mtt tracking pixel
        // {name} Tp name
        this._action("mtt.tp.new", "/rs/adm/mtt/tp/tp/{name}");

        // POST Enable tracking pixel
        // {id} Tp id
        this._action("mtt.tp.enable", "/rs/adm/mtt/tp/tp/{id}/enable");

        // POST Disable tracking pixel
        // {id} Tp id
        this._action("mtt.tp.disable", "/rs/adm/mtt/tp/tp/{id}/disable");

        // PUT rename existing mtt tracking pixel
        // {id} Tp id
        // {name} New pixel name
        this._action("mtt.tp.rename", "/rs/adm/mtt/tp/tp/rename/{id}/{name}");

        // GET Tracking pixel info
        // {id} Tp id
        this._action("mtt.tp.get", "/rs/adm/mtt/tp/tp/{id}")
    },

    members: {

        _prefix: null,

        _testPrefix: null,

        _resourceManager: null,

        toUri: function (path) {
            path = this._prefix + path;
            if (qx.core.Environment.get("ncms.testing.urls")) {
                return this._resourceManager.toUri(this._testPrefix + path);
            } else {
                return this._resourceManager.toUri(path);
            }
        },

        _action: function (id, path) {
            this._addAction(id, this.toUri(path));
        }
    }
});
