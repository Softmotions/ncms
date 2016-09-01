/**
 * Lightweight notifications popups.
 *
 * @asset(ncms/icon/32/exclamation.png)
 */
qx.Class.define("ncms.AlertPopupMessages", {
    extend: qx.core.Object,
    implement: [sm.alert.IAlertMessages],
    include: [sm.event.MForwardEvent],

    events: {

        "close": "qx.event.type.Event"
    },

    construct: function (caption) {
        this.base(arguments);
        this.__caption = caption;
        this.__msgs = null;
    },

    members: {

        __msgs: null,

        __alert: null,

        addMessages: function (caption, messages, isError) {
            this.__msgs = this.__msgs || {};
            this.__msgs[caption] = {
                messages: messages,
                isError: !!isError
            };
        },

        activate: function (isNotification) {
            if (!this.__msgs) {
                return;
            }
            var captions = Object.keys(this.__msgs);
            if (!isNotification) {

                var alert = this.__alert || new sm.alert.DefaultAlertMessages(this.__caption);
                captions.forEach(function (caption) {
                    var slot = this.__msgs[caption];
                    alert.addMessages(caption, slot["messages"], slot["isError"]);
                }, this);
                this.__msgs = null;
                if (alert !== this.__alert) {
                    alert.addListenerOnce("close", function (ev) {
                        this.forwardEvent(ev);
                        this.__destroy();
                    }, this);
                }
                alert.activate(false);

            } else { // is lightweight

                var buf = [];
                var hasErrors = false;
                captions.forEach(function (caption) {
                    buf.push("<strong>" + qx.lang.String.stripTags(caption) + "</strong>");
                    var slot = this.__msgs[caption];
                    slot.messages.forEach(function (m) {
                        buf.push(qx.lang.String.stripTags(m));
                    }, this);
                    if (slot.isError) {
                        hasErrors = true;
                    }
                }, this);
                this.__msgs = null;
                var el = ncms.Application.infoPopup(buf.join("<br>"), {
                    showTime: hasErrors ? 0 : 5000, // todo review?
                    icon: hasErrors ? "ncms/icon/32/exclamation.png" : null
                });
                if (el) { // No errors
                    el.addListenerOnce("disappear", function () {
                        if (ncms.Application.INFO_POPUP.isEmpty()) {
                            this.fireEvent("close");
                        }
                        this.__destroy();
                    }, this);
                }
            }
        },

        __destroy: function () {
            this.__msgs = null;
            this.__alert = null;
        }
    },

    destruct: function () {
        this.__caption = null;
        this.__destroy();
    }
});