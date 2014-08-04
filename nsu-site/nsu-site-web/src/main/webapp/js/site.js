jQuery(function() {
    initTree();
    initMainMenu();
});

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

function initMainMenu() {
    var _nav = $('#nav');
    _nav.find('> ul > li > a').hover(function(event) {
        var _this = $(this);
        if (_nav.find('> ul > li.active').length == 0) {
            return;
        }
        if (!_this.parent().hasClass("active")) {
            $('#nav').find('> ul > li.active').removeClass('active');
            _this.parent().addClass('active');
            $('.header-holder').addClass('active');

        }
    }, function() {

    });
    _nav.find('> ul > li > a').click(function(event) {
        var _this = $(this);
        if (_this.parent().find('.slide-holder').size()) {
            if (!_this.parent().hasClass('active')) {
                $('#nav').find('> ul > li.active').removeClass('active');
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
        if ($(event.target).closest(".header-holder").length) {
            return;
        }
        $(".header-holder").removeClass('active');
        $('#nav').find('> ul > li.active').removeClass('active');
        event.stopPropagation();
    })
}

