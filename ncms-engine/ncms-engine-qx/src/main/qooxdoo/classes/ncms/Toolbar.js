qx.Class.define("ncms.Toolbar", {
    extend : qx.ui.toolbar.ToolBar,

    construct : function() {
        this.base(arguments);
        this.set({overflowHandling : false});
        this._init();
        var app = ncms.Application.INSTANCE;
        app.addListenerOnce("guiInitialized", this.__guiInitialized, this);
        app.addListener("workspaceActivated", this.__workspaceActivated, this);
    },

    members : {

        __workspaceMB : null,

        _init : function() {
            var apps = ncms.Application.APP_STATE;
            this.__workspaceMB = new qx.ui.toolbar.MenuButton();
            this.add(this.__workspaceMB);

            this.add(new qx.ui.core.Spacer, {flex : 1});

            if (apps.getHelpSite()) {
                var helpButton = new qx.ui.toolbar.Button(this.tr("Help"), "sm/icons/misc/help16.png");
                helpButton.addListener("execute", function() {
                    qx.bom.Window.open(apps.getHelpSite());
                });
                helpButton.setToolTipText(this.tr("Help"));
                this.add(helpButton);
            }
            var logoff = new qx.ui.toolbar.Button(this.tr("Logout") + " (" + apps.getUserLogin() + ")",
                    "sm/icons/misc/door_in16.png");
            logoff.setToolTipText(this.tr("Logout"));
            logoff.addListener("execute", function() {
                if (window.confirm(this.tr("Do you really want to logout?"))) {
                    window.location.href = ncms.Application.ACT.getUrl("app.logout");
                }
            }, this);
            this.add(logoff);
            this.setPaddingRight(10);
        },

        __guiInitialized : function() {
            var req = new sm.io.Request(ncms.Application.ACT.getUrl("nav.selectors"), "GET", "application/json");
            req.send(function(resp) {
                var menu = new qx.ui.menu.Menu();
                var nitems = resp.getContent();
                nitems.forEach(function(ni) {
                    var mb = new qx.ui.menu.Button(ni["label"], ni["qxIcon"]);
                    mb.addListener("execute", this.__onMbActivated, this);
                    mb.setUserData("wsSpec", ni);
                    menu.add(mb);
                    this.__workspaceMB.setMenu(menu);
                }, this);
                this.__workspaceMB.setMenu(menu);
                ncms.Application.registerWorkspaces(nitems);
                if (nitems.length > 0) {
                    this.__activateWorkspace(nitems[0]);
                }
            }, this);
        },

        __onMbActivated : function(ev) {
            this.__activateWorkspace(ev.getTarget().getUserData("wsSpec"));
        },

        __activateWorkspace : function(wsSpec) {
            ncms.Application.activateWorkspace(wsSpec);
        },

        __workspaceActivated : function(evt) {
            var wsSpec = evt.getData();
            if (wsSpec && wsSpec["label"] != null) {
                this.__workspaceMB.setLabel(wsSpec["label"]);
            }
        }
    },

    destruct : function() {
        this.__workspaceMB = null;
        //this._disposeObjects("__field_name");                                
    }
});