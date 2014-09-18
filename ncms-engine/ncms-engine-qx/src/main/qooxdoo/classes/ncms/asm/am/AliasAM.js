/**
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.asm.am.AliasAM", {
    extend : qx.core.Object,
    implement : [ ncms.asm.IAsmAttributeManager ],
    include : [ qx.locale.MTranslation, ncms.asm.am.MAttributeManager ],

    statics : {

        getDescription : function() {
            return qx.locale.Manager.tr("Alias");
        },

        getSupportedAttributeTypes : function() {
            return [ "alias" ];
        },

        isHidden : function() {
            return false;
        }
    },

    members : {
        activateOptionsWidget : function(attrSpec, asmSpec) {
        },

        optionsAsJSON : function() {
            return {};
        },

        activateValueEditorWidget : function(attrSpec, asmSpec) {
            var w = new qx.ui.form.TextField();
            this._fetchAttributeValue(attrSpec, function(val) {
                w.setValue(val);
            });
            w.setRequired(!!attrSpec["required"]);

            this._valueWidget = w;
            return w;
        },

        valueAsJSON : function() {
            if (this._valueWidget == null) {
                return;
            }
            return {
                value : this._valueWidget.getValue()
            }
        }
    },

    destruct : function() {
    }
});