qx.Class.define("ncms.asm.am.BreadCrumbsAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],

    statics: {

        __META:  {
            attributeTypes: "breadcrumbs",
            hidden: true,
            requiredSupported: false
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Breadcrumbs");
        },

        getMetaInfo: function () {
            return ncms.asm.am.BreadCrumbsAM.__META;
        }
    },

    members: {

        activateOptionsWidget: function () {
            return null;
        },

        optionsAsJSON: function () {
            return {};
        },

        activateValueEditorWidget: function () {
            return null;
        },

        valueAsJSON: function () {
            return {}
        }
    }
});