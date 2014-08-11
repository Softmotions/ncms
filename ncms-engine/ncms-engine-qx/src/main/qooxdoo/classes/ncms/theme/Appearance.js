/**
 * @asset(ncms/icon/16/actions/search.png)
 * @asset(ncms/icon/16/misc/cross-script.png)
 * @asset(qx/icon/${qx.icontheme}/16/actions/edit-clear.png)
 */

qx.Theme.define("ncms.theme.Appearance", {
    extend : qx.theme.simple.Appearance,
    include : [ sm.Appearance ],

    appearances : {

        "atom/label" : {
            include : "label",
            style : function(states) {
                return {
                    textColor : states.disabled ? "text-disabled" : "#444"
                };
            }
        },

        "button/label" : {
            include : "atom/label"
        },

        "table" : {
            style : function(states) {
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
                    decorator : decorator,
                    padding : padding,
                    textColor : textColor,
                    backgroundColor : states.disabled ? "background-disabled" : "white"
                };
            }
        },

        "splitpane/splitter" : {
            style : function(states) {
                return {
                    backgroundColor : "background"
                };
            }
        },

        "ncms-main-toolbar" : {
            alias : "toolbar",
            include : "toolbar",
            style : function(states) {
                return {
                    backgroundColor : "background"
                }
            }
        },

        "ncms-main-toolbar/part" : {
            include : "toolbar/part",

            style : function(states) {
                return {
                    margin : [0, 15, 0, 10]
                };
            }
        },

        "ncms-tree-am" : {
        },

        "ncms-tree-am/tree" : "virtual-tree",


        /* Assemblies selector */
        "ncms-asm-selector" : {
            style : function(states) {
                return {
                }
            }
        },

        /* Media files selector */
        "ncms-mf-selector" : {
            style : function(states) {
                return {
                }
            }
        },

        //empty placeholder on workspace pane
        "ncms-wsa-placeholder" : {
            style : function(states) {
                return {
                    backgroundColor : "background"
                }
            }
        },

        //form on workspace pane
        "ncms-wsa-form" : {
            style : function(states) {
                return {
                    padding : [10, 50, 10, 10],
                    maxWidth : 600
                }
            }
        },

        "wiki-editor-toolbar-button" : "toolbar-table-button",

        "wiki-editor-toolbar-menubutton" : "toolbar-table-menubutton",

        // message info popup (on top of page, auto hide)
        "ncms-info-popup" : {
            style : function(states) {
                return {
                    padding : [5, 10, 5, 10],
                    maxWidth : 350,
                    backgroundColor : "background"
                }
            }
        }
    }
});