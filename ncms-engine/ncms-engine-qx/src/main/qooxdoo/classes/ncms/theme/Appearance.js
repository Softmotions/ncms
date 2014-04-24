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

        /////////////////////////////////////////////////////////////
        //                   NCMS components

        "ncms-wsa-placeholder" : {
            style : function(states) {
                return {
                    backgroundColor : "#DCDCDC"
                }
            }
        },

        //////////////////////////////////////////////////////////////
        //                    SM components

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