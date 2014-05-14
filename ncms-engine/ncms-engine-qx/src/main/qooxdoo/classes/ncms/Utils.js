/**
 * Various utility functions.
 */
qx.Class.define("ncms.Utils", {

    statics : {

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