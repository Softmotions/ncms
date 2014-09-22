jQuery(function() {
    initNav();
    initTree();
    initSlideShow();
    initAccordion();
    initAboutSections();
    initSearchPopup();
});

/*
 * Drop in replace functions for setTimeout() & setInterval() that
 * make use of requestAnimationFrame() for performance where available
 * http://www.joelambert.co.uk
 *
 * Copyright 2011, Joe Lambert.
 * Free to use under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 */

// requestAnimationFrame() shim by Paul Irish
// http://paulirish.com/2011/requestanimationframe-for-smart-animating/
window.requestAnimFrame = (function() {
    return window.requestAnimationFrame ||
            window.webkitRequestAnimationFrame ||
            window.mozRequestAnimationFrame ||
            window.oRequestAnimationFrame ||
            window.msRequestAnimationFrame ||
            function(/* function */ callback, /* DOMElement */ element) {
                window.setTimeout(callback, 1000 / 60);
            };
})();

/**
 * Behaves the same as setTimeout except uses requestAnimationFrame() where possible for better performance
 * @param {function} fn The callback function
 * @param {int} delay The delay in milliseconds
 */
window.requestTimeout = function(fn, delay) {
    if (!window.requestAnimationFrame && !window.webkitRequestAnimationFrame && !window.mozRequestAnimationFrame && !window.oRequestAnimationFrame && !window.msRequestAnimationFrame) {
        return window.setTimeout(fn, delay);
    }
    var start = new Date().getTime();
    var handle = {};

    function loop() {
        var current = new Date().getTime(),
                delta = current - start;
        delta >= delay ? fn.call() : handle.value = requestAnimFrame(loop);
    }

    handle.value = requestAnimFrame(loop);
    return handle;
};

/**
 * Behaves the same as clearInterval except uses cancelRequestAnimationFrame() where possible for better performance
 * @param {int|object} fn The callback function
 */
window.clearRequestTimeout = function(handle) {
    window.cancelAnimationFrame ? window.cancelAnimationFrame(handle.value) :
    window.webkitCancelRequestAnimationFrame ? window.webkitCancelRequestAnimationFrame(handle.value) :
    window.mozCancelRequestAnimationFrame ? window.mozCancelRequestAnimationFrame(handle.value) :
    window.oCancelRequestAnimationFrame ? window.oCancelRequestAnimationFrame(handle.value) :
    window.msCancelRequestAnimationFrame ? msCancelRequestAnimationFrame(handle.value) :
    clearTimeout(handle);
};


function initTree() {
    $('ul.dynamic li.node > span').click(function() {
        var li = $(this).parent();
        var flag = $(li).hasClass('open');
        if (flag == true) {
            $(li).removeClass('open');
            $(li).addClass('close');
        } else {
            $(li).removeClass('close');
            $(li).addClass('open');
        }
    });
}

function initAboutSections() {
    $('a.about').click(function(event) {
        var _this = $(this);
        _this.slideUp(300);
        _this.prev("div").slideDown(300);
        event.preventDefault();
    });
}

function initNav() {
    jQuery('.slide-frame').sameHeight({
        elements : 'ul',
        flexible : true,
        multiLine : true
    });
    var _nav = $('#nav, #nav2');
    _nav.find('> ul > li > a').hover(function() {
        var _this = $(this);
        var _lnav = _this.closest('#nav, #nav2');
        if (_lnav.find('> ul > li.active').length == 0) {
            return;
        }
        if (!_this.parent().hasClass("active")) {
            _lnav.find('> ul > li.active').removeClass('active');
            _this.parent().addClass('active');
            _this.closest('.nav-holder').addClass('active');
        }
    }, function() {

    });
    _nav.find('> ul > li > a').click(function(event) {
        var _this = $(this);
        var _lnav = _this.closest('#nav, #nav2');
        if (_this.parent().find('.slide-holder').size()) {
            if (!_this.parent().hasClass('active')) {
                _lnav.find('> ul > li.active').removeClass('active');
                _this.parent().addClass('active');
                _this.closest('.nav-holder').addClass('active');
            } else {
                _this.parent().removeClass('active');
                _this.closest('.nav-holder').removeClass('active');
            }
            event.preventDefault();
        }
    });
    $(document).click(function(event) {
        if ($(event.target).closest(".nav-holder").length) {
            return;
        }
        $('.nav-holder').removeClass('active');
        $('#nav, #nav2').find('> ul > li.active').removeClass('active');
        event.stopPropagation();
    });
}


function initSlideShow() {
    $('.gallery-holder, .slider-holder').fadeGallery({
        slideElements : '.slide',
        pauseOnHover : true,
        autoRotation : true,
        switchTime : 10000,
        duration : 650,
        event : 'click'
    });
    $('.photo-line').tinycarousel({
        interval : true
    });
}

function initSearchPopup() {
    $('a.search').click(function() {
        $('a.search').hide();
        $('div#search-popup').show();
        return false;
    });
}

jQuery.fn.fadeGallery = function(_options) {
    var _options = jQuery.extend({
        slideElements : 'div.slideset > div',
        pagerLinks : '.control-panel li',
        btnNext : 'a.btn-next',
        btnPrev : 'a.btn-prev',
        btnPlayPause : 'a.play-pause',
        pausedClass : 'paused',
        playClass : 'playing',
        activeClass : 'active',
        pauseOnHover : true,
        autoRotation : false,
        autoHeight : false,
        switchTime : 3000,
        duration : 650,
        event : 'click'
    }, _options);

    return this.each(function() {
        var _this = jQuery(this);
        var _slides = jQuery(_options.slideElements, _this);
        var _pagerLinks = jQuery(_options.pagerLinks, _this);
        var _btnPrev = jQuery(_options.btnPrev, _this);
        var _btnNext = jQuery(_options.btnNext, _this);
        var _btnPlayPause = jQuery(_options.btnPlayPause, _this);
        var _pauseOnHover = _options.pauseOnHover;
        var _autoRotation = _options.autoRotation;
        var _activeClass = _options.activeClass;
        var _pausedClass = _options.pausedClass;
        var _playClass = _options.playClass;
        var _autoHeight = _options.autoHeight;
        var _duration = _options.duration;
        var _switchTime = _options.switchTime;
        var _controlEvent = _options.event;

        var _hover = false;
        var _prevIndex = 0;
        var _currentIndex = 0;
        var _slideCount = _slides.length;
        var _timer;
        if (!_slideCount) return;
        _slides.hide().eq(_currentIndex).show();
        if (_autoRotation) _this.removeClass(_pausedClass).addClass(_playClass);
        else _this.removeClass(_playClass).addClass(_pausedClass);

        if (_btnPrev.length) {
            _btnPrev.bind(_controlEvent, function() {
                prevSlide();
                return false;
            });
        }
        if (_btnNext.length) {
            _btnNext.bind(_controlEvent, function() {
                nextSlide();
                return false;
            });
        }
        if (_pagerLinks.length) {
            _pagerLinks.each(function(_ind) {
                jQuery(this).bind(_controlEvent, function() {
                    if (_currentIndex != _ind) {
                        _prevIndex = _currentIndex;
                        _currentIndex = _ind;
                        switchSlide();
                    }
                    return false;
                });
            });
        }

        if (_btnPlayPause.length) {
            _btnPlayPause.bind(_controlEvent, function() {
                if (_this.hasClass(_pausedClass)) {
                    _this.removeClass(_pausedClass).addClass(_playClass);
                    _autoRotation = true;
                    autoSlide();
                } else {
                    if (_timer) clearRequestTimeout(_timer);
                    _this.removeClass(_playClass).addClass(_pausedClass);
                }
                return false;
            });
        }

        function prevSlide() {
            _prevIndex = _currentIndex;
            if (_currentIndex > 0) _currentIndex--;
            else _currentIndex = _slideCount - 1;
            switchSlide();
        }

        function nextSlide() {
            _prevIndex = _currentIndex;
            if (_currentIndex < _slideCount - 1) _currentIndex++;
            else _currentIndex = 0;
            switchSlide();
        }

        function refreshStatus() {
            if (_pagerLinks.length) _pagerLinks.removeClass(_activeClass).eq(_currentIndex).addClass(_activeClass);
            _slides.eq(_prevIndex).removeClass(_activeClass);
            _slides.eq(_currentIndex).addClass(_activeClass);
        }

        function switchSlide() {
            _slides.stop(true, true);
            _slides.eq(_prevIndex).fadeOut(_duration);
            _slides.eq(_currentIndex).fadeIn(_duration);
            refreshStatus();
            autoSlide();
        }

        function autoSlide() {
            if (!_autoRotation || _hover) return;
            if (_timer) clearRequestTimeout(_timer);
            _timer = requestTimeout(nextSlide, _switchTime + _duration);
        }

        if (_pauseOnHover) {
            _this.hover(function() {
                _hover = true;
                if (_timer) clearRequestTimeout(_timer);
            }, function() {
                _hover = false;
                autoSlide();
            });
        }
        refreshStatus();
        autoSlide();
    });
};

function initAccordion() {
    $('.accordion > li > a').click(function(event) {
        var _this = $(this);
        if (!_this.parent().hasClass('active')) {
            _this.parent().find('ul').slideDown(200, function() {
                _this.parent().addClass('active');
            });
        } else {
            _this.parent().find('ul').slideUp(200, function() {
                _this.parent().removeClass('active');
            });
        }
        event.preventDefault();
    });
    $('.side-list > li > a').click(function(event) {
        if (!$(this).parent().hasClass('active')) {
            $(this).parents('.side-list').children('li.active').find('ul').slideUp(200, function() {
                $(this).parent().removeClass('active');
            });
            $(this).parent().find('ul').slideDown(200, function() {
                $(this).parent().addClass('active')
            });
        }
        event.preventDefault();
    })
}

//Tinycarousel plugin
(function($) {
    var pluginName = "tinycarousel"
            , defaults = {
                start : 0      // The starting slide
                , axis : "x"    // vertical or horizontal scroller? ( x || y ).
                , buttons : false   // show left and right navigation buttons.
                , bullets : false  // is there a page number navigation present?
                , interval : false  // move to another block on intervals.
                , intervalTime : 8000   // interval time in milliseconds.
                , animation : true   // false is instant, true is animate.
                , animationTime : 1000   // how fast must the animation move in ms?
                , infinite : true   // infinite carousel.
            };

    function Plugin($container, options) {
        this.options = $.extend({}, defaults, options);
        this._defaults = defaults;
        this._name = pluginName;

        var self = this
                , $viewport = $container.find(".viewport:first")
                , $overview = $container.find(".overview:first")
                , $slides = 0
                , $next = $container.find(".next:first")
                , $prev = $container.find(".prev:first")
                , $bullets = $container.find(".bullet")

                , viewportSize = 0
                , contentStyle = {}
                , slidesVisible = 0
                , slideSize = 0
                , slideIndex = 0

                , isHorizontal = this.options.axis === 'x'
                , sizeLabel = isHorizontal ? "Width" : "Height"
                , posiLabel = isHorizontal ? "left" : "top"
                , intervalTimer = null;

        this.slideCurrent = 0;
        this.slidesTotal = 0;

        function initialize() {
            self.update();
            self.move(self.slideCurrent);
            setEvents();
            return self;
        }

        this.update = function() {
            $overview.find(".mirrored").remove();

            $slides = $overview.children();
            viewportSize = $viewport[0]["offset" + sizeLabel];
            slideSize = $slides.first()["outer" + sizeLabel](true);
            self.slidesTotal = $slides.length;
            self.slideCurrent = self.options.start || 0;
            slidesVisible = Math.ceil(viewportSize / slideSize);

            $overview.append($slides.slice(0, slidesVisible).clone().addClass("mirrored"));
            $overview.css(sizeLabel.toLowerCase(), slideSize * (self.slidesTotal + slidesVisible));

            return self;
        };

        function setEvents() {
            if (self.options.buttons) {
                $prev.click(function() {
                    self.move(--slideIndex);
                    return false;
                });

                $next.click(function() {
                    self.move(++slideIndex);
                    return false;
                });
            }

            $(window).resize(self.update);

            if (self.options.bullets) {
                $container.on("click", ".bullet", function() {
                    self.move(slideIndex = +$(this).attr("data-slide"));
                    return false;
                });
            }
        }

        this.start = function() {
            if (self.options.interval) {
                clearTimeout(intervalTimer);
                intervalTimer = setTimeout(function() {
                    self.move(++slideIndex);

                }, self.options.intervalTime);
            }
            return self;
        };

        this.stop = function() {
            clearTimeout(intervalTimer);
            return self;
        };

        this.move = function(index) {
            slideIndex = index;
            self.slideCurrent = slideIndex % self.slidesTotal;

            if (slideIndex < 0) {
                self.slideCurrent = slideIndex = self.slidesTotal - 1;
                $overview.css(posiLabel, -(self.slidesTotal) * slideSize);
            }

            if (slideIndex > self.slidesTotal) {
                self.slideCurrent = slideIndex = 1;
                $overview.css(posiLabel, 0);
            }

            contentStyle[posiLabel] = -slideIndex * slideSize;

            $overview.animate(
                    contentStyle, {
                        queue : false, duration : self.options.animation ? self.options.animationTime : 0, always : function() {
                            $container.trigger("move", [$slides[self.slideCurrent], self.slideCurrent]);
                        }
                    });

            setButtons();
            self.start();
            return self;
        };

        function setButtons() {
            if (self.options.buttons && !self.options.infinite) {
                $prev.toggleClass("disable", self.slideCurrent <= 0);
                $next.toggleClass("disable", self.slideCurrent >= self.slidesTotal - slidesVisible);
            }

            if (self.options.bullets) {
                $bullets.removeClass("active");
                $($bullets[self.slideCurrent]).addClass("active");
            }
        }

        return initialize();
    }

    $.fn[pluginName] = function(options) {
        return this.each(function() {
            if (!$.data(this, "plugin_" + pluginName)) {
                $.data(this, "plugin_" + pluginName, new Plugin($(this), options));
            }
        });
    };
})(jQuery);


/*
 * SameHeight plugin
 */
(function($) {
    $.fn.sameHeight = function(opt) {
        var options = $.extend({
            skipClass : 'same-height-ignore',
            leftEdgeClass : 'same-height-left',
            rightEdgeClass : 'same-height-right',
            elements : '>*',
            flexible : false,
            multiLine : false,
            useMinHeight : false,
            biggestHeight : false
        }, opt);
        return this.each(function() {
            var holder = $(this),
                    postResizeTimer, ignoreResize;
            var elements = holder.find(options.elements).not('.' + options.skipClass);
            if (!elements.length) return;

            // resize handler
            function doResize() {
                elements.css(options.useMinHeight && supportMinHeight ? 'minHeight' : 'height', '');
                if (options.multiLine) {
                    // resize elements row by row
                    resizeElementsByRows(elements, options);
                } else {
                    // resize elements by holder
                    resizeElements(elements, holder, options);
                }
            }

            doResize();

            // handle flexible layout / font resize
            var delayedResizeHandler = function() {
                if (!ignoreResize) {
                    ignoreResize = true;
                    doResize();
                    clearTimeout(postResizeTimer);
                    postResizeTimer = setTimeout(function() {
                        doResize();
                        setTimeout(function() {
                            ignoreResize = false;
                        }, 10);
                    }, 100);
                }
            };

            // handle flexible/responsive layout
            if (options.flexible) {
                $(window).bind('resize orientationchange fontresize', delayedResizeHandler);
            }

            // handle complete page load including images and fonts
            $(window).bind('load', delayedResizeHandler);
        });
    };

    // detect css min-height support
    var supportMinHeight = typeof document.documentElement.style.maxHeight !== 'undefined';

    // get elements by rows
    function resizeElementsByRows(boxes, options) {
        var currentRow = $(),
                maxHeight, maxCalcHeight = 0,
                firstOffset = boxes.eq(0).offset().top;
        boxes.each(function(ind) {
            var curItem = $(this);
            if (curItem.offset().top === firstOffset) {
                currentRow = currentRow.add(this);
            } else {
                maxHeight = getMaxHeight(currentRow);
                maxCalcHeight = Math.max(maxCalcHeight, resizeElements(currentRow, maxHeight, options));
                currentRow = curItem;
                firstOffset = curItem.offset().top;
            }
        });
        if (currentRow.length) {
            maxHeight = getMaxHeight(currentRow);
            maxCalcHeight = Math.max(maxCalcHeight, resizeElements(currentRow, maxHeight, options));
        }
        if (options.biggestHeight) {
            boxes.css(options.useMinHeight && supportMinHeight ? 'minHeight' : 'height', maxCalcHeight);
        }
    }

    // calculate max element height
    function getMaxHeight(boxes) {
        var maxHeight = 0;
        boxes.each(function() {
            maxHeight = Math.max(maxHeight, $(this).outerHeight());
        });
        return maxHeight;
    }

    // resize helper function
    function resizeElements(boxes, parent, options) {
        var calcHeight;
        var parentHeight = typeof parent === 'number' ? parent : parent.height();
        boxes.removeClass(options.leftEdgeClass).removeClass(options.rightEdgeClass).each(function(i) {
            var element = $(this);
            var depthDiffHeight = 0;
            var isBorderBox = element.css('boxSizing') === 'border-box';

            if (typeof parent !== 'number') {
                element.parents().each(function() {
                    var tmpParent = $(this);
                    if (parent.is(this)) {
                        return false;
                    } else {
                        depthDiffHeight += tmpParent.outerHeight() - tmpParent.height();
                    }
                });
            }
            calcHeight = parentHeight - depthDiffHeight;
            calcHeight -= isBorderBox ? 0 : element.outerHeight() - element.height();

            if (calcHeight > 0) {
                element.css(options.useMinHeight && supportMinHeight ? 'minHeight' : 'height', calcHeight);
            }
        });
        boxes.filter(':first').addClass(options.leftEdgeClass);
        boxes.filter(':last').addClass(options.rightEdgeClass);
        return calcHeight;
    }
}(jQuery));


$(function init() {
    if ($('input:checkbox').size()) var _checkbox = $('input:checkbox').checkbox();
    if ($('input:radio').size()) var _radio = $('input:radio').radio();
});
$.fn.checkbox = function(o) {
    var callMethod = $.fn.checkbox.method;
    if (typeof o == "string" && o in $.fn.checkbox.method) {
        var checkbox = $(this);
        callMethod[o](checkbox);
        return checkbox;
    }
    if (!("method" in $.fn.checkbox)) {
        $.fn.checkbox.method = {
            "destroy" : function(checkbox) {
                if (checkbox.data('customized')) {
                    checkbox.off('change.customForms');
                    checkbox.each(function() {
                        $(this).data('customCheckbox').off('click.customForms').remove();
                    });
                    checkbox.removeData();
                } else {
                    throw new Error('объект не проинициализирован');
                }
            },
            "check" : function(checkbox) {
                checkbox.trigger('change.customForms', ['check']);
            },
            "uncheck" : function(checkbox) {
                checkbox.trigger('change.customForms', ['uncheck']);
            },
            "toggle" : function(checkbox) {
                var method = this;
                checkbox.each(function() {
                    if (!$(this).is(':checked')) {
                        method.check($(this));
                    } else {
                        method.uncheck($(this));
                    }
                });
            }
        };
        callMethod = $.fn.checkbox.method;
    }
    var checkboxes = [];
    $(this).each(function() {
        if (!$(this).data('customized')) {
            checkboxes.push(this);
        }
    });
    if (!$(this).size()) {
        throw new Error('селектор ' + $(this).selector + ' возвратил пустой набор элементов');
    }
    if (checkboxes.length) {
        o = $.extend({
            "checkboxClass" : "checkboxAreaChecked",
            "labelClass" : "active",
            "customCheckboxClass" : "checkboxArea"
        }, o);
        var customCheckbox = $('<div class="' + o.customCheckboxClass + '"></div>');
        checkboxes = $(checkboxes);
        checkboxes.data('customized', true);
        return checkboxes.each(function() {
            var checkbox = $(this),
                    localCustomCheckbox = customCheckbox.clone(),
                    checkboxClass = o.checkboxClass,
                    labelClass = o.labelClass;
            checkbox.data('customCheckbox', localCustomCheckbox);
            localCustomCheckbox.insertAfter(checkbox);
            if (checkbox.closest('label').size()) {
                checkbox.data('label', checkbox.closest('label'));
            } else if (checkbox.attr('id')) {
                checkbox.data('label', $('label[for=' + checkbox.attr('id') + ']'));
            }
            checkbox.on('change.customForms',function(e, command) {
                if (command == "check") {
                    check();
                } else if (command == "uncheck") {
                    uncheck();
                } else {
                    if (checkbox.is(':checked')) {
                        check();
                    } else {
                        uncheck();
                    }
                }
            }).trigger('change.customForms');
            localCustomCheckbox.on('click.customForms', function(e) {
                if (!localCustomCheckbox.hasClass(checkboxClass)) {
                    callMethod.check(checkbox);
                } else {
                    callMethod.uncheck(checkbox);
                }
                e.preventDefault();
            });

            function check() {
                checkbox.get(0).checked = true;
                localCustomCheckbox.addClass(checkboxClass);
                if (checkbox.data('label')) {
                    checkbox.data('label').addClass(labelClass);
                }
            }

            function uncheck() {
                checkbox.get(0).checked = false;
                localCustomCheckbox.removeClass(checkboxClass);
                if (checkbox.data('label')) {
                    checkbox.data('label').removeClass(labelClass);
                }
            }
        });
    } else {
        throw Error('чекбокс/ы уже проинициализирован/ы');
    }
};

$.fn.radio = function(o) {
    var callMethod = $.fn.radio.method;
    if (typeof o == "string" && o in $.fn.radio.method) {
        var radio = $(this);
        callMethod[o](radio);
        return radio;
    }
    if (!("method" in $.fn.radio)) {
        $.fn.radio.method = {
            "destroy" : function(radio) {
                var initedEls = [];
                radio.each(function() {
                    if ($(this).data('customized')) {
                        initedEls.push(this);
                    }
                });
                if (initedEls.length) {
                    radio = $(initedEls);
                    radio.off('change.customForms');
                    radio.each(function() {
                        $(this).data('customRadio').off('click.customForms').remove();
                    });
                    radio.removeData();
                } else {
                    throw new Error('объект не проинициализирован');
                }
            },
            "check" : function(radio) {
                radio.trigger('change', ['check']);
            }
        };
        callMethod = $.fn.radio.method;
    }
    if (!('group' in $.fn.radio)) {
        $.fn.radio.group = {};
    }
    if (!$(this).size()) {
        throw new Error('селектор ' + $(this).selector + ' возвратил пустой набор элементов');
    }
    var radios = [];
    $(this).each(function() {
        if (!$(this).data('customized')) {
            radios.push(this);
        }
    });
    if (radios.length) {
        o = $.extend({
            "radioClass" : "radioAreaChecked",
            "labelClass" : "active",
            "customRadioClass" : "radioArea"
        }, o);
        var customRadio = $('<div class="' + o.customRadioClass + '"></div>'),
                group = $.fn.radio.group;
        radios = $(radios);
        radios.data('customized', true);
        radios.each(function() {
            if ($(this).attr('name') && !($(this).attr('name') in group))
                group[$(this).attr('name')] = radios.filter('input:radio[name=\'' + $(this).attr('name') + '\']');
        });
        return radios.each(function() {
            var radio = $(this),
                    localCustomRadio = customRadio.clone(),
                    curGroup = radio.attr('name') in group ? group[radio.attr('name')] : 0,
                    radioClass = o.radioClass,
                    labelClass = o.labelClass;
            radio.data('customRadio', localCustomRadio);
            localCustomRadio.insertAfter(radio);
            if (radio.closest('label').size()) {
                radio.data('label', radio.closest('label'));
            } else if (radio.attr('id')) {
                radio.data('label', $('label[for=' + radio.attr('id') + ']'));
            }
            radio.on('change.customForms',function(e, command) {
                if (radio.is(':checked') || command == "check") {
                    if (curGroup) {
                        uncheck(curGroup.not(radio).next());
                        if (curGroup.data('label').size()) {
                            curGroup.each(function() {
                                if ($(this).data('label')) {
                                    $(this).data('label').removeClass('active');
                                }
                            });
                        }
                    }
                    check(localCustomRadio);
                    if (command == "check") check(radio);
                    if (radio.data('label')) {
                        radio.data('label').addClass(labelClass);
                    }
                }
            }).trigger('change.customForms');
            localCustomRadio.on('click.customForms', function(e) {
                if (!localCustomRadio.hasClass(radioClass)) {
                    callMethod.check(radio);
                }
                e.preventDefault();
            });

            function check(radio) {
                if (radio.is('input:radio')) {
                    radio.get(0).checked = true;
                } else {
                    radio.addClass(radioClass);
                }
            }

            function uncheck(radio) {
                if (radio.is('input:radio')) {
                    radio.get(0).checked = false;
                } else {
                    radio.removeClass(radioClass);
                }
            }
        });
    } else {
        throw Error('радиокнопка/и уже проинициализирована/ы');
    }
};

function initSearch(pageSize) {
    var spc = {
        form : $('form#spc-search-form'),
        results : $('ul#spc-search-results'),
        fetchMore : $('a#spc-fetch-more'),
        loader : $('.loader'),
        start : 0,
        pageSize : pageSize || 1
    };

    spc.loader.hide();
    spc.results.find('li').show();

    updateSearchButtons = function() {
        spc.fetchMore.hide();
        if (spc.results.find('li').size() >= ((spc.start || 0) + spc.pageSize)) {
            spc.fetchMore.show()
        }
    };

    doSearch = function(reset) {
        if (reset) {
            spc.results.html(null);
        }

        spc.loader.show();
        spc.start = reset ? 0 : (spc.start || 0) + spc.pageSize;
        var fdata = spc.form.serializeArray();
        fdata.push({name : "spc.action", value : "search"});
        fdata.push({name : "spc.start", value : spc.start});
        fdata.push({name : "spc.limit", value : spc.pageSize});
        $.post(spc.form[0].action, fdata).done(function(data) {
            spc.loader.hide();
            spc.results.append(data);
            spc.results.find('li:hidden').slideDown();
            updateSearchButtons();
        });

        updateSearchButtons();
    };

    spc.form.submit(function() {
        doSearch(true);
        return false;
    });
    spc.fetchMore.click(function() {
        doSearch(false);
        return false;
    });
    updateSearchButtons();
}

function initMainNews() {
    var mpc = {
        columns : [
            {
                subscribed : true,
                holder : $('div.news-holder'),
                results : $('ul.news-list'),
                type : 'a'
            },
            {
                subscribed : true,
                holder : $('div.announcement-holder'),
                results : $('ul.announcement-list'),
                type : 'b'
            },
            {
                subscribed : true,
                holder : $('div.faculty-holder'),
                results : $('ul.faculty-list'),
                type : 'c'
            }
        ],
        main : $('div#news'),
        fetchMore : $('a#mpc-fetch-more'),
        loader : $('.loader')
    }

    mpc.loader.hide();
    mpc.fetchMore.hide();

    var subscribed = false;
    var minHeight = 0;
    for (var i = 0; i < mpc.columns.length; ++i) {
        var column = mpc.columns[i];
        if (column.subscribed) {
            column.subscribed = column.results.find('li').size() > 0;
        }
        var height = column.holder.height();
        if ((height > 500 && (height < minHeight || minHeight < 500)) || height > minHeight) {
            minHeight = height;
        }
        subscribed = subscribed || column.subscribed;
    }

    mpc.main.height(minHeight);
    if (subscribed) {
        mpc.fetchMore.show();
    }

    var loadMore = function() {
        var height = mpc.main.height();
        var toload = [];
        for (var i = 0; i < mpc.columns.length; ++i) {
            var column = mpc.columns[i];
            if (column.subscribed && (column.holder.height() - 250 < height)) {
                toload.push(column);
            }
        }

        mpc.fetchMore.hide();

        var updateView = function() {
            var subscribed = false;
            var minHeight = 0;
            var maxHeight = 0;
            for (var i = 0; i < mpc.columns.length; ++i) {
                var column = mpc.columns[i];
                if (column.subscribed) {
                    column.subscribed = column.results.find('li').size() > 0;
                }
                var height = column.holder.height();
                if ((height > 500 && (height < minHeight || minHeight < 500)) || height > minHeight) {
                    minHeight = height;
                }
                if (height > maxHeight) {
                    maxHeight = height;
                }
                subscribed = subscribed || column.subscribed;
            }

            if (subscribed) {
                mpc.main.height(minHeight);
                mpc.fetchMore.show();
            } else {
                mpc.main.height(maxHeight);
            }

            if (loading == 0) {
                mpc.loader.hide();
            }
        };

        var loading = 0;
        var fetchMore = function(def) {
            mpc.loader.show();
            ++loading;
            var data = {
                "mpc.action" : "fetchMore",
                "mpc.fetch.type" : def.type
            };
            var prevSize = data["mpc." + def.type + ".skip"] = def.results.find('li').size();
            $.post(mpc.fetchMore[0].href, data,function(html) {
                def.results.append(html);
                if (def.results.find('li').size() == prevSize) {
                    def.subscribed = false;
                }
            }).fail(function() {
                def.subscribed = false;
            }).always(function() {
                if (--loading == 0) {
                    updateView();
                }
            });
        };

        for (var j = 0; j < toload.length; ++j) {
            fetchMore(toload[j]);
        }

        if (loading == 0) {
            updateView();
        }
    };

    mpc.fetchMore.click(function() {
        loadMore();
        return false;
    });
}

function initFetchMore(btn, action, results, pageSize, prefix) {
    $(btn).hide();
    $(results).find('li').show();

    $(btn).click(function() {
                var data = {};
                data[prefix + ".action"] = "fetchMore";
                data[prefix + ".skip"] = $(results).find('li').size();
                data[prefix + ".limit"] = pageSize;

                $.post(action, data,function(html) {
                    $(btn).hide();
                    var count = $(results).find('li').size()
                    $(results).append(html);
                    if ($(results).find('li').size() - count >= pageSize) {
                        $(btn).show();
                    }
                    $(results).find('li:hidden').slideDown();
                });
                return false;
            }
    );

    if ($(results).find('li').size() >= pageSize) {
        $(btn).show();
    }
}