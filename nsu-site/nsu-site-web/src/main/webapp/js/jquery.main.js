$(document).ready(function() {
	$('input.text').focus(function() {
		$(this).closest('.input-holder').addClass('parent-focus');
	}).blur(function() {
		$(this).closest('.input-holder').removeClass('parent-focus');
	});
	$('input, textarea').placeholder();
});
// page init
jQuery(function() {
	initSameHeight();
	initSlideShow();
	initAccordion();
	initNav();
	initRemember();
});

function initRemember() {
	$('.event-list .remember').click(function(event) {
		$('.remember-form').fadeOut(200);
		$(this).closest('li').find('.remember-form').fadeIn(200);
		event.preventDefault();
	});
	$('.remember-form .close').click(function(event) {
		$('.remember-form').fadeOut(200);
		event.preventDefault();
	});
}

function initNav() {
	$('#nav > ul > li > a').click(function(event) {
		var _this = $(this);
		if (_this.parent().find('.slide-holder').size()) {
			if (!_this.parent().hasClass('active')) {
				$('#nav > ul > li.active').removeClass('active');
				_this.parent().addClass('active');
				$('.header-holder').addClass('active');
			} else {
				_this.parent().removeClass('active');
				$('.header-holder').removeClass('active');
			}
			event.preventDefault();
		}
	});
	$(document).click(function(event) {
		if ($(event.target).closest(".header-holder").length) return;
		$(".header-holder").removeClass('active');
		$('#nav > ul > li.active').removeClass('active');
		event.stopPropagation();
	})
}

function initAccordion() {
	$('.accordion > li > a').click(function(event) {
		var _this = $(this);
		if (!_this.parent().hasClass('active')) {
			_this.parent().find('ul').slideDown(500, function() {
				_this.parent().addClass('active');
			});
		} else {
			_this.parent().find('ul').slideUp(500, function() {
				_this.parent().removeClass('active');
			});
		}
		event.preventDefault();
	});
	$('.side-list > li > a').click(function(event) {
		if (!$(this).parent().hasClass('active')) {
			$(this).parents('.side-list').children('li.active').find('ul').slideUp(500, function() {
				$(this).parent().removeClass('active');
			});
			$(this).parent().find('ul').slideDown(500, function() {
				$(this).parent().addClass('active')
			});
		}
		event.preventDefault();
	})
}

function initSlideShow() {
	$('.gallery-holder, .slider-holder').fadeGallery({
		slideElements: '.slide',
		pauseOnHover: true,
		autoRotation: true,
		switchTime: 4000,
		duration: 650,
		event: 'click'
	});
}

// align blocks height
function initSameHeight() {
	jQuery('.slide-frame').sameHeight({
		elements: 'ul',
		flexible: true,
		multiLine: true
	});
}

jQuery.fn.fadeGallery = function(_options) {
	var _options = jQuery.extend({
		slideElements: 'div.slideset > div',
		pagerLinks: '.control-panel li',
		btnNext: 'a.btn-next',
		btnPrev: 'a.btn-prev',
		btnPlayPause: 'a.play-pause',
		pausedClass: 'paused',
		playClass: 'playing',
		activeClass: 'active',
		pauseOnHover: true,
		autoRotation: false,
		autoHeight: false,
		switchTime: 3000,
		duration: 650,
		event: 'click'
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
}
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
		function( /* function */ callback, /* DOMElement */ element) {
			window.setTimeout(callback, 1000 / 60);
		};
})();
/**
 * Behaves the same as setTimeout except uses requestAnimationFrame() where possible for better performance
 * @param {function} fn The callback function
 * @param {int} delay The delay in milliseconds
 */

window.requestTimeout = function(fn, delay) {
	if (!window.requestAnimationFrame &&
		!window.webkitRequestAnimationFrame &&
		!window.mozRequestAnimationFrame &&
		!window.oRequestAnimationFrame &&
		!window.msRequestAnimationFrame)
		return window.setTimeout(fn, delay);

	var start = new Date().getTime(),
		handle = new Object();

	function loop() {
		var current = new Date().getTime(),
			delta = current - start;

		delta >= delay ? fn.call() : handle.value = requestAnimFrame(loop);
	};

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

/*
 * jQuery SameHeight plugin
 */
;
(function($) {
	$.fn.sameHeight = function(opt) {
		var options = $.extend({
			skipClass: 'same-height-ignore',
			leftEdgeClass: 'same-height-left',
			rightEdgeClass: 'same-height-right',
			elements: '>*',
			flexible: false,
			multiLine: false,
			useMinHeight: false,
			biggestHeight: false
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

/*
 * jQuery FontResize Event
 */
jQuery.onFontResize = (function($) {
	$(function() {
		var randomID = 'font-resize-frame-' + Math.floor(Math.random() * 1000);
		var resizeFrame = $('<iframe>').attr('id', randomID).addClass('font-resize-helper');

		// required styles
		resizeFrame.css({
			width: '100em',
			height: '10px',
			position: 'absolute',
			borderWidth: 0,
			top: '-9999px',
			left: '-9999px'
		}).appendTo('body');

		// use native IE resize event if possible
		if (window.attachEvent && !window.addEventListener) {
			resizeFrame.bind('resize', function() {
				$.onFontResize.trigger(resizeFrame[0].offsetWidth / 100);
			});
		}
		// use script inside the iframe to detect resize for other browsers
		else {
			var doc = resizeFrame[0].contentWindow.document;
			doc.open();
			doc.write('<scri' + 'pt>window.onload = function(){var em = parent.jQuery("#' + randomID + '")[0];window.onresize = function(){if(parent.jQuery.onFontResize){parent.jQuery.onFontResize.trigger(em.offsetWidth / 100);}}};</scri' + 'pt>');
			doc.close();
		}
		jQuery.onFontResize.initialSize = resizeFrame[0].offsetWidth / 100;
	});
	return {
		// public method, so it can be called from within the iframe
		trigger: function(em) {
			$(window).trigger("fontresize", [em]);
		}
	};
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
			"destroy": function(checkbox) {
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
			"check": function(checkbox) {
				checkbox.trigger('change.customForms', ['check']);
			},
			"uncheck": function(checkbox) {
				checkbox.trigger('change.customForms', ['uncheck']);
			},
			"toggle": function(checkbox) {
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
			"checkboxClass": "checkboxAreaChecked",
			"labelClass": "active",
			"customCheckboxClass": "checkboxArea"
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
			checkbox.on('change.customForms', function(e, command) {
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
}
$.fn.radio = function(o) {
	var callMethod = $.fn.radio.method;
	if (typeof o == "string" && o in $.fn.radio.method) {
		var radio = $(this);
		callMethod[o](radio);
		return radio;
	}
	if (!("method" in $.fn.radio)) {
		$.fn.radio.method = {
			"destroy": function(radio) {
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
			"check": function(radio) {
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
			"radioClass": "radioAreaChecked",
			"labelClass": "active",
			"customRadioClass": "radioArea"
		}, o);
		var customRadio = $('<div class="' + o.customRadioClass + '"></div>'),
			group = $.fn.radio.group;
		radios = $(radios);
		radios.data('customized', true);
		radios.each(function() {
			if ($(this).attr('name') && !($(this).attr('name') in group))
				group[$(this).attr('name')] = radios.filter('input:radio[name=' + $(this).attr('name') + ']');
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
			radio.on('change.customForms', function(e, command) {
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
}
$(function init() {
	if ($('select').size()) {
		var select = $('select').select();
	}
});
//version 1.1.0
$.fn.select = function(o) {
	var callMethod = $.fn.select.method,
		itemClick = jQuery.Event("itemClick"),
		selectReady = jQuery.Event("selectReady"),
		enabled = jQuery.Event("enabled"),
		disabled = jQuery.Event("disabled"),
		destroyed = jQuery.Event("destroyed");
	if (typeof o == "string" && o in $.fn.select.method) {
		var select = $(this);
		callMethod[o](select, arguments[1]);
		return $(this);
	}
	if (!("method" in $.fn.select)) {
		$.fn.select.method = {
			"destroy": function(select) {
				if (select.data('customized')) {
					select.off('change' + o.namespace);
					select.each(function() {
						var instance = $(this);
						instance.data('customSelect').remove();
						$(document).off('mousedown', instance.data("mousedownHandler"));
						$(window).off('resize', instance.data("resizeHandler"));
					});
					select.removeData();
					select.trigger('destroyed');
				} else {
					throw new Error('объект не проинициализирован');
				}
			},
			"enable": function(select) {
				if (select.data('disable')) {
					select.attr('disabled', false);
					select.data('customSelect').first().on('click' + o.namespace, select.data('openerHandler')).removeClass('disabled');
					select.trigger('enabled');
				}
			},
			"disable": function(select) {
				if (!select.data('disable')) {
					select.data('disable', true);
					select.attr('disabled', true);
					select.data('openerHandler', $._data(select.data('customSelect').first().get(0), "events").click[0].handler);
					select.data('customSelect').first().off('click').addClass('disabled');
					select.trigger('disabled');
				}
			},
			"pick": function(select, index) {
				select.each(function() {
					this.selectedIndex = index;
				});
				select.trigger("change" + o.namespace);
			}
		};
		callMethod = $.fn.select.method;
	}
	o = $.extend({
		"list": "ul",
		"namespace": ".select",
		"item": "li",
		"itemHTML": "li span",
		"openerClass": "selectmenu",
		"icoClass": "selectmenu-icon",
		"selectedClass": "selectmenu-status",
		"activeItemClass": "active",
		"activeOpenerClass": "active",
		"dropDownClass": "selectmenu-menu",
		"style": "dropdown", //popup,dropdown
		"transferClass": true,
		"dropdownHasBorder": true,
		"hasIcons": true,
		"resizable": false,
		"triggerEvents": true
	}, o);
	var select = [],
		body = $('body'),
		openerHTML = $('<a class="' + o.openerClass + '"><span class="' + o.icoClass + '"></span><span class="' + o.selectedClass + '"></span></a>'),
		dropdownHTML = $('<div class=' + o.dropDownClass + '>' +
			'<div class="select-top">' +
			'<div class="select-l"></div>' +
			'<div class="select-r"></div>' +
			'</div>' +
			'<div class="select-c">' +
			'<div class="c appendHere">' +
			'</div>' +
			'</div>' +
			'<div class="select-bottom">' +
			'<div class="select-l"></div>' +
			'<div class="select-r"></div>' +
			'</div>' +
			'</div>');
	$(this).each(function(i) {
		if (!$(this).data('customized')) {
			select.push(this);
		}
	});
	if (select.length) {
		$(select).each(function() {
			var opener = openerHTML.clone(),
				nativeSelect = $(this),
				title = nativeSelect.find("option[title]").text(),
				options = nativeSelect.find("option[title]").attr('disabled', true).end().find('option'),
				optionSize = options.size() - 1,
				dropdown = dropdownHTML.clone(),
				itemTree = o.itemHTML.split(' '),
				hasChild = itemTree.length >= 2,
				list = "<" + o.list + ">";
			nativeSelect.find('option').each(function(i, data) {
				if ($(this).attr('title')) {
					list += "<" + o.item + " class='title' style='display:none;'>" + data.childNodes[0].nodeValue + "</" + o.item + ">";
				} else {
					if (!hasChild) {
						list += "<" + o.item + ">" + data.childNodes[0].nodeValue + "</" + o.item + ">";
					} else {
						var buffer = '';
						for (var k = itemTree.length - 1; k != 0; k--) {
							if (!buffer) {
								buffer += "<" + itemTree[k] + ">" + data.childNodes[0].nodeValue + "</" + itemTree[k] + ">";
							} else if (k != 0 && itemTree.length > 2) {
								buffer = "<" + itemTree[k] + ">" + buffer + "</" + itemTree[k] + ">";
							}
						}
						buffer = "<" + itemTree[0] + ">" + buffer + "</" + itemTree[0] + ">";
						list += buffer;
					}
				}
				if (i == optionSize) {
					list += "</" + o.list + ">";
				}
			});
			list = $(list);
			dropdown = dropdown.find('.appendHere').removeClass('appendHere').append(list).end();
			opener.insertAfter(nativeSelect);
			opener.find('.' + o.selectedClass).text(nativeSelect.find('option:selected').text());
			body.append(dropdown);
			(o.dropdownHasBorder) ? dropdown.width(opener.width()) : dropdown.width(opener.outerWidth());
			if (o.transferClass) {
				opener.addClass(opener.attr('class') + " " + nativeSelect.attr('class'));
				dropdown.addClass(dropdown.attr('class') + " " + nativeSelect.attr('class'));
			}
			$(this).data('customSelect', opener.add(dropdown));
			$(this).data('customized', true);
			var listItems = list.find(">" + o.item),
				dropdownWidth = dropdown.outerWidth(),
				dropdownHeight = dropdown.outerHeight();
			selectedByHover = '',
			selected = '';
			if (!o.resizable) {
				opener.width(nativeSelect.outerWidth());
				(o.dropdownHasBorder) ? dropdownWidth = dropdown.width(opener.width()) : dropdownWidth = dropdown.width(opener.outerWidth());
			} else {
				$(window).on('resize.opener', function() {
					(o.dropdownHasBorder) ? dropdownWidth = dropdown.width(opener.width()) : dropdownWidth = dropdown.width(opener.outerWidth());
				}).trigger('resize.opener');
			}
			if (title) {
				opener.find('.' + o.selectedClass).text(title);
				nativeSelect.trigger('change' + o.namespace, [options.filter(':selected').index()]);
			}
			nativeSelect.on("change" + o.namespace, function(e, selectedIndex, dontHide, dontTrigger) {
				if (dontTrigger) return;
				if (!selectedIndex && selectedIndex !== 0) selectedIndex = this.selectedIndex;
				this.selectedIndex = selectedIndex;
				listItems.removeClass(o.activeItemClass).eq(selectedIndex).addClass(o.activeItemClass);
				selected = options.eq(selectedIndex);
				opener.find('.' + o.selectedClass).text(selected.text());
				if (!dontHide) {
					dropdown.hide();
					opener.removeClass(o.activeOpenerClass);
				}
				nativeSelect.trigger("change", [null, null, true]);
			});
			if (o.hasIcons) {
				options.each(function(i) {
					listItems.eq(i).prepend('<span class="' + this.className + '"></span>');
				});
				nativeSelect.on("change" + o.namespace, function(e, selectedIndex, dontHide, dontTrigger) {
					if (dontTrigger) return;
					opener.find('.' + o.selectedClass).prepend('<span class="' + selected.attr('class') + '"></span>');
				});
				opener.find('.' + o.selectedClass).prepend('<span class="' + options.filter(':selected').attr('class') + '"></span>');
			}
			nativeSelect.hide();
			listItems.click(function(e) {
				if (!$(this).hasClass(o.activeItemClass)) {
					nativeSelect.trigger("change" + o.namespace, [$(this).index()]);
				}
				dropdown.hide();
				opener.removeClass(o.activeOpenerClass);
			});
			opener.click(function(e) {
				if (dropdown.is(':hidden')) {
					dropdown.show();
					opener.addClass(o.activeOpenerClass);
					alignDropDown();
				} else {
					dropdown.hide();
					opener.removeClass(o.activeOpenerClass);
				}
			});
			nativeSelect.data("resizeHandler", function() {
				if (dropdown.is(':visible')) {
					alignDropDown();
				}
			});
			nativeSelect.data("mousedownHandler", function(e) {
				if (!$(e.target).closest(dropdown).size() && !$(e.target).closest(opener).size()) {
					dropdown.hide();
					opener.removeClass(o.activeOpenerClass);
				}
			});
			$(window).on('resize', nativeSelect.data("resizeHandler"));
			$(document).on('mousedown', nativeSelect.data("mousedownHandler"));
			//event section
			if (o.triggerEvents) {
				listItems.click(function(e) {
					nativeSelect.trigger(itemClick, [$(this).text()]);
				});
				nativeSelect.trigger(selectReady, [dropdown]);
			}

			function alignDropDown() {
				if (o.style == "dropdown") {
					var top = opener.offset().top + opener.outerHeight(),
						left = opener.offset().left;
					/*
if(top + dropdownHeight > $(window).height() && top - dropdownHeight - opener.outerHeight() > 0){
							dropdown.css({
								'top': top - dropdownHeight - opener.outerHeight(),
								'left': left
							});
						}else{
*/
					dropdown.css({
						'top': top,
						'left': left
					});
					/*
}
*/
				} else {
					var activeEl = listItems.eq(nativeSelect.get(0).selectedIndex);
					activeEl = activeEl.hasClass('title') ? activeEl.next() : activeEl;
					var top = opener.offset().top - activeEl.position().top,
						left = opener.offset().left;
					dropdown.css({
						'top': top,
						'left': left
					});
				}
			}
			if (nativeSelect.is(':disabled')) nativeSelect.select('disable');
		});
	} else {
		throw Error('селектор $("' + $(this).selector + '") ничего не возвратил');
	}
}