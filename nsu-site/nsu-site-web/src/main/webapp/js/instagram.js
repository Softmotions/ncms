function supports_html5_storage() {
    try {
        return 'localStorage' in window && window['localStorage'] !== null;
    } catch (e) {
        return false;
    }
}

(function ($) {

    $.fn.instagramLite = function (options) {

        //return if no element was bound
        //so chained events can continue
        if (!this.length) {
            return this;
        }

        //define default parameters
        var defaults = {
            userID: null,
            clientID: null,
            limit: 6,
            list: true,
            urls: false,
            localstore : null,
            error: function () {
            },
            success: function () {
            }
        };

        //define plugin
        var plugin = this;

        //define settings
        plugin.settings = {};

        //merge defaults and options
        plugin.settings = $.extend({}, defaults, options);

        //define element
        var el = $(this);

        //if client ID and username were provided
        if (plugin.settings.clientID && plugin.settings.userID) {
            //for each element
            el.each(function () {
                var data = null;
                if (plugin.settings.localstore && supports_html5_storage()) {
                    try {
                        var item = JSON.parse(localStorage.getItem(plugin.settings.localstore.key));
                        if (item && (new Date().getTime() - item.time <= plugin.settings.localstore.timeout)) {
                            data = item.data;
                        }
                    } catch (e) {
                    }
                }

                var updateItems = function (data, fromCache) {
                    //for each piece of media returned
                    for (var i = 0; i < data.length; i++) {
                        //define media namespace
                        var thisMedia = data[i];

                        //if media type is image
                        if (thisMedia.type === 'image') {
                            //construct image
                            var img = '<img src="' + thisMedia.images.thumbnail.url + '" alt="Instagram Image" data-filter="' + thisMedia.filter + '" />';

                            //if url setting is true
                            if (plugin.settings.urls) {
                                var img = '<a href="' + thisMedia.link + '" target="_blank">' + img + '</a>';
                            }

                            //if list setting is true
                            if (plugin.settings.list) {
                                var img = '<li>' + img + '</li>';
                            }

                            //append image
                            el.append(img);
                        }
                    }

                    //execute error callback
                    plugin.settings.success.call(el, fromCache);
                };

                if (!data) {
                    //get user's media using their ID
                    $.ajax({
                        type: 'GET',
                        url: 'https://api.instagram.com/v1/users/' + plugin.settings.userID + '/media/recent/?client_id=' + plugin.settings.clientID + '&count=' + plugin.settings.limit + '&callback=?',
                        dataType: 'jsonp',
                        cache: true,
                        success: function (data) {
                            if (data.meta.code === 200) {
                                if (plugin.settings.localstore && supports_html5_storage()) {
                                    localStorage.setItem(plugin.settings.localstore.key, JSON.stringify({time: new Date().getTime(), data: data.data}));
                                }
                                updateItems(data.data, false);
                            } else {
                                //execute error callback
                                plugin.settings.error.call(this, data.meta.code, data.meta.error_message);
                            }
                        },
                        error: function () {
                            //execute error callback
                            plugin.settings.error.call(this);

                        }
                    });
                } else {
                    updateItems(data, true);
                }
            });
        } else {
            console.log('Both a client ID and username are required to use this plugin.');
        }
    }
})(jQuery);