/**
 * Ncms Application
 *
 * @asset(icons/*)
 */
qx.Class.define("ncms.Application", {
    extend : qx.application.Standalone,
    include : [qx.locale.MTranslation],
    statics : {

        //ACT : new ncms.Actions(),
        APP_STATE : null

        ///////////////////////////////////////////////////////////
        //                         Alerts
        ///////////////////////////////////////////////////////////
//        confirm : function(message, callback, context) {
//            var root = qx.core.Init.getApplication().getRoot();
//            (new dialog.Confirm({
//                "message" : message,
//                "callback" : callback || null,
//                "context" : context || null,
//                "blockerOpacity" : root.getBlockerOpacity(),
//                "blockerColor" : root.getBlockerColor() || "transparent",
//                "yesButtonLabel" : root.tr("Да"),
//                "noButtonLabel" : root.tr("Нет")
//            })).show();
//        },
//        alert : function(message, callback, context) {
//            var root = qx.core.Init.getApplication().getRoot();
//            (new dialog.Alert({
//                "message" : message,
//                "callback" : callback || null,
//                "context" : context || null,
//                "blockerOpacity" : root.getBlockerOpacity(),
//                "blockerColor" : root.getBlockerColor() || "transparent"
//            })).show();
//        },
//        warning : function(message, callback, context) {
//            var root = qx.core.Init.getApplication().getRoot();
//            (new dialog.Alert({
//                "message" : message,
//                "callback" : callback || null,
//                "context" : context || null,
//                "blockerOpacity" : root.getBlockerOpacity(),
//                "blockerColor" : root.getBlockerColor() || "transparent",
//                "image" : "icon/48/status/dialog-warning.png"
//            })).show();
//        },
//        prompt : function(message, callback, context) {
//            var root = qx.core.Init.getApplication().getRoot();
//            (new dialog.Prompt({
//                "message" : message,
//                "callback" : callback || null,
//                "context" : context || null,
//                "blockerOpacity" : root.getBlockerOpacity(),
//                "blockerColor" : root.getBlockerColor() || "transparent"
//            })).show();
//        }
    },
    members : {

        __header : null,

        __nav : null,

        main : function() {
            // Call super class
            this.base(arguments);

            // Enable logging in debug variant
            if (qx.core.Environment.get("ncms.debug")) {
                qx.log.appender.Native;
                qx.log.appender.Console;
            }
            this.getRoot().setBlockerColor("black");
            this.getRoot().setBlockerOpacity(0.5);

            var comp = new qx.ui.container.Composite(new qx.ui.layout.Dock().set({separatorY : "separator-vertical"}));
            this.getRoot().add(comp, {edge : 0});

            //var bt = new qx.ui.form.Button(this.tr("Тест"));


//            ncms.Application.APP_STATE = new ncms.app.AppState(ncms.Application.ACT.getUrl("app.state"));
//            this.__header = new ncms.Header();
//            comp.add(this.__header, {edge : "north"});
//
//            var hsp = new qx.ui.splitpane.Pane();
//            comp.add(hsp);
//
//            var right = new sm.ui.cont.LazyStack();
//            this.__nav = new ncms.nav.NavSide(right);
//            this.__nav.setWidth(350);
//            hsp.add(this.__nav, 0);
//            hsp.add(right, 1);
        }
    }
});