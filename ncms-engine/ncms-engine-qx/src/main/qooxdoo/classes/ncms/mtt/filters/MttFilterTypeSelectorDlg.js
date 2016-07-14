/**
 * Filter type selector
 */
qx.Class.define("ncms.mtt.filters.MttFilterTypeSelectorDlg", {
    extend: ncms.cc.TypesSelectorDlg,

    construct: function (caption) {
        this.base(arguments, caption || this.tr("Select a filter"), function (cb) {
            ncms.mtt.filters.MttFiltersRegistry.forEachMttFilterTypeClassPair(cb);
        });
    }
});