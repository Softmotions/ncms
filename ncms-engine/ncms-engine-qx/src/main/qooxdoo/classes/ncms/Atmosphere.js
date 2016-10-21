/**
 * Atmosphere integration.
 */
qx.Class.define("ncms.Atmosphere", {
    extend: qx.core.Object,


    events: {
        "message": "qx.event.type.Data"
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
            transport: "streaming",
            fallbackTransport: "long-polling",
            /*logLevel: "debug",*/
            trackMessageLength: true,
            trackMessageSize: true,
            reconnectInterval: 5000,
            maxReconnectOnClose: 360
        }, false);

        opts.onOpen = this.__onOpen.bind(this);
        opts.onClose = this.__onClose.bind(this);
        opts.onMessage = this.__onMessage.bind(this);
        opts.onError = this.__onError.bind(this);
        opts.onReconnect = this.__onReconnect.bind(this);
        opts.onMessagePublished = this.__onMessagePublished.bind(this);
        opts.onClientTimeout = this.__onClientTimeout.bind(this);
        opts.onTransportFailure = this.__onTransportFailure.bind(this);

    },

    members: {

        __atm: null,

        __channel: null,

        __opts: null,

        __onOpen: function (resp) {
            qx.log.Logger.info("onOpen");
        },

        __onClose: function (resp) {
            qx.log.Logger.info("onClose");
        },

        __onMessage: function (resp) {
            var message = resp.responseBody;
            try {
                this.fireDataEvent("message", JSON.parse(message));
            } catch (e) {
                console.log('Error: ', message.data);
            }
        },

        __onReconnect: function (req, resp) {
            qx.log.Logger.info("onReconnect");
        },

        __onMessagePublished: function (req, resp) {
            qx.log.Logger.info("onMessagePublished");
        },

        __onClientTimeout: function (req) {
            qx.log.Logger.info("onClientTimeout");
        },

        __onError: function (resp) {
            qx.log.Logger.info("onError");
        },

        __onTransportFailure: function (err, r) {
            qx.log.Logger.info("onTransportFailure: " + err);
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
        }
    },


    destruct: function () {
        this.deactivate();
    }
});