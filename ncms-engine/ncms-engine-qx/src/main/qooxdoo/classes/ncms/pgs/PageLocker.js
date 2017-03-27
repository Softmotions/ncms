/**
 * Page user locks manager.
 */
qx.Class.define("ncms.pgs.PageLocker", {
    extend: qx.core.Object,
    type: "singleton",
    include: [qx.locale.MTranslation, qx.core.MAssert],

    members: {

        /**
         * Acquire page lock.
         */
        lock: function (opts, cb, self) {
            this.assertTrue(!!(opts && opts["id"]), "Invalid options");
            var req = ncms.Application.request("pages.lock", {id: opts["id"]}, "PUT", "application/json");
            req.addListenerOnce("finished", function(ev) {
                (ev.getData() && cb && cb.call(self, {success: false}));
            });
            req.send(function (resp) {
                var c = resp.getContent();
                if (opts["reportErrors"] && !c["success"]) {
                    ncms.Application.errorPopup(this.tr("Page locked by another user: %1", c["locker"]));
                }
                (cb && cb.call(self, c));
            }, this);

        },

        /**
         * Unlock page.
         */
        unlock: function (opts, cb, self) {
            this.assertTrue(!!(opts && opts["id"]), "Invalid options");
            var req = ncms.Application.request("pages.unlock", {id: opts["id"]}, "PUT", "application/json");
            req.addListenerOnce("finished", function(ev) {
                (ev.getData() && cb && cb.call(self, {success: false}));
            });
            req.send(function(resp) {
                var c = resp.getContent();
                if (opts["reportErrors"] && !c["success"]) {
                    ncms.Application.errorPopup(this.tr("Failed to unlock page"));
                }
                (cb && cb.call(self, c));
            }, this);
        }
    }
});