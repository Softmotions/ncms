qx.Class.define("ncms.wsa.WSAPlaceholder", {
    extend: qx.ui.core.Widget,

    properties: {

        appearance: {
            refine: true,
            init: "ncms-wsa-placeholder"
        }

    },

    construct: function () {
        this.base(arguments);
        this._setLayout(new qx.ui.layout.VBox());
    }
});