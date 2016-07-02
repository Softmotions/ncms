/**
 * Helper methods used in
 */
qx.Mixin.define("ncms.asm.am.MAttributeManager", {

    members: {

        _optionsWidget: null,

        _valueWidget: null,


        _fetchAttributeValue: function (attrSpec, cb, cbContext) {
            if (attrSpec == null) {
                cb(null);
                return;
            }
            if (!attrSpec["hasLargeValue"]) {
                cb.call(cbContext, (attrSpec["value"] === undefined ? null : attrSpec["value"]));
                return;
            }
            if (attrSpec["largeValue"] != null) {
                cb.call(cbContext, (attrSpec["largeValue"] === undefined ? null : attrSpec["largeValue"]));
                return;
            }
            //make attribute value request
            var req = new sm.io.Request(
                ncms.Application.ACT.getRestUrl("asms.attribute", {
                    id: attrSpec["asmId"],
                    name: attrSpec["name"]
                }),
                "GET", "application/json");
            req.send(function (resp) {
                var attr = resp.getContent();
                var eval = attr["hasLargeValue"] ? attr["largeValue"] : attr["value"];
                attrSpec["largeValue"] = attr["largeValue"];
                attrSpec["value"] = attr["value"];
                cb.call(cbContext, (eval === undefined ? null : eval));
            });
        }
    },

    destruct: function () {
        this._optionsWidget = null;
        this._valueWidget = null;
    }
});