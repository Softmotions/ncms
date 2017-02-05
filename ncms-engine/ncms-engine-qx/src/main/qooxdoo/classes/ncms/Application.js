/**
 * Ncms Application
 *
 * WSA abbrev means: workspace area (big right side zone)
 *
 *
 * @use(ncms.asm.AsmNav)
 * @use(ncms.mmgr.MediaNav)
 * @use(ncms.pgs.PagesNav)
 * @use(ncms.usr.UsersNav)
 * @use(ncms.news.NewsNav)
 * @use(ncms.mtt.MttNav)
 * @use(ncms.mtt.tp.MttTpNav)
 *
 * @asset(ncms/icon/16/help/help.png)
 * @asset(ncms/icon/16/misc/door_in.png)
 *
 * Ace editor
 * @asset(ncms/script/ace_all.js)
 * @asset(ncms/script/worker-css.js)
 * @asset(ncms/script/worker-html.js)
 * @asset(ncms/script/worker-javascript.js)
 * @asset(ncms/script/worker-json.js)
 * @asset(ncms/script/worker-xml.js)
 *
 * Atmosphere
 * @asset(ncms/script/atmosphere.js)
 * @asset(ncms/script/atmosphere.min.js)
 *
 * Medium editor
 * @asset(ncms/script/medium-editor.js)
 * @asset(ncms/script/medium-editor.min.js)
 * @asset(ncms/script/ncms-preview.js)
 * @asset(ncms/css/medium-editor.css)
 *
 * Clipboard.js
 * @asset(ncms/script/clipboard.min.js)
 */
qx.Class.define("ncms.Application", {
    extend: qx.application.Standalone,
    include: [qx.locale.MTranslation],

    statics: {

        INSTANCE: null,
        APP_STATE: null,
        UUID: null,
        ACT: null,

        //translations
        EXTERNAL_TRANSLATIONS: [
            qx.locale.Manager.tr("Your user session expired! Please login again")
        ],

        ///////////////////////////////////////////////////////////
        //                      Extra scripts                    //
        ///////////////////////////////////////////////////////////

        loadExtraScripts: function (cb, ctx) {
            var resource = [
                "ncms/script/ace_all.js",
                "ncms/script/atmosphere.js",
                "ncms/script/clipboard.min.js"
            ];
            var load = function (list) {
                if (list.length == 0) {
                    cb.call(ctx);
                    return;
                }
                var res = list.shift();
                console.log("Loading extra script: " + res);
                var uri = qx.util.ResourceManager.getInstance().toUri(res);
                var loader = new qx.bom.request.Script();
                loader.onload = function () {
                    load(list);
                };
                loader.open("GET", uri);
                loader.send();
            };
            load(resource);
        },

        ///////////////////////////////////////////////////////////
        //                      Request helper                   //
        ///////////////////////////////////////////////////////////

        request: function (action, obj, method, rtype) {
            if (typeof obj === "string") {
                rtype = method;
                method = obj;
                obj = null;
            }
            return new sm.io.Request(ncms.Application.ACT
            .getRestUrl(action, obj), method || "GET", rtype || "application/json");
        },

        ///////////////////////////////////////////////////////////
        //                         Alerts
        ///////////////////////////////////////////////////////////

        confirmCb: sm.alert.Alerts.confirmCb,

        confirm: sm.alert.Alerts.confirm,

        alert: sm.alert.Alerts.alert,

        warning: sm.alert.Alerts.warning,

        errorPopup: sm.alert.Alerts.errorPopup,

        infoPopup: sm.alert.Alerts.infoPopup,

        ///////////////////////////////////////////////////////////
        //              Components/Workspaces/Helpers            //
        ///////////////////////////////////////////////////////////

        getAtmosphere: function () {
            return ncms.Application.INSTANCE.getAtmosphere();
        },

        getComponent: function (name) {
            return ncms.Application.INSTANCE.getComponent(name);
        },

        registerComponent: function (name, component) {
            return ncms.Application.INSTANCE.registerComponent(name, component);
        },

        getUserId: function () {
            return ncms.Application.APP_STATE.getUserId();
        },

        userHasRole: function (role) {
            return ncms.Application.APP_STATE.userHasRole(role);
        },

        userInRoles: function (roles) {
            return ncms.Application.APP_STATE.userInRoles(roles);
        },

        activateWorkspace: function (wsSpec) {
            return ncms.Application.INSTANCE.activateWorkspace(wsSpec);
        },

        registerWorkspaces: function (wsSpec) {
            return ncms.Application.INSTANCE.registerWorkspaces(wsSpec);
        },

        getActiveWorkspace: function () {
            return ncms.Application.INSTANCE.getActiveWorkspace();
        },

        formatDate: function (date, formatName) {
            date = ncms.Application.toLocalDate(date);
            var df = new qx.util.format.DateFormat(qx.locale.Date.getDateFormat(formatName || "medium").toString());
            return df.format(date);
        },

        formatDateTime: function (date, dateFormatName, timeFormatName) {
            date = ncms.Application.toLocalDate(date);
            var df = new qx.util.format.DateFormat(qx.locale.Date.getDateFormat(dateFormatName || "medium").toString());
            var tf = new qx.util.format.DateFormat(qx.locale.Date.getTimeFormat(timeFormatName || "short").toString());
            return df.format(date) + " " + tf.format(date);
        },

        toLocalDate: function (date) {
            if (date == null || date.$$local_date) {
                return date;
            }
            if (typeof date === "string") {
                date = +new Date(date);
            } else if (typeof date === "object") {
                date = +date;
            }
            var stzo = ncms.Application.APP_STATE.getServerTZOffset();
            var ltzo = (new Date()).getTimezoneOffset() * 60 * 1000;
            var ret = new Date(date - ltzo - stzo);
            ret.$$local_date = true;
            return ret;
        },

        logout: function () {
            ncms.Application.INSTANCE.logout();
        },

        extensionPoints: function (key) {
            return ncms.Application.INSTANCE.extensionPoints(key);
        },

        registerExtensionPoint: function (key, point) {
            return ncms.Application.INSTANCE.registerExtensionPoint(key, point);
        }
    },

    events: {

        /**
         * Fired when main gui widget created and attached
         */
        "guiInitialized": "qx.event.type.Event",

        /**
         * Fired if specifig admin GUI workspace activated
         * Data: {"qxClass":<workspace loader class>, "label":<workspace name>}
         * Example data: {"qxClass":"ncms.pgs.PagesNav","label":"Страницы"}
         */
        "workspaceActivated": "qx.event.type.Data"
    },

    members: {

        /**
         * Refs to gui components
         */
        __components: null,

        __header: null,

        __nav: null,

        __logoutPending: false,

        __construct: sm.lang.Object.newInstance,

        __extensionPoints: null,

        __atmosphere: null,

        __clipboard: null,

        _createRootWidget: function () {
            var root = new qx.ui.root.Application(document);
            root.setWindowManager(new sm.ui.window.ExtendedWindowManager());
            return root;
        },

        logout: function () {
            if (!this.__logoutPending) {
                this.__logoutPending = true;
                window.location.href = ncms.Application.ACT.getUrl("app.logout");
            }
        },

        main: function () {

            this.__extensionPoints = {};
            ncms.Application.ACT = this.createActions();

            //load AsmAttrManagersRegistry
            ncms.asm.am.AsmAttrManagersRegistry


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

            var comp = new qx.ui.container.Composite(new qx.ui.layout.Dock());
            this.getRoot().add(comp, {edge: 0});

            //Toolbar
            var toolbar = new ncms.Toolbar();
            comp.add(toolbar, {edge: "north"});

            var hsp = new qx.ui.splitpane.Pane();
            comp.add(hsp);

            //Left nav side
            var navStack = new sm.ui.cont.LazyStack();
            navStack.setWidth(350);
            hsp.add(navStack, 0);

            //Right nav side
            var rightStack = this.__createRightStack();
            hsp.add(rightStack, 1);

            this.registerComponent("toolbar", toolbar);
            this.registerComponent("nav-stack", navStack);
            this.registerComponent("right-stack", rightStack);

            this.showDefaultWSA();

            ncms.Application.loadExtraScripts(function () {
                this.__initAtmosphere();
                if (typeof window.Clipboard === 'function') {
                    this.__clipboard = new window.Clipboard('.copy_button');
                }
                this.fireEvent("guiInitialized");
            }, this);

            window.addEventListener("dragover", function (ev) {
                ev.preventDefault();
            }, false);
            window.addEventListener("drop", function (ev) {
                ev.preventDefault();
            }, false);
        },

        __initAtmosphere: function () {
            if (this.__atmosphere) {
                this._disposeObjects("__atmosphere");
            }
            this.__atmosphere = new ncms.Atmosphere({
                url: ncms.Application.ACT.toUri("/ws/adm/ui")
            });
            this.__atmosphere.addListener("serverDisconnected", this.__onServerDisconnected, this);
            this.__atmosphere.activate();
            ncms.Events.getInstance().attachAtmosphere(this.__atmosphere);
        },

        __onServerDisconnected: function () {
            qx.log.Logger.warn("Server disconnected");
            var blocker = this.getRoot().getBlocker();
            blocker.block();
            var restored = null;
            sm.alert.Alerts.closeAllPopups();
            sm.alert.Alerts.infoPopup(
                this.tr("<b>Connection to server lost.</b><br>Please wait..."), {
                    forever: true,
                    overZ: 1e6,
                    icon: false
                });
            this.__atmosphere.addListenerOnce("serverReconnected", function () {
                qx.log.Logger.warn("Server reconnected");
                blocker.unblock();
                sm.alert.Alerts.closeAllPopups();
                sm.alert.Alerts.infoPopup(this.tr("Connection to server restored."), {
                    showTime: Number.MAX_VALUE
                });
            }, this);
        },

        getAtmosphere: function () {
            return this.this.__atmosphere;
        },

        getUserId: function () {
            return ncms.Application.APP_STATE.getUserId();
        },

        getComponent: function (name) {
            var val = this.__components[name];
            if (!val) {
                throw new Error("Unknown component: '" + name + "'");
            }
            return val;
        },

        registerComponent: function (name, component) {
            var val = this.__components[name];
            if (val) {
                throw new Error("Component with name: " + name + " already registered");
            }
            this.__components[name] = component;
        },

        activateWorkspace: function (wsSpec) {
            qx.log.Logger.info("Activate workspace: " + JSON.stringify(wsSpec));
            this.getComponent("nav-stack").showWidget(wsSpec["qxClass"]);
            this.fireDataEvent("workspaceActivated", wsSpec);
        },

        registerWorkspaces: function (wsList) {
            //it is LazyStack
            var ns = this.getComponent("nav-stack");
            for (var i = 0, l = wsList.length; i < l; ++i) {
                var wspec = wsList[i];
                var cname = wspec["qxClass"];
                qx.log.Logger.info("Registering workspace: " + cname);
                if (cname == null) {
                    continue;
                }
                ns.registerWidget(cname, function (id, opts) {
                    var clazz = qx.Class.getByName(id);
                    var wspec = opts["wspec"];
                    qx.log.Logger.info("Creating new instance of workspace: " + clazz);
                    if (!clazz) {
                        throw new Error("Class: '" + id + "' is not defined");
                    }
                    var cargs = Array.isArray(wspec["args"]) ? wspec["args"] : undefined;
                    return this.__construct(clazz, cargs);
                }, {cache: true, wspec: wspec}, this);
            }
        },

        getActiveWorkspace: function () {
            return this.getComponent("nav-stack").getActiveWidget();
        },

        /**
         * Display default workspace area placeholder
         * widget (ncms.wsa.WSAPlaceholder)
         */
        showDefaultWSA: function () {
            return this.showWSA("ncms.wsa.WSAPlaceholder");
        },

        /**
         * WSA abbrev means: workspace area (big right side zone)
         * @param widgetId {String}
         */
        showWSA: function (widgetId) {
            return this.getComponent("right-stack").showWidget(widgetId);
        },

        /**
         * Disposes WSA widget
         */
        disposeWSA: function (widgetId) {
            return this.getComponent("right-stack").disposeWidget(widgetId);
        },

        /**
         * WSA abbrev means: workspace area (big right side zone)
         * @param widgetId {String}
         * @returns {Widget|null|*}
         */
        getWSA: function (widgetId) {
            return this.getComponent("right-stack").getWidget(widgetId, true);
        },

        /**
         * Return active WSA ID
         * @returns {String|null}
         */
        getActiveWSAID: function () {
            return this.getComponent("right-stack").getActiveWidgetId();
        },

        /**
         * WSA abbrev means: workspace area (big right side zone)
         * @see sm.ui.cont.LazyStack.registerWidget
         */
        registerWSA: function (widgetId, factory, opts, self) {
            this.getComponent("right-stack").registerWidget(widgetId, factory, opts, self);
        },

        /**
         * Register extension point function.
         *
         * @param key {String} Extension points group
         * @param point {Function} Extension point
         */
        registerExtensionPoint: function (key, point) {
            if (typeof key !== "string") {
                throw new Error("Extension point key must be a string");
            }
            if (typeof point !== "function") {
                throw new Error("Extension point must be a function");
            }
            var epl = this.__extensionPoints[key];
            if (!Array.isArray(epl)) {
                epl = this.__extensionPoints[key] = [];
            }
            epl.push(point);
        },

        extensionPoints: function (key) {
            return this.__extensionPoints[key] || [];
        },

        __createRightStack: function () {
            var rs = new sm.ui.cont.LazyStack();
            rs.setWidgetsHidePolicy("exclude");
            rs.registerWidget("ncms.wsa.WSAPlaceholder", function () {
                return new ncms.wsa.WSAPlaceholder()
            }, null, this);
            return rs;
        },

        __bootstrap: function () {
            sm.io.Request.LOGIN_ACTION = function () {
                if (this.__logoutPending) {
                    return;
                }
                this.__logoutPending = true;
                window.alert(qx.locale.Manager.tr("Your session expired! Please login again"));
                window.location.reload(true);
            }.bind(this);
            ncms.Application.INSTANCE = this;
            ncms.Application.UUID = sm.util.UUID.generate();
            ncms.Application.APP_STATE = new sm.AppState("app.state", ncms.Application.ACT);
            qx.log.Logger.info("Application UUID: " + ncms.Application.UUID);
            // Intercapt all xhr request
            sm.io.Request.registerSendInterceptor(this.__requestPreSend, this);
        },

        __requestPreSend: function (req) {
            var url = req.getUrl();
            var part = encodeURIComponent("__app") + "=" + encodeURIComponent(ncms.Application.UUID);
            if (url.indexOf("?") !== -1) {
                url += "&" + part;
            } else {
                url += "?" + part;
            }
            req.setUrl(url);
        },

        // overridden
        close: function (val) {
            if (this.__logoutPending) {
                return;
            }
            var appName = ncms.Application.APP_STATE.getAppName() || "Application";
            if (!qx.core.Environment.get("ncms.testing")) {
                return this.tr("You leave %1", appName);
            }
        },

        createActions: function () {
            return new ncms.Actions();
        }
    },

    destruct: function () {
        this._disposeObjects("__atmosphere");
    },

    defer: function (statics) {
        // Set alert windows implementation to `sm.io.Request.`
        sm.io.Request.ALERT_WND_IMPL = sm.alert.AlertPopupMessages;
        sm.AppFixes.applyPlatformFixes();
    }
});