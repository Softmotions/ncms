/**
 * Filter type selector
 */
qx.Class.define("ncms.mtt.actions.MttActionTypeSelectorDlg", {
    extend: ncms.cc.TypesSelectorDlg,

    construct: function (caption) {
        this.base(arguments, caption || this.tr("Select an action"), function (cb) {
            ncms.mtt.actions.MttActionsRegistry.forEachMttActionTypeClassPair(cb);
        });
    }
});