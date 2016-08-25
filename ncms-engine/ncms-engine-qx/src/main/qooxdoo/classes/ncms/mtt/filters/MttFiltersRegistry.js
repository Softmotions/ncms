/**
 * Registry of available mtt filters.
 */
qx.Class.define("ncms.mtt.filters.MttFiltersRegistry", {
    type: "static",

    statics: {

        TYPE2CLASS: null,

        CLASSES: [
            ncms.mtt.filters.MttVHostFilter,
            ncms.mtt.filters.MttParametersFilter,
            ncms.mtt.filters.MttHeadersFilter,
            ncms.mtt.filters.MttCookiesFilter,
            ncms.mtt.filters.MttPageFilter,
            ncms.mtt.filters.MttUserAgentFilter,
            ncms.mtt.filters.MttResourceFilter
        ],

        createMttFilterInstance: function (clazz) {
            clazz = (typeof clazz === "string") ? qx.Class.getByName(clazz) : clazz;
            qx.core.Assert.assertTrue(clazz != null);
            qx.core.Assert.assertTrue(qx.Class.hasInterface(clazz, ncms.mtt.filters.IMttFilter));
            return sm.lang.Object.newInstance(clazz);
        },

        forEachMttFilterTypeClassPair: function (cb) {
            sm.lang.Object.forEachClass(function (clazz) {
                if (!qx.Class.hasInterface(clazz, ncms.mtt.filters.IMttFilter) ||
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

        findMttFilterClassForType: function (type) {
            if (this.TYPE2CLASS != null) {
                return this.TYPE2CLASS[type];
            }
            var me = this;
            this.TYPE2CLASS = {};
            this.forEachMttFilterTypeClassPair(function (itype, clazz) {
                me.TYPE2CLASS[itype] = clazz;
            });
            return this.TYPE2CLASS[type];
        }
    }
});