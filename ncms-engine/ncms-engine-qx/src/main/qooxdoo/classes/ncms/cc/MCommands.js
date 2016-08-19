/**
 * Commands support
 */
qx.Mixin.define("ncms.cc.MCommands", {

    members: {

        __cmds: null,

        __fw: null,

        __getCmds: function () {
            if (this.__cmds == null) {
                this.__cmds = [];
            }
            return this.__cmds;
        },

        _registerCommand: function (cmd, exec, ctx) {
            cmd.setEnabled(false);
            this.__getCmds().push(cmd);
            if (exec) {
                cmd.addListener("execute", exec, ctx);
            }
        },

        _registerCommandFocusWidget: function (fw) {
            if (this.__fw) {
                this.__fw.removeListener("focusin", this._enableCmds, this);
                this.__fw.removeListener("appear", this._enableCmds, this);
                this.__fw.removeListener("focusout", this._disableCmds, this);
                this.__fw.removeListener("disappear", this._disableCmds, this);
            }
            this.__fw = fw;
            if (!fw.isFocusable()) {
                fw.addListener("appear", this._enableCmds, this);
            } else {
                fw.addListener("focusin", this._enableCmds, this);
                fw.addListener("focusout", this._disableCmds, this);
            }
            fw.addListener("disappear", this._disableCmds, this);
            if (fw.hasState("focused") || (!fw.isFocusable() && fw.isVisible())) {
                this._enableCmds();
            } else {
                this._disableCmds();
            }
        },

        _enableCmds: function () {
            this.__getCmds().forEach(function (el) {
                el.setEnabled(true);
            });
        },

        _disableCmds: function () {
            this.__getCmds().forEach(function (el) {
                el.setEnabled(false);
            });
        }
    },

    destruct: function () {
        this._disposeArray("__cmds");
        if (this.__fw) {
            this.__fw.removeListener("focusin", this._enableCmds, this);
            this.__fw.removeListener("focusout", this._disableCmds, this);
            this.__fw.removeListener("disappear", this._disableCmds, this);
            this.__fw = null;
        }
    }
});