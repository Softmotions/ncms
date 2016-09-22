/* ************************************************************************

 ************************************************************************ */

qx.Theme.define("ncms.theme.Decoration", {
    extend: qx.theme.simple.Decoration,

    decorations: {

        "tooltip-error": {
            style: {
                backgroundColor: "tooltip-error",
                radius: 4,
                shadowColor: "shadow",
                shadowBlurRadius: 2,
                shadowLength: 1
            }
        },

        "separator-bottom-vertical": {
            style: {
                widthBottom: 1,
                colorBottom: "border-separator"
            }
        }
    }
});