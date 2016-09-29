qx.Class.define("ncms.asm.am.BumpOrdinalAM", {
    extend: ncms.asm.am.BooleanAM,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "bump",
            hidden: false,
            requiredSupported: false
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Bump page ordinal");
        },

        getMetaInfo: function () {
            return ncms.asm.am.BumpOrdinalAM.__META;
        }
    },


    members: {

        activateOptionsWidget: function (attrSpec, asmSpec) {
            return null;
        },

        optionsAsJSON: function () {
            return {};
        }
    }
});