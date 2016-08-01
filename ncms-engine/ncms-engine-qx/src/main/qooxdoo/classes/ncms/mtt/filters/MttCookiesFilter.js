/**
 * Http cookies filter
 */
qx.Class.define("ncms.mtt.filters.MttCookiesFilter", {
    extend: ncms.mtt.filters.MttParametersFilter,
    implement: [ncms.mtt.filters.IMttFilter],
    include: [qx.locale.MTranslation],

    statics: {

        getDescription: function () {
            return qx.locale.Manager.tr("Request cookies");
        },

        getType: function () {
            return "cookies";
        },

        specForHuman: function (spec) {
            return ncms.mtt.filters.MttParametersFilter.specForHuman(spec);
        }
    },

    members: {

        _getRulesLabel: function () {
            return this.tr("Cookies filtering rules")
        },

        _getDescriptionLabel: function () {
            return this.tr(
                "Filter rules on each line:%1 <b>?</b> at end of param name means this cookie is not required",
                "<pre>" +
                "  cookie_name   = glob_mask<br>" +
                "  cookie_name? != glob_mask<br>" +
                "</pre>");
        }
    },

    destruct: function () {
    }

});
