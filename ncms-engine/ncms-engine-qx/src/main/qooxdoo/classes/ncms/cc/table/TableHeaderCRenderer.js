/**
 * Cell renderer:
 * Text of a first row with bold style.
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 * @version $Id$
 */
qx.Class.define("ncms.cc.table.TableHeaderCRenderer", {
    extend: qx.ui.table.cellrenderer.Conditional,

    members: {
        _getCellStyle: function (cellInfo) {
            if (cellInfo.row == 0) { //First row is the header
                return "font-weight:bold;";
            } else {
                return this.base(arguments, cellInfo);
            }
        }
    }
});