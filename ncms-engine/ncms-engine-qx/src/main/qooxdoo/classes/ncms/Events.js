qx.Class.define("ncms.Events", {
    extend: qx.core.Object,
    type: "singleton",

    events: {

        /**
         * Page content edited.
         *
         * Data:
         * {
         *  id : {Number} Page ID,
         * }
         */
        "pageEdited": "qx.event.type.Data",


        /**
         * Page publish status changed.
         *
         * Data:
         * {
         *  id : {Number} Page ID,
         *  published : {Boolean} Is page published
         * }
         */
        "pageChangePublished": "qx.event.type.Data",


        /**
         * Page template changed.
         *
         * Data:
         * {
         *  id : {Number} Page ID,
         *  templateId : {Number} Page template ID
         * }
         *
         */
        "pageChangeTemplate": "qx.event.type.Data",


        /**
         * Basic assembly properties changed in the assembly editor GUI.
         */
        "asmPropsChanged": "qx.event.type.Data",


        /**
         * Marketing transfer tool rule properties changed
         */
        "mttRulePropsChanged": "qx.event.type.Data"
    },


    members: {

        attachAtmosphere: function (atm) {
            atm.removeListener("message", this.__onAtmosphereMessages, this);
            atm.addListener("message", this.__onAtmosphereMessages, this);
        },

        __onAtmosphereMessages: function (ev) {
            var app = ncms.Application.INSTANCE;
            var uid = app.getUserId();
            var msg = ev.getData();
            var hints = msg.hints || {};
            console.log("msg: " + JSON.stringify(msg));
            switch (msg.type) {
                case "AsmModifiedEvent":
                    if (hints["published"] != null) {
                        this.fireDataEvent("pageChangePublished",
                            qx.lang.Object.mergeWith({published: !!hints["published"]}, msg))
                    } else {
                        this.fireDataEvent("pageEdited", msg)
                    }
                    break;
            }
        }
    }
});