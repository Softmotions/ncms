/**
 * Registry of available assembly attrubute managers.
 *
 * Require a set of custom AM components:
 */
qx.Class.define("ncms.asm.am.AsmAttrManagersRegistry", {
    type: "static",

    statics: {

        CLASSES: [
            ncms.asm.am.AliasAM,
            ncms.asm.am.StringAM,
            ncms.asm.am.SelectAM,
            ncms.asm.am.BooleanAM,
            ncms.asm.am.AsmRefAM,
            ncms.asm.am.WebRefAM,
            ncms.asm.am.FileRefAM,
            ncms.asm.am.PageRefAM,
            ncms.asm.am.TreeAM,
            ncms.asm.am.WikiAM,
            ncms.asm.am.ImageAM,
            ncms.asm.am.BreadCrumbsAM,
            ncms.asm.am.MainPageAM,
            ncms.asm.am.RichRefAM,
            ncms.asm.am.DateAM,
            ncms.asm.am.BumpOrdinalAM,
            ncms.asm.am.MedialineAM,
            ncms.asm.am.AsmCoreAM,
            ncms.asm.am.TableAM
        ],


        createAttrManagerInstanceForType: function (type) {
            var clazz = this.findEditorClassForType(type);
            if (clazz == null) {
                return null;
            }
            return this.createAttrManagerInstance(clazz);
        },


        createAttrManagerInstance: function (amClassName) {
            var clazz = (typeof amClassName === "string") ? qx.Class.getByName(amClassName) : amClassName;
            qx.core.Assert.assertTrue(clazz != null);
            qx.core.Assert.assertTrue(qx.Class.hasInterface(clazz, ncms.asm.IAsmAttributeManager));
            return sm.lang.Object.newInstance(clazz);
        },


        /**
         * Iterates over all attribute managers classes
         * in projection of supported types.
         *
         * The iteration process will be aborted
         * if `cb` function return `false` boolean value.
         *
         * @param cb {forEachAttributeManagerClassCB}
         *
         * @callback forEachAttributeManagerClassCB
         * @param type {String}
         * @param clazz {qx.Class}
         */
        forEachAttributeManagerTypeClassPair: function (cb) {
            sm.lang.Object.forEachClass(function (clazz) {
                if (!qx.Class.hasInterface(clazz, ncms.asm.IAsmAttributeManager) ||
                    typeof clazz.getMetaInfo !== "function" ||
                    typeof clazz.getDescription !== "function") {
                    return;
                }
                var meta = clazz.getMetaInfo() || {};
                var types = meta["attributeTypes"] || [];
                if (typeof types === "string") {
                    types = [types];
                }
                for (var i = 0; i < types.length; ++i) {
                    var type = types[i];
                    if (cb(type, clazz) === false) {
                        return false; //abort iteration
                    }
                }
            }, this);
        },


        /**
         * Find class of editor which supports the specified
         * attribute `type`.
         *
         * @param type {String} Type supported by editor
         * @return {qx.Class|null} Editor class for specified attribute type.
         */
        findEditorClassForType: function (type) {
            var ret = null;
            this.forEachAttributeManagerTypeClassPair(function (itype, clazz) {
                if (itype === type) {
                    ret = clazz;
                    return false;
                }
            });
            return ret;
        }

    }
});
