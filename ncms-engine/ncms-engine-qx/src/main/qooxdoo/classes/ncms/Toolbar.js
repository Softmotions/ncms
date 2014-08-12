/**
 * @asset(ncms/icon/16/help/help.png)
 * @asset(ncms/icon/16/misc/door_in.png)
 */
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

    properties : {
        appearance : {
            refine : true,
            init : "ncms-main-toolbar"
        }
    },

    members : {

        __mainPart : null,

        __rightPart : null,

        __spacer : null,

        _init : function() {
            var apps = ncms.Application.APP_STATE;
            this.__mainPart = new qx.ui.toolbar.Part().set({appearance : "ncms-main-toolbar/part"});
            this.add(this.__mainPart);

            this.__spacer = new qx.ui.core.Spacer();
            this.add(this.__spacer, {flex : 1});
            this.__rightPart = new qx.ui.toolbar.Part();
            this.add(this.__rightPart);


            if (apps.getHelpSite()) {
                var helpButton = new qx.ui.toolbar.Button(this.tr("Help"),
                        "ncms/icon/16/help/help.png");
                helpButton.addListener("execute", function() {
                    qx.bom.Window.open(apps.getHelpSite());
                });
                helpButton.setToolTipText(this.tr("Help"));
                this.__rightPart.add(helpButton);
            }
            var logoff = new qx.ui.toolbar.Button(this.tr("Logout") + " (" + apps.getUserLogin() + ")",
                    "ncms/icon/16/misc/door_in.png");
            logoff.setToolTipText(this.tr("Logout"));
            logoff.addListener("execute", function() {
                ncms.Application.logout();
            }, this);
            this.__rightPart.add(logoff);

            this.setPaddingRight(10);
        },

        __guiInitialized : function() {
            var req = new sm.io.Request(ncms.Application.ACT.getUrl("nav.selectors"), "GET", "application/json");
            req.send(function(resp) {
                var nitems = resp.getContent();
                var rg = new qx.ui.form.RadioGroup();
                nitems.forEach(function(ni) {
                    var b = new qx.ui.form.ToggleButton(ni["label"], ni["qxIcon"]);
                    b.setAppearance("toolbar-button");
                    b.setUserData("wsSpec", ni);
                    rg.add(b);
                    this.__mainPart.add(b);
                }, this);
                rg.addListener("changeSelection", function(ev) {
                    var w = ev.getData()[0];
                    if (w) {
                        this.__activateWorkspace(w.getUserData("wsSpec"));
                    }
                }, this);

                ncms.Application.registerWorkspaces(nitems);
                if (nitems.length > 0) {
                    this.__activateWorkspace(nitems[0]);
                }
            }, this);
        },

        __activateWorkspace : function(wsSpec) {
            ncms.Application.activateWorkspace(wsSpec);
        },

        __workspaceActivated : function(evt) {
        }
    },

    destruct : function() {
        this.__mainPart = null;
        this.__rightPart = null;
        this.__spacer = null;
    }
});