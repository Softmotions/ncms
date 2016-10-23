/**
 * Alert notifications.
 */
qx.Class.define("ncms.Alerts", {
    type: "static",

    statics: {

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
            ncms.Alerts.infoPopup(message, options);
        },

        /**
         * Close all active popups
         */
        closeAllPopups: function () {
            var info = ncms.Application.INFO_POPUP;
            info && info.getChildren().forEach(function (c) {
                try {
                    c.destroy();
                } catch (e) {
                }
            });
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
                            var top = 10;
                            this.setLayoutProperties({
                                left: left,
                                top: top
                            });
                        }
                    }
                }, info);
                root.add(info);
            }
            (function () {
                var windows = root.getWindows();
                var maxWindowZIndex = info.getZIndex();
                var maxWindow = info;
                if (typeof options["overZ"] === "number") {
                    maxWindowZIndex = Math.max(maxWindowZIndex, options["overZ"]);
                    maxWindow = null;
                }
                for (var i = 0; i < windows.length; i++) {
                    var zIndex = windows[i].getZIndex();
                    maxWindowZIndex = Math.max(maxWindowZIndex, zIndex);
                    if (zIndex == maxWindowZIndex) {
                        maxWindow = windows[i];
                    }
                }
                if (maxWindow != info) {
                    info.setZIndex(maxWindowZIndex + 1e3);
                }
            })();

            if (info.getChildren().length >= 10) {
                qx.log.Logger.warn("Too many popups opened");
                qx.log.Logger.warn("Popup message: ", message);
                return null;
            }
            if (options["icon"] === false) {
                options["icon"] = null;
            } else {
                options["icon"] = (options["icon"] != null) ? options["icon"] : "ncms/icon/32/information.png";
            }
            var el = new qx.ui.basic.Atom(message, options["icon"]).set({
                center: true,
                rich: true,
                selectable: true,
                appearance: "ncms-info-popup",
                maxWidth: 400
            });
            info.add(el);

            if (!options["forever"]) {
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
                        var ah =
                            qx.bom.element.Animation
                            .animate(this.getContentElement().getDomElement(), fadeOut);
                        ah.once("end", function () {
                            this.destroy();
                        }, this);
                    }
                    this.addListenerOnce("click", function () {
                        ah && ah.stop();
                        this.destroy();
                    }, this);
                }, el);
            }
            return el;
        }
    }
});