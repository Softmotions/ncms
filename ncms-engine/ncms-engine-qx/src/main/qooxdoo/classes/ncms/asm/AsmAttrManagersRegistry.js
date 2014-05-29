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
        ]

    }
});