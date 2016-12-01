/**
 * @asset(ncms/icon/16/help/help.png)
 * @asset(ncms/icon/16/misc/door_in.png)
 */
qx.Class.define("ncms.Toolbar", {
    extend: qx.ui.toolbar.ToolBar,

    construct: function () {
        this.base(arguments);
        this.set({overflowHandling: false});
        this._init();
        var app = ncms.Application.INSTANCE;
        app.addListenerOnce("guiInitialized", this.__guiInitialized, this);
        app.addListener("workspaceActivated", this.__workspaceActivated, this);
    },

    properties: {
        appearance: {
            refine: true,
            init: "ncms-main-toolbar"
        }
    },

    members: {

        __mainPart: null,

        __rightPart: null,

        __spacer: null,

        _init: function () {
            var apps = ncms.Application.APP_STATE;
            this.__mainPart = new qx.ui.toolbar.Part().set({appearance: "ncms-main-toolbar/part"});
            this.add(this.__mainPart);

            this.__spacer = new qx.ui.core.Spacer();
            this.add(this.__spacer, {flex: 1});
            this.__rightPart = new qx.ui.toolbar.Part();
            this.add(this.__rightPart);


            if (apps.getHelpSite()) {
                var helpButton = new qx.ui.toolbar.Button(this.tr("Help"),
                    "ncms/icon/16/help/help.png");
                helpButton.addListener("execute", function () {
                    qx.bom.Window.open(apps.getHelpSite(), "NCMS:Help");
                });
                helpButton.setToolTipText(this.tr("Help"));
                this.__rightPart.add(helpButton);
            }
            var logoff = new qx.ui.toolbar.Button(this.tr("Logout") + " (" + apps.getUserLogin() + ")",
                "ncms/icon/16/misc/door_in.png");
            logoff.setToolTipText(this.tr("Logout"));
            logoff.addListener("execute", function () {
                ncms.Application.logout();
            }, this);
            this.__rightPart.add(logoff);
            this.setPaddingRight(10);
        },

        __guiInitialized: function () {
            var req = ncms.Application.request("nav.selectors");
            req.setParameter("qxLocale", qx.bom.client.Locale.getLocale());
            req.send(function (resp) {
                var mb = null; //Menu button
                var nitems = resp.getContent();
                //Sample data: [...,{"qxClass":"ncms.mtt.MttNav","label":"MTT","extra":true,"args":[]}]

                // Main menu
                var rg = new qx.ui.form.RadioGroup();
                nitems.filter(function (el) {
                    return !el.extra;
                }).forEach(function (ni) {
                    var tb = new qx.ui.form.ToggleButton(ni["label"], ni["qxIcon"]);
                    tb.setAppearance("toolbar-button");
                    tb.setUserData("wsSpec", ni);
                    rg.add(tb);
                    this.__mainPart.add(tb);
                }, this);
                rg.addListener("changeSelection", function (ev) {
                    var w = ev.getData()[0];
                    if (w) {
                        if (mb) {
                            mb.removeState("checked");
                            mb.setLabel(this.tr("Tools"));
                        }
                        this.__activateWorkspace(w.getUserData("wsSpec"));
                    }
                }, this);

                // Extra tools
                var titems = nitems.filter(function (el) {
                    return el.extra;
                });
                if (titems.length) {
                    var menu = new qx.ui.menu.Menu();
                    mb = new qx.ui.toolbar.MenuButton(this.tr("Tools"));
                    rg.setAllowEmptySelection(true);
                    mb.setMenu(menu);
                    mb.setShowArrow(true);
                    this.__mainPart.add(mb);
                    titems.forEach(function (ni) {
                        var bt = new qx.ui.menu.Button(ni["label"], ni["qxIcon"]);
                        bt.setUserData("wsSpec", ni);
                        bt.addListener("execute", function (ev) {
                            rg.resetSelection();
                            mb.addState("checked");
                            mb.setLabel(ni["label"]);
                            this.__activateWorkspace(ev.getTarget().getUserData("wsSpec"));
                        }, this);
                        menu.add(bt);
                    }, this);
                }

                ncms.Application.registerWorkspaces(nitems);
                if (nitems.length > 0) {
                    this.__activateWorkspace(nitems[0]);
                }
            }, this);
        },

        __activateWorkspace: function (wsSpec) {
            ncms.Application.activateWorkspace(wsSpec);
        },

        __workspaceActivated: function (evt) {
        }
    },

    destruct: function () {
        this.__mainPart = null;
        this.__rightPart = null;
        this.__spacer = null;
    }
});