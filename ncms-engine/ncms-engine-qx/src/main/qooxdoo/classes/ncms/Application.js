/**
 * Ncms Application
 *
 * @asset(icons/*)
 * @asset(sm/icons/misc/help16.png)
 * @asset(sm/icons/misc/door_in16.png)
 *
 * @use(ncms.asm.NavAssemblies)
 * @use(ncms.mmgr.NavMediaManager)
 * @use(ncms.pgs.NavPages)
 */
qx.Class.define("ncms.Application", {
    extend : qx.application.Standalone,
    include : [qx.locale.MTranslation],
    statics : {

        INSTANCE : null,
        APP_STATE : null,
        ACT : null,


        ///////////////////////////////////////////////////////////
        //                         Alerts
        ///////////////////////////////////////////////////////////

        confirm : function(message, callback, context) {
            var root = qx.core.Init.getApplication().getRoot();
            (new dialog.Confirm({
                "message" : message,
                "callback" : callback || null,
                "context" : context || null,
                "blockerOpacity" : root.getBlockerOpacity(),
                "blockerColor" : root.getBlockerColor() || "transparent",
                "yesButtonLabel" : root.tr("Yes"),
                "noButtonLabel" : root.tr("No")
            })).show();
        },

        alert : function(message, callback, context) {
            var root = qx.core.Init.getApplication().getRoot();
            (new dialog.Alert({
                "message" : message,
                "callback" : callback || null,
                "context" : context || null,
                "blockerOpacity" : root.getBlockerOpacity(),
                "blockerColor" : root.getBlockerColor() || "transparent"
            })).show();
        },

        warning : function(message, callback, context) {
            var root = qx.core.Init.getApplication().getRoot();
            (new dialog.Alert({
                "message" : message,
                "callback" : callback || null,
                "context" : context || null,
                "blockerOpacity" : root.getBlockerOpacity(),
                "blockerColor" : root.getBlockerColor() || "transparent",
                "image" : "icon/48/status/dialog-warning.png"
            })).show();
        },

        prompt : function(message, callback, context) {
            var root = qx.core.Init.getApplication().getRoot();
            (new dialog.Prompt({
                "message" : message,
                "callback" : callback || null,
                "context" : context || null,
                "blockerOpacity" : root.getBlockerOpacity(),
                "blockerColor" : root.getBlockerColor() || "transparent"
            })).show();
        },

        getComponent : function(name) {
            return ncms.Application.INSTANCE.getComponent(name);
        },

        registerComponent : function(name, component) {
            return ncms.Application.INSTANCE.registerComponent(name, component);
        },

        getUserId : function() {
            return ncms.Application.APP_STATE.getUserId();
        },

        userHasRole : function(role) {
            return ncms.Application.APP_STATE.userHasRole(role);
        },

        userInRoles : function(roles) {
            return ncms.Application.APP_STATE.userInRoles(roles);
        },

        activateWorkspace : function(wsSpec) {
            return ncms.Application.INSTANCE.activateWorkspace(wsSpec);
        },

        registerWorkspaces : function(wsSpec) {
            return ncms.Application.INSTANCE.registerWorkspaces(wsSpec);
        }
    },


    events : {

        /**
         * Fired when main gui widget created and attached
         */
        "guiInitialized" : "qx.event.type.Event",


        /**
         * Fired if specifig admin GUI workspace activated
         * Data: {"qxClass":<workspace loader class>, "label":<workspace name>}
         * Example data: {"qxClass":"ncms.pgs.NavPages","label":"Страницы"}
         */
        "workspaceActivated" : "qx.event.type.Data"
    },

    members : {

        /**
         * Refs to gui components
         */
        __components : null,

        __header : null,

        __nav : null,

        main : function() {
            // Enable logging in debug variant
            if (qx.core.Environment.get("ncms.debug")) {
                qx.log.appender.Native;
                qx.log.appender.Console;
            }
            // Call super class
            this.base(arguments);
            this.__components = {};
            this.__bootstrap();

            this.getRoot().setBlockerColor("black");
            this.getRoot().setBlockerOpacity(0.5);

            var comp = new qx.ui.container.Composite(new qx.ui.layout.Dock().set({separatorY : "separator-vertical"}));
            this.getRoot().add(comp, {edge : 0});

            //Toolbar
            var toolbar = new ncms.Toolbar();
            comp.add(toolbar, {edge : "north"});

            var hsp = new qx.ui.splitpane.Pane();
            comp.add(hsp);

            //Left nav side
            var navStack = new sm.ui.cont.LazyStack();
            navStack.setWidth(250);
            hsp.add(navStack, 0);

            //Right nav side
            var rightStack = new sm.ui.cont.LazyStack();
            hsp.add(rightStack, 1);

            this.registerComponent("toolbar", toolbar);
            this.registerComponent("nav-stack", navStack);
            this.registerComponent("right-stack", rightStack);
            this.fireEvent("guiInitialized");
        },

        getComponent : function(name) {
            var val = this.__components[name];
            if (!val) {
                throw new Error("Unknown component: '" + name + "'");
            }
            return val;
        },

        registerComponent : function(name, component) {
            var val = this.__components[name];
            if (val) {
                throw new Error("Component with name: " + name + " already registered");
            }
            this.__components[name] = component;
        },

        activateWorkspace : function(wsSpec) {
            qx.log.Logger.info("Activate workspace: " + JSON.stringify(wsSpec));
            this.getComponent("nav-stack").showWidget(wsSpec["qxClass"]);
            this.fireDataEvent("workspaceActivated", wsSpec);
        },

        registerWorkspaces : function(wsList) {
            //it is LazyStack
            var ns = this.getComponent("nav-stack");
            for (var i = 0, l = wsList.length; i < l; ++i) {
                var wspec = wsList[i];
                var cname = wspec["qxClass"];
                qx.log.Logger.info("Registering workspace: " + cname);
                if (cname == null) {
                    continue;
                }
                ns.registerWidget(cname, function(id, opts) {
                    var clazz = qx.Class.getByName(id);
                    var wspec = opts["wspec"];
                    qx.log.Logger.info("Creating new instance of workspace: " + clazz);
                    if (!clazz) {
                        throw new Error("Class: '" + id + "' is not defined");
                    }
                    var cargs = Array.isArray(wspec["args"]) ? wspec["args"] : undefined;
                    return this.__construct(clazz, cargs);
                }, {cache : true, wspec : wspec}, this);
            }
        },

        __bootstrap : function() {
            ncms.Application.INSTANCE = this;
            ncms.Application.APP_STATE = new ncms.AppState("app.state");
        },

        __construct : function(constructor, args) {
            function F() {
                return constructor.apply(this, args);
            }

            F.prototype = constructor.prototype;
            return new F();
        }
    },

    defer : function(statics) {
        //Class modulations
        qx.Class.include(qx.ui.table.Table, qx.ui.table.MTableContextMenu);
        if (statics.ACT == null) {
            statics.ACT = new ncms.Actions();
        }
    }
});