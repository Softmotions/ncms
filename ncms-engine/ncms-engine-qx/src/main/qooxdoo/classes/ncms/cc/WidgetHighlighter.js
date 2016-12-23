/**
 * Highlighter for widget
 */
qx.Class.define("ncms.cc.WidgetHighlighter", {
    extend: qx.core.Object,

    events: {},

    /**
     * Creates a highlighter for the passed widget.
     *
     * @param widget {qx.ui.core.Widget} Widget which should be added the highlighter
     */
    construct: function (widget) {
        this.base(arguments);
        this.__widget = widget;

        widget.addListener("resize", this.__onBoundsChange, this);
        widget.addListener("move", this.__onBoundsChange, this);
        widget.addListener("disappear", this.__onWidgetDisappear, this);
    },

    members: {

        __widget: null,

        __highlighter: null,
        
        __isActive: false,
        
        __onBoundsChange: function (e) {
            var data = e.getData();

            if (this.isActive()) {
                this._updateHighlighterBounds(data);
            }
        },

        __onWidgetAppear: function () {
            this._updateHighlighterBounds(this.__widget.getBounds());
            this.__widget.getContentElement().add(this.getHighlighterElement());
        },

        __onWidgetDisappear: function () {
            if (this.isActive()) {
                this.getHighlighterElement().getParent().remove(this.getHighlighterElement());
                this.__widget.addListenerOnce("appear", this.__onWidgetAppear, this);
            }
        },

        _updateHighlighterBounds: function (bounds) {
            this.getHighlighterElement().setStyles({
                width: bounds.width + "px",
                height: bounds.height + "px",
                left: bounds.left + "px",
                top: bounds.top + "px"
            });
        },

        getHighlighterElement: function () {
            if (!this.__highlighter) {
                var styles = {
                    position: "absolute",
                    opacity: 0.7,
                    backgroundColor: "#EFEFEF",
                    zIndex: 15,
                    outline: "5px dashed",
                    "outline-offset": "-5px"
                };
                this.__highlighter = new qx.html.Element("div", styles);
                this.__widget.getContentElement().add(this.__highlighter);
                this.__highlighter.exclude();

            }
            return this.__highlighter;
        },

        isActive: function () {
            return this.__isActive;
        },

        show: function () {
            if (this.isActive()) {
                return;
            }

            var bounds = this.__widget.getBounds();
            // no bounds -> widget not yet rendered -> bounds will be set on resize
            if (bounds) {
                this._updateHighlighterBounds(bounds);
            }

            var highlighter = this.getHighlighterElement();
            highlighter.include();
            highlighter.activate();
            
            this.__isActive = true;
        },

        hide: function () {
            if (!this.isActive()) {
                return;
            }

            var highlighter = this.getHighlighterElement();
            highlighter.exclude();
            
            this.__isActive = false;
        }
    },

    destruct: function () {
        this.__widget.removeListener("resize", this.__onBoundsChange, this);
        this.__widget.removeListener("move", this.__onBoundsChange, this);
        this.__widget.removeListener("disappear", this.__onWidgetDisappear, this);
        this.__widget = null;
        this._disposeObjects("__highlighter");
    }

});
