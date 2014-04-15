/**
 * Ncms Application
 *
 * @asset(icons/*)
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
        }
    },


    events : {

        /**
         * Fired when main gui widget created and attached
         */
        "guiInitialized" : "qx.event.type.Event"
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






            this.fireEvent("guiInitialized");
        },

        __bootstrap : function() {
            ncms.Application.INSTANCE = this;
            ncms.Application.APP_STATE = new sm.app.AppState(ncms.Application.ACT.getUrl("app.state"));
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