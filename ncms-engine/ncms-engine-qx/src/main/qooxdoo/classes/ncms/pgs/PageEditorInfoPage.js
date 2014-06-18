/**
 * Info page tab of page editor tabbox.
 *
 * @asset(ncms/icon/16/user/user-blue.png)
 */
qx.Class.define("ncms.pgs.PageEditorInfoPage", {
    extend : qx.ui.tabview.Page,
    include : [ ncms.pgs.MPageEditorPane ],


    construct : function() {
        this.base(arguments, this.tr("General"));
        this.setLayout(new qx.ui.layout.VBox(4));

        //Page name
        this.__pageNameLabel = new qx.ui.basic.Label();
        this.__pageNameLabel.setFont("headline");
        this.add(this.__pageNameLabel);

        //Optional warning/alert box
        this.__alertBox = new sm.ui.AlertBox();
        this.add(this.__alertBox);
        this.__alertBox.exclude();

        //Page mdate label
        this.__mdateLabel = new qx.ui.basic.Label();
        this.add(this.__mdateLabel);

        //Page owner
        this.__ownerSelector =
                new sm.ui.form.ButtonField(this.tr("Owner"),
                        "ncms/icon/16/user/user-blue.png",
                        true);
        this.__ownerSelector.setReadOnly(true);
        this.__ownerSelector.addListener("execute", this.__chooseOwner, this);
        this.add(this.__ownerSelector);

        this.addListener("loadPane", this.__onLoadPane, this);
    },

    members : {

        /**
         * Page name label
         */
        __pageNameLabel : null,

        /**
         * Modification date
         */
        __mdateLabel : null,

        /**
         * Optional warning/alert box
         */
        __alertBox : null,

        /**
         * Owner
         */
        __ownerSelector : null,

        /**
         * Extra page info
         */
        __info : null,

        __onLoadPane : function(ev) {
            var spec = ev.getData();
            this.__spec = spec;

            this.__pageNameLabel.setValue(spec["name"]);

            var req = new sm.io.Request(ncms.Application.ACT.getRestUrl("pages.info", spec),
                    "GET", "application/json");
            req.send(function(resp) {
                var info = resp.getContent();
                this.__info = info;
                if (info["mdate"] != null) {
                    this.__mdateLabel.setValue(this.tr("Last modification: %1, %2",
                                    ncms.Application.formatDate(info["mdate"]),
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

                this.__setOwner(info)

            }, this);
        },

        __setOwner : function(info) {
            var owner = info["owner"];
            var am = info["accessmask"] || "r";
            this.__ownerSelector.setMainButtonEnabled(am.indexOf("s") !== -1);
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


        __chooseOwner : function() {
            var info = this.__info;
            if (info == null) {
                return;
            }
            var dlg = new ncms.usr.UserSelectorDlg(
                    (info["name"] != null) ?
                    this.tr("Choose the owner of %1", info["name"]) :
                    this.tr("Choose the owner")
            );
            dlg.addListener("completed", function(ev) {
                var user = ev.getData()[0];
                qx.log.Logger.info("User choosen: " + JSON.stringify(user));
                var req = new sm.io.Request(
                        ncms.Application.ACT.getRestUrl("pages.owner",
                                {id : info["id"], owner : user["name"]}
                        ), "PUT", "application/json");
                req.send(function(resp) {
                    resp = resp.getContent();
                    qx.lang.Object.mergeWith(this.__info, resp, true);
                    qx.log.Logger.info("info=" + JSON.stringify(this.__info));
                    this.__setOwner(this.__info);
                    dlg.close();
                }, this);
            }, this);
            dlg.open();
        }
    },

    destruct : function() {
        this.__ownerSelector = null;
        this.__pageNameLabel = null;
        this.__mdateLabel = null;
        this.__alertBox = null;
        this.__info = null;
        //this._disposeObjects("__field_name");
    }
});