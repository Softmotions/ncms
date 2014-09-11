/**
 * @asset(ncms/icon/16/misc/image-plus.png)
 */
qx.Class.define("ncms.asm.am.MedialineAMValueWidget", {
    extend : sm.table.ToolbarLocalTable,
    implement : [ qx.ui.form.IModel,
                  ncms.asm.am.IValueWidget ],
    include : [ ncms.asm.am.MValueWidget,
                sm.table.MTableMutator ],

    properties : {
        model : {
            check : "Object",
            nullable : true,
            event : "changeModel",
            apply : "__applyModel"
        }
    },


    construct : function(asmSpec, attrSpec) {
        this.__asmSpec = asmSpec;
        this.__attrSpec = attrSpec;
        this.base(arguments);
        this.set({height : 200});
        this._reload([]);
    },


    members : {

        __asmSpec : null,

        __attrSpec : null,

        _createTable : function(tm) {
            var table = new sm.table.Table(tm, tm.getCustom());
            table.getSelectionModel().addListener("changeSelection", this.__syncState, this);
            table.set({
                showCellFocusIndicator : false,
                statusBarVisible : false,
                focusCellOnPointerMove : false});

            table.setContextMenu(new qx.ui.menu.Menu());
            table.addListener("beforeContextmenuOpen", this.__beforeContextmenuOpen, this);
            return table;
        },

        _createToolbarItems : function(toolbar) {
            var part = new qx.ui.toolbar.Part().set({"appearance" : "toolbar-table/part"});
            toolbar.add(part);

            var bt = this._createButton(null, "ncms/icon/16/misc/image-plus.png");
            bt.setToolTipText(this.tr("Add images to the gallery"));
            bt.addListener("execute", this.__addImages, this);
            part.add(bt);

            return toolbar;
        },

        _createButton : function(label, icon, handler, self) {
            var bt = new qx.ui.toolbar.Button(label, icon).set({"appearance" : "toolbar-table-button"});
            if (handler != null) {
                bt.addListener("execute", handler, self);
            }
            return bt;
        },

        __beforeContextmenuOpen : function(ev) {
            var rd = this.getSelectedRowData();
            var menu = ev.getData().getTarget();
            menu.removeAll();

            var bt = new qx.ui.menu.Button(this.tr("Add images"));
            bt.addListenerOnce("execute", this.__addImages, this);
            menu.add(bt);
        },

        _setJsonTableData : function(tm, items) {
            var data = {
                "columns" : [
                    {
                        "title" : this.tr("Image/Link").toString(),
                        "id" : "resource",
                        "sortable" : false,
                        "width" : "1*"
                    },
                    {
                        "title" : this.tr("Title").toString(),
                        "id" : "description",
                        "width" : "1*"
                    }
                ],
                "items" : items
            };
            tm.setJsonData(data);
            this.__syncState();
        },

        __addImages : function() {
            var asmSpec = this.__asmSpec;
            var dlg = new ncms.mmgr.PageFilesSelectorDlg(
                    asmSpec["id"],
                    this.tr("Add images to the media gallery"), {
                        allowModify : true,
                        linkText : false
                    });
            dlg.setCtypeAcceptor(ncms.Utils.isImageContentType.bind(ncms.Utils));
            dlg.addListener("completed", function(evt) {
                var data = evt.getData();
                qx.log.Logger.info("data=" + JSON.stringify(data));
                dlg.close();
            }, this);
            dlg.open();
        },

        __syncState : function() {
        },

        __applyModel : function(model) {
            qx.log.Logger.info("model=" + model);

        }
    },

    destruct : function() {
        this.__asmSpec = null;
        this.__attrSpec = null;
        //this._disposeObjects("__field_name");                                
    }
});