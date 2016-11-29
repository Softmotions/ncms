/**
 * Media files utils
 */
qx.Class.define("ncms.mmgr.MediaFilesUtils", {
    extend: qx.core.Object,

    statics: {

        /**
         * Fetch meta media info for given file
         * identified by `id`
         *
         * @param locationOrId {Number|String} Media file id or file location
         * @param cb {Function} Callback with media info argument.
         * @param self {Object?null} This object for callback function.
         *
         */
        fetchMediaInfo: function (locationOrId, cb, self) {
            var req;
            if (typeof locationOrId === "number") {
                req = ncms.Application.request("media.meta", {
                    id: locationOrId
                });
            } else {
                req = ncms.Application.request("media.meta.by.path", {
                    path: locationOrId
                })
            }
            req.send(function (resp) {
                cb.call(self || null, resp.getContent());
            });
        }
    }
});