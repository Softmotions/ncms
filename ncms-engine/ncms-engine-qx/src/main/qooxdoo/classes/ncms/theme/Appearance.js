/**
 * @asset(ncms/icon/16/actions/search.png)
 * @asset(ncms/icon/16/misc/cross-script.png)
 * @asset(qx/icon/${qx.icontheme}/16/actions/edit-clear.png)
 * @asset(ncms/icon/22/places/folder.png)
 * @asset(ncms/icon/22/places/folder-open.png)
 * @asset(ncms/icon/16/misc/cross.png)
 * @asset(qx/decoration/Modern/form/tooltip-error-arrow-right.png)
 * @asset(qx/decoration/Modern/form/tooltip-error-arrow.png)
 */

qx.Theme.define("ncms.theme.Appearance", {
    extend: qx.theme.simple.Appearance,
    include: [sm.Appearance],

    appearances: {

        "toolbar": {
            style: function (states) {
                return {
                    backgroundColor: "#ededed",
                    padding: 0
                };
            }
        },

        "window/captionbar": {
            style: function (states) {
                return {
                    backgroundColor: states.active ? "background" : "background-disabled",
                    padding: [8, 8, 0, 8],
                    font: "bold"
                };
            }
        },


        "virtual-tree": {
            include: "tree",
            alias: "tree",

            style: function (states) {
                return {
                    itemHeight: 24
                };
            }
        },


        "tree-folder": {
            style: function (states) {
                var backgroundColor;
                if (states.selected) {
                    backgroundColor = "background-selected";
                    if (states.disabled) {
                        backgroundColor += "-disabled";
                    }
                }
                return {
                    padding: [0, 8, 0, 5],
                    icon: states.opened ? "ncms/icon/22/places/folder-open.png" : "ncms/icon/22/places/folder.png",
                    backgroundColor: backgroundColor,
                    iconOpened: "ncms/icon/22/places/folder-open.png",
                    opacity: (states.drag || states.locked) ? 0.5 : undefined
                };
            }
        },

        "tree-folder/extra": "tree-folder/label",

        "tree-folder/extra2": "tree-folder/label",

        "tree-folder/weight": "spinner",

        "tree-file": {
            include: "tree-folder",
            alias: "tree-folder",

            style: function (states) {
                return {
                    icon: "icon/16/mimetypes/text-plain.png",
                    opacity: (states.drag || states.locked) ? 0.5 : undefined
                };
            }
        },


        "tree-folder/icon": {
            include: "image",
            style: function (states) {
                return {
                    padding: [0, 4, 0, 0]
                };
            }
        },

        "toolbar-table/toolbar": {
            include: "toolbar",
            alias: "toolbar",

            style: function (states) {
                return {
                    backgroundColor: "#ededed",
                    padding: 0
                };
            }

        },

        "atom/label": {
            include: "label",
            style: function (states) {
                return {
                    textColor: states.disabled ? "text-disabled" : "#444"
                };
            }
        },

        "button/label": {
            include: "atom/label"
        },

        "table": {
            style: function (states) {
                var textColor;
                if (states.disabled) {
                    textColor = "text-disabled";
                } else if (states.showingPlaceholder) {
                    textColor = "text-placeholder";
                } else {
                    textColor = undefined;
                }
                var decorator;
                var padding;
                if (states.disabled) {
                    decorator = "inset";
                    padding = [1, 1];
                } else if (states.invalid) {
                    decorator = "border-invalid";
                    padding = [0, 0];
                } else if (states.focused) {
                    decorator = "focused-inset";
                    padding = [0, 0];
                } else {
                    padding = [1, 1];
                    decorator = "inset";
                }
                return {
                    decorator: decorator,
                    padding: padding,
                    textColor: textColor,
                    backgroundColor: states.disabled ? "background-disabled" : "white"
                };
            }
        },

        "splitpane/splitter": {
            style: function (states) {
                return {
                    backgroundColor: "background"
                };
            }
        },

        "sm-search-field/clear": {
            include: "atom",
            alias: "atom",
            style: function (states) {
                return {
                    icon: "ncms/icon/16/misc/cross.png",
                    padding: 0
                }
            }
        },

        "ncms-main-toolbar": {
            alias: "toolbar",
            include: "toolbar",
            style: function (states) {
                return {
                    backgroundColor: "background"
                }
            }
        },

        "ncms-main-toolbar/part": {
            include: "toolbar/part",

            style: function (states) {
                return {
                    margin: [0, 15, 0, 10]
                };
            }
        },

        "ncms-tree-am": {},

        "ncms-tree-am/tree": "virtual-tree",


        /* Assemblies selector */
        "ncms-asm-selector": {
            style: function (states) {
                return {}
            }
        },

        /* Media files selector */
        "ncms-mf-selector": {
            style: function (states) {
                return {}
            }
        },

        /* Mtt rules selector */
        "ncms-mtt-rules-selector": {
            style: function (states) {
                return {}
            }
        },

        /* Mtt tracking pixels selector */
        "ncms-mtt-tp-selector": {
            style: function (states) {
                return {}
            }
        },

        //empty placeholder on workspace pane
        "ncms-wsa-placeholder": {
            style: function (states) {
                return {
                    backgroundColor: "background"
                }
            }
        },

        //form on workspace pane
        "ncms-wsa-form": {
            style: function (states) {
                return {
                    padding: [10, 50, 10, 10],
                    maxWidth: 700
                }
            }
        },

        "wiki-editor-toolbar-button": "toolbar-table-button",

        "wiki-editor-toolbar-menubutton": "toolbar-table-menubutton",

        // message info popup (on top of page, auto hide)
        "ncms-info-popup": {
            style: function (states) {
                return {
                    padding: [5, 10, 10, 10],
                    maxWidth: 350,
                    backgroundColor: "#FFFFBF"
                }
            }
        },
        /*
         ---------------------------------------------------------------------------
         TOOL TIP
         ---------------------------------------------------------------------------
         */

        "tooltip/atom": "atom",

        "tooltip-error": {
            style: function (states) {
                return {
                    placeMethod: "widget",
                    offset: [-3, 1, 0, 0],
                    arrowPosition: states.placementLeft ? "left" : "right",
                    position: "right-top",
                    showTimeout: 100,
                    hideTimeout: 10000,
                    padding: [0, 4, 4, 0]
                };
            }
        },

        "tooltip-error/arrow": {
            include: "image",

            style: function (states) {
                var source = states.placementLeft ?
                             "qx/decoration/Modern/form/tooltip-error-arrow-right.png" : "qx/decoration/Modern/form/tooltip-error-arrow.png";
                return {
                    source: source,
                    padding: [6, 0, 0, 0],
                    zIndex: 10000001
                };
            }
        },

        "tooltip-error/atom": {
            include: "popup",

            style: function (states) {
                return {
                    textColor: "text-selected",
                    backgroundColor: undefined,
                    decorator: "tooltip-error",
                    font: "tooltip-error",
                    padding: [3, 4, 4, 4],
                    margin: [1, 0, 0, 0],
                    maxWidth: 333
                };
            }
        }
    }
});