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
        this.__msgs = {};
    },

    members: {

        __msgs: null,

        __alert: null,

        addMessages: function (caption, messages, isError) {
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
            if (captions.length == 0) {
                return;
            }
            if (!isNotification) {

                var alert = this.__alert || new sm.alert.DefaultAlertMessages(this.__caption);
                captions.forEach(function (caption) {
                    var slot = this.__msgs[caption];
                    alert.addMessages(caption, slot["messages"], slot["isError"]);
                }, this);
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
                var el = ncms.Application.infoPopup(buf.join("<br>"), {
                    showTime: hasErrors ? 60000 : 5000, // todo review
                    icon: hasErrors ? "ncms/icon/32/exclamation.png" : null
                });
                el.addListenerOnce("disappear", function () {
                    this.fireEvent("close");
                    this.__destroy();
                }, this);
            }
        },

        __destroy: function() {
            this.__msgs = {};
            this.__alert = null;
        }
    },

    destruct: function () {
        this.__caption = null;
        this.__destroy();
    }
});