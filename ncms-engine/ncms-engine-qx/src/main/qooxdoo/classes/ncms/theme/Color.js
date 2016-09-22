/* ************************************************************************

 ************************************************************************ */

qx.Theme.define("ncms.theme.Color", {
    extend: qx.theme.simple.Color,

    colors: {
        "table-row-gray": "#dcdcdc",

        "button-box-bright": "#ededed",
        "button-box-dark": "#e1e1e1",

        /*"button-box-bright-pressed" : "#e1e1e1",
         "button-box-dark-pressed" : "#ededed"*/

        "button-box-bright-pressed": "#ffd974",
        "button-box-dark-pressed": "#ffd974",

        // invalid form widgets
        "invalid": "#C82C2C",
        "border-focused-invalid": "#FF9999",
        "checkbox-hovered-inner-invalid": "#FAF2F2",
        "checkbox-hovered-invalid": "#F7E9E9",
        "radiobutton-hovered-invalid": "#F7EAEA",
        "border-invalid": "#C82C2C",
        "input-focused-inner-invalid": "#FF6B78",
        "tooltip-error": "#C82C2C"
    }
});