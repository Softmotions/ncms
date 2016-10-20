/**
 * Atmosphere integration.
 */
qx.Class.define("ncms.Atmosphere", {
    extend: qx.core.Object,

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
            console.log("onOpen");
        },

        __onClose: function (resp) {
        },

        __onMessage: function (resp) {
            var message = resp.responseBody;
            console.log("onMessage: " + message);
            try {
                var json = JSON.parse(message);
            } catch (e) {
                console.log('Error: ', message.data);
                return;
            }
        },

        __onReconnect: function (req, resp) {
            console.log("onReconnect");
        },

        __onMessagePublished: function (req, resp) {
            console.log("onMessagePublished");
        },

        __onClientTimeout: function (req) {
            console.log("onClientTimeout");
        },

        __onError: function (resp) {
            console.log("onError");
        },

        __onTransportFailure: function (err, r) {
            console.log("onTransportFailure: " + err);
        },

        __checkState: function () {

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