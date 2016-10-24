/**
 * Common page editor tab-pane code.
 *
 * Supports lazy loading of pane content.
 * Pane data can loaded on `loadPane` event.
 */
qx.Mixin.define("ncms.pgs.MPageEditorPane", {

    events: {
        /**
         * Fired if pane should be populated by page specific data
         *
         * Data: pageSpec property value.
         */
        "loadPane": "qx.event.type.Data",

        /**
         * Fired if pane should be cleared.
         */
        "clearPane": "qx.event.type.Event"
    },

    properties: {

        /**
         * pageSpec:
         * {
         *   id : {Number} Page ID,
         *   name : {String} Page name
         * }
         *
         * @see ncms.pgs.PageEditor
         */
        "pageSpec": {
            check: "Object",
            nullable: true,
            apply: "__applyPageSpec"
        },


        /**
         * If editor data is modified by user
         * and needs to be saved
         */
        modified: {
            check: "Boolean",
            nullable: false,
            init: false,
            event: "modified",
            apply: "_applyModified"
        }
    },

    construct: function () {

        this.addListener("appear", function () {
            this._appeared = true;
            if (this.__stateDeffered) {
                this.__applyPageSpec(this.getPageSpec());
            }
        }, this);

        this.addListener("disappear", function () {
            this._appeared = false;
        }, this);
    },

    members: {

        _appeared: false,

        __stateDeffered: false,

        __applyPageSpec: function (spec) {
            if (!this._appeared) {
                this.__stateDeffered = true;
                return;
            }
            this.__stateDeffered = false;
            if (spec != null) {
                this.fireDataEvent("loadPane", spec);
            } else {
                this.fireEvent("clearPane");
            }
        }
    }
});