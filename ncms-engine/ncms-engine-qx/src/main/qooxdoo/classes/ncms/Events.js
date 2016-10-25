qx.Class.define("ncms.Events", {
    extend: qx.core.Object,
    type: "singleton",

    events: {

        /**
         * New page created
         *
         * Data:
         * {
         *   id:            {Number} Page ID
         *   navParentId:   {Number} Navigation parent ID
         *   name:          {String} Page assembly name (guid)
         *   hname:         {String} Page human name
         *   user:          {String} User initiates this event
         *   hints:         {Map} optional event hints
         * }
         *
         */
        "pageCreated": "qx.event.type.Data",

        /**
         * Page content edited.
         *
         * Data:
         * {
         *  id:     {Number} Page ID,
         *  user:   {String} User initiates this event
         *  hints:  {Map} optional event hints
         * }
         */
        "pageEdited": "qx.event.type.Data",


        /**
         * Page publish status changed.
         *
         * Data:
         * {
         *  id:        {Number} Page ID,
         *  published: {Boolean} Is page published
         *  user:      {String} User initiates this event
         *  hints:     {Map} optional event hints
         * }
         */
        "pageChangePublished": "qx.event.type.Data",

        /**
         * Page template changed.
         *
         * Data:
         * {
         *  id:         {Number} Page ID,
         *  templateId: {Number} Page template ID
         *  user:       {String} User initiates this event
         *  hints:      {Map} optional event hints
         * }
         *
         */
        "pageChangeTemplate": "qx.event.type.Data",

        /**
         * Page locked by user.
         *
         * {
         *   id:    {Number} Page ID,
         *   user:  {String} Lock owner
         *   hints: {Map} optional event hints
         * }
         */
        "pageLocked": "qx.event.type.Data",

        /**
         * User released lock on page.
         *
         * {
         *   id:    {Number} Page ID,
         *   user:  {String} Lock owner
         *   hints: {Map} optional event hints
         * }
         */
        "pageUnlocked": "qx.event.type.Data",

        /**
         * Basic assembly properties changed in the assembly editor GUI.
         */
        "asmPropsChanged": "qx.event.type.Data",

        /**
         * Marketing transfer tools rule properties changed
         */
        "mttRulePropsChanged": "qx.event.type.Data"
    },

    members: {

        attachAtmosphere: function (atm) {
            atm.removeListener("message", this.__onAtmosphereMessages, this);
            atm.addListener("message", this.__onAtmosphereMessages, this);
        },

        __onAtmosphereMessages: function (ev) {
            var app = ncms.Application.INSTANCE,
                uid = app.getUserId(),
                msg = ev.getData(),
                hints = msg.hints || {},
                mergeWith = qx.lang.Object.mergeWith;

            switch (msg.type) {
                case "AsmModifiedEvent":
                    if (hints["published"] != null) {
                        this.__fireDataEvent("pageChangePublished",
                            mergeWith({published: !!hints["published"]}, msg))
                    } else if (hints["template"] != null) {
                        this.__fireDataEvent("pageChangeTemplate",
                            mergeWith({templateId: hints["template"]}, msg))
                    } else {
                        this.__fireDataEvent("pageEdited", msg)
                    }
                    break;
                case "AsmLockedEvent":
                    this.__fireDataEvent("pageLocked", msg);
                    break;
                case "AsmUnlockedEvent":
                    this.__fireDataEvent("pageUnlocked", msg);
                    break;
                case "AsmCreatedEvent":
                    if (hints["page"]) {
                        this.__fireDataEvent("pageCreated", msg);
                    }
            }
        },

        __fireDataEvent: function (type, data) {
            console.log("event=" + type + " data=" + JSON.stringify(data));
            if (data.hints == null) {
                data.hints = {};
            }
            this.fireDataEvent(type, data);
        }
    }
});