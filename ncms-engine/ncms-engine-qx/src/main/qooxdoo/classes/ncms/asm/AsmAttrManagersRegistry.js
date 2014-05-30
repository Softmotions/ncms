/**
 *  Registry of available assembly attrubute managers.
 */
qx.Class.define("ncms.asm.AsmAttrManagersRegistry", {
    type : "static",

    statics : {

        CLASSES : [

            //Simple string editor
            ncms.asm.am.RawAM,
            ncms.asm.am.StringAM
        ],


        createAttrManagerInstance : function(amClassName) {
            var clazz = qx.Class.getByName(amClassName);
            qx.core.Assert.assertTrue(clazz != null);
            qx.core.Assert.assertTrue(qx.Class.hasInterface(clazz, ncms.asm.IAsmAttributeManager));
            return sm.lang.Object.newInstance(clazz);
        },


        /**
         * Iterates over all attribute managers classes
         * in projection of supported types.
         *
         * @param cb {forEachAttributeManagerClassCB}
         *
         * @callback forEachAttributeManagerClassCB
         * @param type {String}
         * @param clazz {qx.Class}
         */
        forEachAttributeManagerTypeClassPair : function(cb) {
            sm.lang.Object.forEachClass(function(clazz) {
                if (!qx.Class.hasInterface(clazz, ncms.asm.IAsmAttributeManager) ||
                        typeof clazz.getSupportedAttributeTypes !== "function" ||
                        typeof clazz.getDescription !== "function") {
                    return;
                }
                var types = clazz.getSupportedAttributeTypes() || [];
                for (var i = 0; i < types.length; ++i) {
                    var type = types[i];
                    cb(type, clazz);
                }
            }, this);
        }

    }
});