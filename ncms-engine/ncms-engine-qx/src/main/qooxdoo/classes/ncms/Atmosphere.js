/**
 * Atmosphere integration.
 */
qx.Class.define("ncms.Atmosphere", {
    extend: qx.core.Object,

    events: {

        /**
         * Data: server message JSON object
         */
        "message": "qx.event.type.Data",

        /**
         * Fired if a connection to the server was lost.
         *
         * Data:
         *
         * {
         *   status: {Number} response status.
         *   state: {String} response state, eg: re-connecting
         *   reason: {String} Human readable error reason
         * }
         */
        "serverDisconnected": "qx.event.type.Data",

        /**
         * Fired if a connection to the server restored after `serverDisconnected`
         */
        "serverReconnected": "qx.event.type.Event",

        /**
         * Fired if reconnect attempt occurred
         */
        "reconnectAttempt": "qx.event.type.Event"
    },

    construct: function (opts) {
        this.base(arguments);
        if (!window.atmosphere) {
            throw new Error("Atmosphere library not found");
        }
        var atm = this.__atm = window.atmosphere;
        opts = this.__opts = qx.lang.Object.mergeWith({}, opts || {});
        if (opts.url != null && opts.url.indexOf("http") !== 0) {
            opts.url = window.location.protocol + "//" + window.location.host + opts.url;
        }
        opts = qx.lang.Object.mergeWith(opts, {
            transport: "long-polling",
            fallbackTransport: "long-polling",
            /*logLevel: "debug",*/
            trackMessageLength: true,
            trackMessageSize: true,
            reconnectInterval: 5000,
            maxReconnectOnClose: 17280 // 1 day
        }, false);

        opts.onOpen = this.__onOpen.bind(this);
        opts.onClose = this.__onClose.bind(this);
        opts.onMessage = this.__onMessage.bind(this);
        opts.onError = this.__onError.bind(this);
        opts.onReconnect = this.__onReconnect.bind(this);
        opts.onReopen = this.__onReopen.bind(this);
        opts.onMessagePublished = this.__onMessagePublished.bind(this);
        opts.onClientTimeout = this.__onClientTimeout.bind(this);
        opts.onTransportFailure = this.__onTransportFailure.bind(this);
        opts.onOpenAfterResume = this.__onOpenAfterResume.bind(this);
        opts.onLocalMessage = this.__onLocalMessage.bind(this);

    },

    members: {

        __atm: null,

        __channel: null,

        __opts: null,

        __disconnected: false,

        __checkResponse: function (resp) {
            if (resp == null || !resp.headers) {
                return true;
            }
            if (resp.headers["X-Softmotions-Login"] != null) {
                qx.log.Logger.info("Perform logout!");
                try {
                    this.__atm.unsubscribe();
                } catch (ignored) {
                } finally {
                    ncms.Application.logout();
                }
                return false;
            }
            return true;
        },

        __onOpen: function (resp) {
            if (!this.__checkResponse(resp)) return;
            qx.log.Logger.info("onOpen");
            if (this.__disconnected) {
                this.fireEvent("serverReconnected");
            }
            this.__disconnected = false;
        },

        __onClose: function (resp) {
            qx.log.Logger.info("onClose");
            if (!this.__disconnected) {
                this.fireDataEvent("serverDisconnected", {
                    status: resp.status,
                    state: resp.state,
                    reason: resp.reason
                });
            }
            this.__disconnected = true;
        },

        __onMessage: function (resp) {
            if (!this.__checkResponse(resp)) return;
            var message = resp.responseBody;
            console.log("Atmosphere message: " + message);
            try {
                this.fireDataEvent("message", JSON.parse(message));
            } catch (e) {
                console.error('Error: ', e);
            }
        },

        __onReopen: function (resp) {
            if (!this.__checkResponse(resp)) return;
            qx.log.Logger.info("onReopen");
            if (this.__disconnected) {
                this.fireEvent("serverReconnected");
            }
            this.__disconnected = false;
        },

        __onReconnect: function (req, resp) {
            if (!this.__checkResponse(resp)) return;
            qx.log.Logger.info(
                "onReconnect" +
                " status=" + resp.status +
                " error=" + resp.error +
                " state=" + resp.state +
                " reason=" + resp.reason);
            if (!this.__disconnected) {
                this.fireDataEvent("serverDisconnected", {
                    status: resp.status,
                    state: resp.state,
                    reason: resp.reason
                });
            }
            this.__disconnected = true;
            this.fireEvent("reconnectAttempt");
        },

        __onMessagePublished: function (req, resp) {
            qx.log.Logger.info("onMessagePublished");
        },

        __onClientTimeout: function (req) {
            qx.log.Logger.info("onClientTimeout");
        },

        __onError: function (resp) {
            if (!this.__checkResponse(resp)) return;
            qx.log.Logger.info("onError");
        },

        __onTransportFailure: function (err, r) {
            qx.log.Logger.info("onTransportFailure: " + err);
        },

        __onOpenAfterResume: function (req) {
            this.__disconnected = false;
        },

        __onLocalMessage: function (resp) {
            qx.log.Logger.info("onLocalMessage");
        },

        push: function (data) {
            if (this.__channel) {
                this.__channel.push(JSON.stringify(data));
            }
        },

        activate: function () {
            this.deactivate();
            qx.log.Logger.info("Activating atmosphere");
            this.__channel = this.__atm.subscribe(this.__opts);
        },

        deactivate: function () {
            if (this.__atm == null) {
                return;
            }
            qx.log.Logger.info("Deactivate atmosphere");
            this.__atm.unsubscribe();
            this.__channel = null;
            this.__disconnected = false;
        }
    },

    destruct: function () {
        this.deactivate();
    }
});