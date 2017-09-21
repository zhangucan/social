$(document)
    .ready(function() {
        // fix main menu to page on passing
        $('.main.menu').visibility({
            type: 'fixed',
        });
        $('.accordion').accordion();

        $('.overlay').visibility({
            type: 'fixed',
            offset: 80
        });

        // lazy load images
        $('.image').visibility({
            type: 'image',
            transition: 'vertical flip in',
            duration: 500
        });

        // show dropdown on hover
        $('.main.menu  .ui.dropdown').dropdown({
            on: 'hover'
        });
        $('#contentor').masonry({
            itemSelector: '.card',
            columnWidth: 11
        });


        $('  .menu .item')
            .tab()
        ;
    });
