/*
 * Copyright (c) 2011. Softmotions Ltd. (softmotions.com)
 * All Rights Reserved.
 */

/**
 * Синхронизация состояния приложения с серверной стороной
 */

qx.Class.define("ncms.AppState", {
    extend: qx.core.Object,

    events: {

        /**
         * В качестве данных события используется текущий экземпляр AppState.__stateObject
         */
        "stateChanged": "qx.event.type.Data"
    },

    properties: {},

    /**
     * @param urlName {String} Адрес сервиса ответственнго за обновление состояния
     */
    construct: function (urlName) {
        this.base(arguments);
        this.__urlName = urlName;
        this.__url = ncms.Application.ACT.getUrl(urlName);
        this.__json = new sm.store.Json(null, false);
        this.__json.addListener("loaded", this._applyJsonData, this);
        var req = new sm.io.Request(this.__url, "GET", "application/json");
        req.setAsynchronous(false);
        this.__json.setRequest(req);
    },

    members: {
        __urlName: null,
        __url: null,
        __json: null,
        __stateObject: null,

        reload: function (cb) {
            var req = new sm.io.Request(
                this.__url, "GET", "application/json");
            if (cb != null) {
                req.addListenerOnce("finished", cb);
            }
            this.__json.setRequest(req);
        },

        /**
         * Устанавливает значение property объекта состояния текущего приложения
         * @param props {Object}
         */
        setStateProperties: function (props) {
            if (this.__stateObject == null) {
                throw new Error("AppState must be synchronized before changing state props");
            }
            var sprops = this.__stateObject["properties"];
            if (!sprops) {
                throw new Error("Invalid state object, 'properties' section must be presented");
            }
            Object.getOwnPropertyNames(props).forEach(function (pn) {
                sprops[pn] = props[pn];
            });
            var req = new sm.io.Request(this.__url, "PUT", "application/json");
            req.setData(JSON.stringify(sprops));
            this.__json.setRequest(req);
        },

        /**
         * Set single state object property, it is more performant method than setStateProperties
         * @param pname {String}
         * @param pval {Object}
         */
        setStateProperty: function (pname, pval) {
            if (this.__stateObject == null) {
                throw new Error("AppState must be synchronized before changing state props");
            }
            var sprops = this.__stateObject["properties"];
            if (!sprops) {
                throw new Error("Invalid state object, 'properties' section must be presented");
            }
            var url = ncms.Application.ACT.getRestUrl(this.__urlName, pname);
            var req = new sm.io.Request(url, "POST", "application/json");
            req.setData(JSON.stringify(pval));
            req.send(function () {
                sprops[pname] = pval;
                this.fireDataEvent("stateChanged", this.__stateObject);
            }, this);
        },

        /**
         * Get state property value
         * @param pname {String}
         */
        getStateProperty: function (pname) {
            return (this.__stateObject && this.__stateObject["properties"])
                ? this.__stateObject["properties"][pname] : null;
        },

        _getStateConstant: function (pname) {
            return (this.__stateObject) ? this.__stateObject[pname] : null;
        },

        getAppName: function () {
            return this._getStateConstant("appName");
        },

        getHelpSite: function () {
            return this._getStateConstant("helpSite");
        },

        getHelpSiteTopicUrl: function (topic) {
            return (this.__stateObject && this.__stateObject["helpTopics"])
                ? this.__stateObject["helpTopics"][topic] : null;
        },

        getSessionId: function () {
            return this._getStateConstant("sessionId");
        },

        getUserId: function () {
            return this._getStateConstant("userId");
        },

        getUserLogin: function () {
            return this._getStateConstant("userLogin");
        },

        getUserFullName: function () {
            return this._getStateConstant("userFullName");
        },

        getUserNickname: function () {
            return this._getStateConstant("userNickname");
        },

        getUserEmail: function () {
            return this._getStateConstant("email");
        },

        getServerTZOffset: function() {
            return this._getStateConstant("serverTZOffset");
        },

        userHasRole: function (role) {
            var aroles = this._getStateConstant("roles");
            if (qx.lang.Type.isArray(aroles)) {
                return aroles.indexOf(role) != -1;
            } else if (aroles != null) {
                return !!aroles[role];
            } else {
                return false;
            }
        },

        userInRoles: function (role) {
            if (role == null) {
                return false;
            }
            if (typeof role == "string") {
                return this.userHasRole(role);
            }
            for (var i = 0; i < role.length; ++i) {
                if (this.userHasRole(role[i])) {
                    return true;
                }
            }
            return false;
        },

        _applyJsonData: function (ev) {
            var data = ev.getData();
            if (data == null) {
                this.warn("No data found in json model");
                this.fireDataEvent("stateChanged", this.__stateObject);
                return;
            }
            this.__stateObject = data;
            this.fireDataEvent("stateChanged", this.__stateObject);
        }
    }
});