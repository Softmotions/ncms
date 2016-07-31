/**
 * Http header filter
 */
qx.Class.define("ncms.mtt.filters.MttHeadersFilter", {
    extend: ncms.mtt.filters.MttParametersFilter,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Request headers");
        },

        getType: function () {
            return "headers";
        },

        specForHuman: function (spec) {
            return ncms.mtt.filters.MttParametersFilter.specForHuman(spec);
        }
    },

    members: {

        _getRulesLabel: function() {
            return this.tr("Headers filtering rules")
        },

        _getDescriptionLabel: function() {
            return this.tr(
                "Filter rules on each line:%1 <b>?</b> at end of param name means this header is not required",
                "<pre>" +
                "  header_name=glob_mask<br>" +
                "  header_name?=glob_mask<br>" +
                "</pre>");
        }
    },

    destruct: function () {

    }
});