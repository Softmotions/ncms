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
 * @asset(ncms/icon/32/information.png)
 * @asset(ncms/icon/32/exclamation.png)
 * @asset(ncms/icon/32/error.png)
 * @asset(ncms/icon/32/exclamation.png)
 *
 * Ace editor
 * @asset(ncms/script/ace_all.js)
 *
 * Atmosphere
 * @asset(ncms/script/atmosphere.js)
 * @asset(ncms/script/atmosphere.min.js)
 */
qx.Class.define("ncms.Application", {
    extend: qx.application.Standalone,
    include: [qx.locale.MTranslation],

    statics: {

        INSTANCE: null,
        APP_STATE: null,
        ACT: null,
        INFO_POPUP: null,

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
                "ncms/script/atmosphere.js"
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


        confirmCb: function (message, cblabel, cbvalue, callback, context) {
            (new sm.dialog.Confirm({
                "message": message,
                "callback": callback || null,
                "context": context || null,
                "checkbox": {
                    "label": cblabel,
                    "value": cbvalue
                }
            })).open();
        },

        confirm: function (message, callback, context) {
            (new sm.dialog.Confirm({
                "message": message,
                "callback": callback || null,
                "context": context || null
            })).open();
        },

        alert: function (message, callback, context) {
            (new sm.dialog.Message({
                "message": message,
                "callback": callback || null,
                "context": context || null
            })).open();
        },

        warning: function (message, callback, context) {
            (new sm.dialog.Message({
                "message": message,
                "callback": callback || null,
                "context": context || null,
                "image": "ncms/icon/32/exclamation.png"
            })).open();
        },


        errorPopup: function (message, options) {
            options = options || {};
            if (options["icon"] === undefined) {
                options["icon"] = "ncms/icon/32/error.png";
            }
            if (options["showTime"] == null) {
                options["showTime"] = Number.MAX_VALUE;
            }
            this.infoPopup(message, options);
        },

        /**
         * Show auto-hided popup message
         *
         * @param message {String} message to show
         * @param options {Object?} additional options:
         *       - icon - icon to show. by default of if <code>undefined</code> will be shown default icon: status/dialog-information,
         *                              if <code>null</code> no icon will be shown
         *       - showTime {Number?} time in ms to show message, default 1500 ms
         *       - hideTime {Number?} fade out animation duration, default 500 ms
         */
        infoPopup: function (message, options) {
            options = options || {};
            var showTime = options["showTime"];
            var hideTime = options["hideTime"];
            var root = qx.core.Init.getApplication().getRoot();
            var info = ncms.Application.INFO_POPUP;

            if (!info) {

                info = ncms.Application.INFO_POPUP =
                    new qx.ui.container.Composite(new qx.ui.layout.VBox(4)
                    .set({alignX: "center"}));
                info.getContentElement().addClass("ncms-app-popup");

                info.isEmpty = function () {
                    return !info.hasChildren();
                };

                info.addListener("resize", function () {
                    var parent = this.getLayoutParent();
                    if (parent) {
                        var bounds = parent.getBounds();
                        if (bounds) {
                            var hint = this.getSizeHint();
                            var left = Math.round((bounds.width - hint.width) / 2);
                            this.setLayoutProperties({
                                left: left,
                                top: 10
                            });
                        }
                    }
                }, info);
                root.add(info);

            } else {
                var maxWindowZIndex = info.getZIndex();
                var windows = root.getWindows();
                for (var i = 0; i < windows.length; i++) {
                    if (windows[i] != this) {
                        var zIndex = windows[i].getZIndex();
                        maxWindowZIndex = Math.max(maxWindowZIndex, zIndex);
                    }
                }
                info.setZIndex(maxWindowZIndex + 1e8);
            }

            if (info.getChildren().length >= 10) {
                qx.log.Logger.warn("Too many popups opened");
                qx.log.Logger.warn("Popup message: ", message);
                return null;
            }

            options["icon"] = (options["icon"] != null) ? options["icon"] : "ncms/icon/32/information.png";
            var el = new qx.ui.basic.Atom(message, options["icon"]).set({
                center: true,
                rich: true,
                selectable: true,
                appearance: "ncms-info-popup"
            });
            info.add(el);

            var fadeOut = {
                duration: hideTime || 500,
                delay: showTime != null ? showTime : 1500,
                timing: "ease-out",
                keep: 100,
                keyFrames: {
                    0: {opacity: 1},
                    100: {opacity: 0, display: "none"}
                }
            };
            el.addListenerOnce("appear", function () {
                if (fadeOut.delay > 0) {
                    var ah = qx.bom.element.Animation.animate(this.getContentElement().getDomElement(), fadeOut);
                    ah.once("end", function () {
                        this.destroy();
                    }, this);
                }
                this.addListener("click", function () {
                    ah && ah.stop();
                    this.destroy();
                }, this);
            }, el);

            return el;
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
            if (typeof date === "number") {
                date = new Date(date);
            } else if (typeof date === "string") {
                date = new Date(date);
            }
            var format = qx.locale.Date.getDateFormat(formatName || "medium").toString();
            if (format == null) {
                format = qx.locale.Date.getDateFormat("medium").toString();
            }
            var df = new qx.util.format.DateFormat(format);
            return df.format(date);
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

        _createRootWidget: function () {
            var root = new qx.ui.root.Application(document);
            root.setWindowManager(new sm.ui.window.ExtendedWindowManager());
            return root;
        },

        logout: function () {
            this.__logoutPending = true;
            window.location.href = ncms.Application.ACT.getUrl("app.logout");
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
                this.fireEvent("guiInitialized");
            }, this);
        },

        __initAtmosphere: function () {
            if (this.__atmosphere) {
                this._disposeObjects("__atmosphere");
            }
            this.__atmosphere = new ncms.Atmosphere({
                url: ncms.Application.ACT.toUri("/ws/adm/ui")
            });
            this.__atmosphere.activate();
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
                window.alert(qx.locale.Manager.tr("Your user session expired! Please login again"));
                window.location.reload(true);
            }.bind(this);
            ncms.Application.INSTANCE = this;
            ncms.Application.APP_STATE = new ncms.AppState("app.state");
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

        // Location of UI based on `navigator.languages[0]` instead of `navigator.language`
        // todo review it for IE/EDGE
        var browser = qx.core.Environment.get("browser.name");
        if (browser !== "ie" && browser !== "edge") { // Fix locale
            navigator.userLanguage = navigator.languages[0] || navigator.language || "";
            var locale = navigator.userLanguage;
            var ind = locale.indexOf("-");
            if (ind !== -1) {
                locale = locale.substr(0, ind);
            }
            qx.locale.Manager.getInstance().setLocale(locale);
        }

        // Set alert windows implementation to `sm.io.Request.`
        sm.io.Request.ALERT_WND_IMPL = ncms.AlertPopupMessages;
    }
});