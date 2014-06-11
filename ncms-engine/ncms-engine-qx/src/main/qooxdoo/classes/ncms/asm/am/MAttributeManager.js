/**
 * Helper methods used in
 */
qx.Mixin.define("ncms.asm.am.MAttributeManager", {

    members : {

        _fetchAttributeValue : function(attrSpec, cb) {
            if (attrSpec == null) {
                cb(null);
                return;
            }
            if (!attrSpec["hasLargeValue"]) {
                cb(attrSpec["value"] === undefined ? null : attrSpec["value"]);
                return;
            }
            //make attribute value request
            var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("asms.attribute"),
                    "GET", "application/json");
            req.send(function(resp) {
                var attr = resp.getContent();
                var eval = attr["hasLargeValue"] ? attr["largeValue"] : attr["value"];
                cb(eval === undefined ? null : eval);
            });
        }
    }
});