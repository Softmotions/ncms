/**
 * @asset(ncms/icon/16/actions/search.png)
 * @asset(qx/icon/${qx.icontheme}/16/actions/edit-clear.png)
 */

qx.Theme.define("ncms.theme.Appearance", {
    extend : qx.theme.simple.Appearance,
    appearances : {

        /* Assemblies selector */
        "asm-selector" : {
            style : function(states) {
                return {
                }
            }
        },

        /* Media files selector */
        "mf-selector" : {
            style : function(states) {
                return {
                }
            }
        },

        /////////////////////////////////////////////////////////////
        //                   NCMS components


        //empty placeholder on workspace pane
        "ncms-wsa-placeholder" : {
            style : function(states) {
                return {
                    backgroundColor : "#dcdcdc"
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

        //////////////////////////////////////////////////////////////
        //                    SM components

        "toolbar-table" : "widget",

        "toolbar-table/toolbar" : "toolbar",

        "toolbar-table/part" : {
            include : "toolbar/part",
            alias : "toolbar/part",
            style : function(states) {
                return {
                    margin : [0, 10, 0, 0]
                }
            }
        },

        "toolbar-table-button" : {
            alias : "toolbar-button",
            include : "toolbar-button",
            style : function(states) {
                return {
                    margin : 0
                };
            }
        },


        "sm-bt-field" : {
        },

        "sm-bt-field/button" : {
            include : "button",
            alias : "button",
            style : function(states) {
                return {
                }
            }
        },

        "sm-bt-field/text" : {
            include : "textfield",
            alias : "textfield",
            style : function(states) {
                return {
                }
            }
        },

        "sm-search-field" : {
            style : function(states) {
                var decorator;
                var padding;
                if (states.disabled) {
                    decorator = "inset";
                    padding = [2, 3];
                } else if (states.invalid) {
                    decorator = "border-invalid";
                    padding = [1, 2];
                } else if (states.focused) {
                    decorator = "focused-inset";
                    padding = [1, 2];
                } else {
                    decorator = "inset";
                    padding = [2, 3];
                }
                return {
                    decorator : decorator,
                    padding : padding
                };
            }
        },

        "sm-search-field/options" : {
            include : "atom",
            alias : "atom",
            style : function(states) {
                return {
                    padding : [0, 0, 0, 4]
                }
            }
        },

        "sm-search-field/text" : {
            include : "textfield",
            style : function(states) {
                return {
                    padding : [2, 3],
                    decorator : null
                }
            }
        },

        "sm-search-field/clear" : {
            include : "atom",
            alias : "atom",
            style : function(states) {
                return {
                    icon : "icon/16/actions/edit-clear.png",
                    padding : [0, 0, 0, 0]
                }
            }
        }
    }
});