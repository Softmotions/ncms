/**
 * Actions repository
 */
qx.Class.define("ncms.Actions", {
    extend : sm.conn.Actions,

    construct : function() {
        this.base(arguments);
        this._testPrefix = "http://localhost:8080";
        this._resourceManager = qx.util.ResourceManager.getInstance();

        //Начальное состояние GUI клиента
        this._action("app.state", "/ncms/rs/adm/ws/state");

        //Logout
        this._action("app.logout", "/ncms/rs/adm/ws/logout");

        //Компоненты рабочих зон
        this._action("nav.selectors", "/ncms/rs/adm/ui/widgets/navigation-selectors")
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