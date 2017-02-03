/**
 * Main(index) page marker template attribute.
 */
qx.Class.define("ncms.asm.am.MainPageAM", {
    extend: qx.core.Object,
    implement: [ncms.asm.IAsmAttributeManager],
    include: [qx.locale.MTranslation, ncms.asm.am.MAttributeManager],

    statics: {

        __META: {
            attributeTypes: "mainpage",
            hidden: false,
            requiredSupported: false
        },

        getDescription: function () {
            return qx.locale.Manager.tr("Main page marker");
        },

        getMetaInfo: function () {
            return ncms.asm.am.MainPageAM.__META;
        }
    },

    members: {

        _form: null,

        activateOptionsWidget: function (attrSpec, asmSpec) {
            return this._form = new ncms.asm.am.MainPageAMOptionsWidget(attrSpec, asmSpec);
        },

        optionsAsJSON: function () {
            return this._form._optionsAsJSON();
        },

        activateValueEditorWidget: function (attrSpec, asmSpec) {
            return this._form = new ncms.asm.am.MainPageAMOptionsWidget(attrSpec, asmSpec);
        },

        valueAsJSON: function () {
            return this._form._optionsAsJSON();
        }
    },

    destruct: function () {
        this._disposeObjects("_form");
    }

});