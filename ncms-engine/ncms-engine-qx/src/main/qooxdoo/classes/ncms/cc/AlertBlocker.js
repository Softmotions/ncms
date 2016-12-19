/**
 * Alert Blocker with optional message
 * @asset(ncms/icon/32/exclamation.png)
 */
qx.Class.define("ncms.cc.AlertBlocker", {
    extend: qx.ui.core.Widget,

    events: {},

    /**
     * Creates a blocker for the passed widget.
     *
     * @param widget {qx.ui.core.Widget} Widget which should be added the blocker
     */
    construct: function (widget) {
        this.__blocker = new qx.ui.core.Blocker(widget);
        this.__blocker.setColor("#EFEFEF");
        this.__blocker.setOpacity(0.7);

        this.__widget = widget;
        
        this.__widget.addListener("appear", function () {
            this.__appeared = true;
        }, this);

        this.__widget.addListener("disappear", function () {
            this.__appeared = false;
        }, this);
    },

    members: {

        __appeared: false,

        __widget: null,
        
        __blocker: null,

        __blockerAtom: null,

        __centerBa: function () {
            var me = this;
            window.setTimeout(function () {
                if (me.__blockerAtom) {
                    var hint = me.__blockerAtom.getSizeHint();
                    var cl = me.__widget.getContentLocation("box");
                    me.__blockerAtom.setLayoutProperties({
                        left: Math.round(cl.left + (cl.right - cl.left - hint.width) / 2),
                        top: Math.round(cl.top + (cl.bottom - cl.top - hint.height) / 2)
                    });
                }
            });
        },

        __doBlock: function () {
            var me = this;
            if (!me.__blocker.isBlocked()) {
                me.__blocker.block();
                me.__centerBa();
                if (me.__blockerAtom) {
                    me.__blockerAtom.show();
                }
            }
        },

        __doUnblock: function () {
            var me = this;
            if (me.__blocker.isBlocked()) {
                me.__blocker.unblock();
                if (me.__blockerAtom) {
                    me.__blockerAtom.exclude();
                }
            }
        },
        
        block: function (text) {
            if (text) {
                if (this.__blockerAtom == null) {
                    this.__blockerAtom =
                        new qx.ui.basic.Atom(null, "ncms/icon/32/exclamation.png").set({
                            center: true,
                            rich: true,
                            selectable: true,
                            appearance: "ncms-info-popup",
                            padding: 20,
                            maxWidth: 300
                        });
                    this.__blockerAtom.setLabel(this.tr(text));
                    console.log(this.__widget.getZIndex());
                    this.__blockerAtom.setZIndex(this.__widget.getZIndex() + 1e3);
                    this.__widget.addListener("resize", this.__centerBa, this);
                    this.__widget.addListener("move", this.__centerBa, this);
                    this.__widget.addListener("disappear", this.__doUnblock, this);
                    qx.core.Init.getApplication().getRoot()._add(this.__blockerAtom);
                    this.__doUnblock.call(this);
                }
            }
            this.__doBlock.call(this);
        },

        unblock: function () {
            this.__doUnblock.call(this);
        },

        onAppear: function (flag) {
            if (this.__appeared && flag) {
                this.__doBlock.call(this);
            }
        }
    },

    destruct: function () {
        this.__widget = null;
        this.__blocker = null;
        this.__blockerAtom = null;
        this.__dispose();
    }

});
