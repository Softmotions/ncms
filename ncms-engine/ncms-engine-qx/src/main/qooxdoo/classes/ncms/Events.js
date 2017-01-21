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
         *   hints:         {Map} optional event hints.
         *                  app - Application UUID
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
         *           moveTargetId - optional target page id if page has been moved,
         *                           0 (zero) if page moved into root.
         *           app - Application UUID
         * }
         */
        "pageEdited": "qx.event.type.Data",

        /**
         * Page removed.
         *
         * Data:
         * {
         *  id:     {Number} Page ID,
         *  user:   {String} User initiates this event
         *  hints:  {Map} optional event hints,
         *          app - Application UUID
         * }
         */
        "pageRemoved": "qx.event.type.Data",

        /**
         * Page publish status changed.
         *
         * Data:
         * {
         *  id:        {Number} Page ID,
         *  published: {Boolean} Is page published
         *  user:      {String} User initiates this event
         *  hints:     {Map} optional event hints,
         *             app - Application UUID
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
         *              app - Application UUID
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
        "mttRulePropsChanged": "qx.event.type.Data",

        /**
         * Created/update media repository item.
         *
         * Data:
         * {
         *   id:        {Number}  Media item ID
         *   isFolder   {Boolean} True if this media item is a folder
         *   path:      {String}  Media repository path to this media item
         *   user:      {String}  User initiates this event
         * }
         *
         */
        "mediaUpdated": "qx.event.type.Data",

        /**
         * Removed media repository item.
         *
         * Data:
         * {
         *   id:        {Number}  Media item ID.
         *   isFolder   {Boolean} True if this media item is a folder
         *   path:      {String}  Media repository path to this media item
         *   user:      {String}  User initiates this event
         * }
         */
        "mediaRemoved": "qx.event.type.Data"
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
                    break;
                case "AsmRemovedEvent":
                    this.__fireDataEvent("pageRemoved", msg); // we are not using page hints
                    break;
                case "MediaUpdateEvent":
                    this.__fireDataEvent("mediaUpdated", msg);
                    break;
                case "MediaDeleteEvent":
                    this.__fireDataEvent("mediaRemoved", msg);
                    break;
                case "ServerMessageEvent":
                    if (msg.hints["app"] === ncms.Application.UUID) {
                        ((msg.error)
                            ? sm.alert.Alerts.errorPopup
                            : sm.alert.Alerts.infoPopup)(
                            qx.lang.String.stripTags(msg.message).replace(/[\n\r]/g, "<br>"), {
                                showTime: msg.persistent ? Number.MAX_VALUE : null
                            }
                        );
                    }
                    break;
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