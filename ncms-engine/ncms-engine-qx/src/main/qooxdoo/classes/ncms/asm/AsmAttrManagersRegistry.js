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

        /**
         * Assembly managers instances cache
         */
        INSTANCES : {},

        createAttrManagerInstance : function(amClassName) {
            if (this.INSTANCES[amClassName] != null) {
                return this.INSTANCES[amClassName];
            }
            var clazz = qx.Class.getByName(amClassName);
            qx.core.Assert.assertTrue(clazz != null);
            qx.core.Assert.assertTrue(qx.Class.hasInterface(clazz, ncms.asm.IAsmAttributeManager));
            var am = sm.lang.Object.newInstance(clazz);
            this.INSTANCES[amClassName] = am;
            return am;
        }

    }
});