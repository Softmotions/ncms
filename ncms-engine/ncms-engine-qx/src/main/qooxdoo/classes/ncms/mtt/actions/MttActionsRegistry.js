/**
 * Registry of available mtt actions.
 */
qx.Class.define("ncms.mtt.actions.MttActionsRegistry", {
    type: "static",

    statics: {

        TYPE2CLASS: null,

        CLASSES: [
            ncms.mtt.actions.MttLogAction,
            ncms.mtt.actions.MttRouteAction,
            ncms.mtt.actions.MttSetCookieAction,
            ncms.mtt.actions.MttSetRequestParametersAction,
            ncms.mtt.actions.MttABMarksAction,
            ncms.mtt.actions.MttRememberOriginAction
        ],

        createMttActionInstance: function (clazz) {
            clazz = (typeof clazz === "string") ? qx.Class.getByName(clazz) : clazz;
            qx.core.Assert.assertTrue(clazz != null);
            qx.core.Assert.assertTrue(qx.Class.hasInterface(clazz, ncms.mtt.actions.IMttAction));
            return sm.lang.Object.newInstance(clazz);
        },

        forEachMttActionTypeClassPair: function (cb) {
            sm.lang.Object.forEachClass(function (clazz) {
                if (!qx.Class.hasInterface(clazz, ncms.mtt.actions.IMttAction) ||
                    typeof clazz.getDescription !== "function" ||
                    typeof clazz.getType !== "function" ||
                    typeof clazz.specForHuman !== "function") {
                    return;
                }
                if (cb(clazz.getType(), clazz) === false) {
                    return false; //abort iteration
                }
            }, this);
        },

        findMttActionClassForType: function (type) {
            if (this.TYPE2CLASS != null) {
                return this.TYPE2CLASS[type];
            }
            var me = this;
            this.TYPE2CLASS = {};
            this.forEachMttActionTypeClassPair(function (itype, clazz) {
                me.TYPE2CLASS[itype] = clazz;
            });
            return this.TYPE2CLASS[type];
        }
    }
});