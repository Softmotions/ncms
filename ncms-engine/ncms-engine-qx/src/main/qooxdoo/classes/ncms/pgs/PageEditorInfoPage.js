/**
 * Info page tab of page editor tabbox.
 *
 * @asset(ncms/icon/16/user/user-blue.png)
 * @asset(ncms/icon/32/home-page.png)
 */
qx.Class.define("ncms.pgs.PageEditorInfoPage", {
    extend: qx.ui.tabview.Page,
    include: [ncms.pgs.MPageEditorPane],

    construct: function () {
        this.base(arguments, this.tr("General"));
        this.setLayout(new qx.ui.layout.VBox(4));

        //Page name
        this.__pageNameLabel = new qx.ui.basic.Label();
        this.__pageNameLabel.setFont("headline");
        this.add(this.__pageNameLabel);

        //Optional warning/alert box
        this.__alertBox = new sm.ui.AlertBox();
        this.__alertBox.setRich(true);
        this.add(this.__alertBox);
        this.__alertBox.exclude();

        //Second optional warning/alert box (page status)
        this.__statusBox = new sm.ui.AlertBox();
        this.__statusBox.setRich(true);
        this.add(this.__statusBox);
        this.__statusBox.exclude();

        //Page mdate label
        this.__mdateLabel = new qx.ui.basic.Label();
        this.__mdateLabel.set({rich: true, wrap: true});
        this.add(this.__mdateLabel);

        var hcont = new qx.ui.container.Composite(new qx.ui.layout.HBox(5));

        var bt = new qx.ui.form.Button(null, "ncms/icon/16/misc/monitor.png");
        bt.setToolTipText(this.tr("Preview"));
        bt.addListener("execute", function () {
            var pp = ncms.Application.ACT.getRestUrl("pages.preview", this.getPageSpec());
            qx.bom.Window.open(pp, "NCMS:Preview");
        }, this);
        hcont.add(bt);

        //Page owner
        this.__ownerSelector =
            new sm.ui.form.ButtonField(this.tr("Owner"),
                "ncms/icon/16/user/user-blue.png",
                true);
        this.__ownerSelector.setReadOnly(true);
        this.__ownerSelector.addListener("execute", this.__chooseOwner, this);
        hcont.add(this.__ownerSelector, {flex: 1});
        this.add(hcont);

        this.addListener("loadPane", this.__onLoadPane, this);

        this.__previewFrame = new sm.ui.embed.ScaledIframe().set({fitWidth: true});
        this.__previewFrame.setPadding([10, 15, 5, 15]);
        this.add(this.__previewFrame, {flex: 1});

        ncms.Events.getInstance().addListener("pageEdited", this.__onPageEdited, this);
        ncms.Events.getInstance().addListener("pageChangePublished", this.__onPageEdited, this);
        ncms.Events.getInstance().addListener("pageChangeTemplate", this.__onPageEdited, this);
    },

    members: {

        __pendingRefresh: false,

        __previewFrame: null,

        /**
         * Page name label
         */
        __pageNameLabel: null,

        /**
         * Modification date
         */
        __mdateLabel: null,

        /**
         * Page status box
         */
        __statusBox: null,

        /**
         * Optional warning/alert box
         */
        __alertBox: null,

        /**
         * Owner
         */
        __ownerSelector: null,

        /**
         * Extra page info
         */
        __info: null,

        __onPageEdited: function (ev) {
            var myspec = this.getPageSpec();
            var evspec = ev.getData();
            if (myspec != null
                && evspec != null
                && myspec["id"] === evspec["id"]
                && (evspec.hints["app"] !== ncms.Application.UUID || !evspec.hints["veditor"])) {
                this.__reload();
            }
        },

        __reload: function () {
            var spec = this.getPageSpec();
            if (spec != null) {
                this.setPageSpec(sm.lang.Object.shallowClone(spec));
            }
        },

        __onLoadPane: function (ev) {
            var spec = ev.getData();
            this.__pageNameLabel.setValue(spec["name"]);
            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.info", spec),
                "GET", "application/json");
            req.send(function (resp) {
                var info = resp.getContent();
                this.__info = info;
                //"indexPage":{"virtualHosts":["localhost"],"langCodes":["*"]}
                if (info["indexPage"] != null) {
                    var ip = info["indexPage"];
                    var msg = [];
                    if (Array.isArray(ip["virtualHosts"])) {
                        msg.push("<b>" + this.tr("Virtual hosts") + ":</b>");
                        msg.push(ip["virtualHosts"]
                        .map(function (vh) {
                            // todo HTTP port?
                            return "<a href='http://" + vh + "' target='_blank'>" + vh + "</a>";
                        }).join(" "));
                    }
                    if (Array.isArray(ip["langCodes"])) {
                        msg.push("<br><b>" + this.tr("Languages") + ":</b>");
                        msg.push(ip["langCodes"].join(", "));
                    }
                    this.__statusBox.setLabel(msg.join(" "));
                    this.__statusBox.setIcon("ncms/icon/32/home-page.png");
                    this.__statusBox.show();
                } else {
                    this.__statusBox.exclude();
                }

                if (info["mdate"] != null) {
                    this.__mdateLabel.setValue(this.tr("Last modification: %1, %2",
                        ncms.Application.formatDateTime(info["mdate"]),
                        info["muser"] ? info["muser"]["fullName"] : "-")
                    );
                } else {
                    this.__mdateLabel.setValue("");
                }

                if (!info["published"]) {
                    this.__alertBox.setLabel(this.tr("This page is not pulished yet"));
                    this.__alertBox.show();
                } else {
                    this.__alertBox.exclude();
                }
                this.__setOwner(info);
            }, this);

            this.__initPreview(spec);
        },

        __initPreview: function (spec) {
            if (spec == null) {
                return;
            }
            var pp = ncms.Application.ACT.getRestUrl("pages.preview.frame", spec);
            if (pp == this.__previewFrame.getSource()) {
                this.__previewFrame.resetSource();
            }
            this.__previewFrame.setSource(pp);
        },

        __setOwner: function (info) {
            var owner = info["owner"];
            var am = info["accessMask"] || "r";
            this.__ownerSelector.setMainButtonEnabled(am.indexOf("o") !== -1);
            if (owner == null) {
                this.__ownerSelector.setValue("");
                return;
            }
            var label = [];
            if (owner["name"] != null) {
                label.push(owner["name"]);
            }
            if (owner["fullName"] != null) {
                label.push("|");
                label.push(owner["fullName"]);
            }
            this.__ownerSelector.setValue(label.join(" "));
        },

        __chooseOwner: function () {
            var info = this.__info;
            if (info == null) {
                return;
            }
            var dlg = new ncms.usr.UserSelectorDlg(
                (info["name"] != null) ?
                this.tr("Choose the owner of %1", info["name"]) :
                this.tr("Choose the owner")
            );
            dlg.addListener("completed", function (ev) {
                var user = ev.getData()[0];
                qx.log.Logger.info("User choosen: " + JSON.stringify(user));
                var req = new sm.io.Request(
                    ncms.Application.ACT.getRestUrl("pages.owner",
                        {id: info["id"], owner: user["name"]}
                    ), "PUT", "application/json");
                req.send(function (resp) {
                    resp = resp.getContent();
                    qx.lang.Object.mergeWith(this.__info, resp, true);
                    this.__setOwner(this.__info);
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        },

        _applyModified: function (val) {
        }
    },

    destruct: function () {
        this.__ownerSelector = null;
        this.__pageNameLabel = null;
        this.__mdateLabel = null;
        this.__alertBox = null;
        this.__statusBox = null;
        this.__info = null;
        ncms.Events.getInstance().removeListener("pageEdited", this.__onPageEdited, this);
        ncms.Events.getInstance().removeListener("pageChangePublished", this.__onPageEdited, this);
        ncms.Events.getInstance().removeListener("pageChangeTemplate", this.__onPageEdited, this);
        //this._disposeObjects("__field_name");
    }
});