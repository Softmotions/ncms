/**
 * Various utility functions.
 */
qx.Class.define("ncms.Utils", {

    statics : {

        /**
         * Checks if all access rights specified in the {@code pattern} are included
         * in the provided {@code accessMask}
         * @param accessMask {String} Access mask
         * @param pattern {String} Access mask rights to check.
         */
        checkAccessAll : function(accessMask, pattern) {
            if (pattern == null || accessMask == null) {
                return false;
            }
            var ok = true;
            for (var i = 0, l = pattern.length; i < l; ++i) {
                if (accessMask.indexOf(pattern.charAt(i)) === -1) {
                    ok = false;
                    break;
                }
            }
            return (ok && l > 0);
        },

        /**
         * Parse options string. Eg: 'foo=bar, foo2=bar2'
         * Returns key-value json map.
         *
         * @param spec {String}
         * @return {Object}
         */
        parseOptions : function(spec) {
            if (spec == null) {
                return {};
            }
            qx.core.Assert.assertString(spec);
            var res = {};
            var idx, sp1 = 0, sp2 = 0;
            var len = spec.length;
            var escaped = false;
            var part;
            while (sp1 < len) {
                idx = spec.indexOf(",", sp1);
                if (idx == -1) {
                    sp1 = len;
                } else {
                    if (idx > 0 && spec.charAt(idx - 1) == "\\") { //escaped delimeter ','
                        sp1 = idx + 1;
                        escaped = true;
                        continue;
                    }
                    sp1 = idx;
                }
                part = spec.substring(sp2, sp1);
                ++sp1;
                sp2 = sp1;
                idx = part.indexOf("=");
                if (idx != -1 && idx < len) {
                    if (escaped) {
                        res[part.substring(0, idx).replace("\\,", ",").trim()] =
                                part.substring(idx + 1).replace("\\,", ",").trim();
                        escaped = false;
                    } else {
                        res[part.substring(0, idx).trim()] = part.substring(idx + 1).trim();
                    }
                }
            }
            return res;
        },


        /**
         * Return true if given ctype is type of image.
         * @param ctype {String}
         * @returns {boolean}
         */
        isImageContentType : function(ctype) {
            if (ctype == null) {
                return false;
            }
            return (ctype.toString().indexOf("image/") == 0);
        },

        /**
         * Return true if given ctype is type of text document.
         * @param ctype {String}
         * @returns {boolean}
         */
        isTextualContentType : function(ctype) {
            if (ctype == null) {
                return false;
            }
            var cs = ctype.toString().trim();
            if (cs.indexOf("text/") == 0) {
                return true;
            }
            if (cs.indexOf("application/") == -1) {
                return false;
            }
            if (cs.indexOf(";") != -1) {
                cs = cs.substring(0, cs.indexOf(";")).trim();
            }
            return (this.TXT_CTYPES.indexOf(cs) != -1);
        },

        /**
         * Create page local folder path as elements array.
         * @param pageId {Number}
         * @returns {Array}
         */
        getPageLocalFolders : function(pageId) {
            var path = ["pages"];
            pageId = String(pageId);
            var node = [];
            for (var i = 0, l = pageId.length; i < l; ++i) {
                node.push(pageId.charAt(i));
                if (node.length == 3) {
                    path.push(node.join(""));
                    node.length = 0;
                }
            }
            if (node.length > 0) {
                path.push(node.join(""));
            }
            return path;
        }
    },

    defer : function(statics, members) {
        statics.TXT_CTYPES = [
            "application/atom+xml",
            "application/rdf+xml",
            "application/rss+xml",
            "application/soap+xml",
            "application/xop+xml",
            "application/xhtml+xml",
            "application/json",
            "application/javascript",
            "application/xml",
            "application/xml-dtd",
            "application/x-tex",
            "application/x-latex",
            "application/x-javascript",
            "application/ecmascript"
        ];
    }
});
