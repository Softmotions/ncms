(function () {
    console.log("Init ncms visual editor");
    var editor = new MediumEditor(".ncms-block", {
        spellcheck: false,
        toolbar: {
            //"static": true,
            //align: 'center',
            //sticky: true
        }
    });
})();
