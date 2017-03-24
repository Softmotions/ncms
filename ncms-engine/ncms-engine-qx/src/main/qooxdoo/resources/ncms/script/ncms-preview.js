(function () {

    function init() {
        window.removeEventListener("load", init, false);
        var appRoot = document.querySelector("[data-ncms-root]");
        if (appRoot) {
            appRoot = appRoot.getAttribute("data-ncms-root");
        }
        if (appRoot == null) {
            console.error("Failed to find ncms-preview meta");
            return;
        }
        console.log("Init ncms visual editor. App root: " + (appRoot == "") ? "/" : appRoot);
        var sections = {};
        var editor = new MediumEditor(".ncms-block", {
            spellcheck: false,
            toolbar: {
                buttons: [
                    'bold', 'italic', 'anchor',
                    'h1', 'h2', 'h3',
                    'orderedlist',
                    'unorderedlist',
                    'pre', 'removeFormat']
                //"static": true,
                //align: 'center',
                //sticky: true
            }
        });

        function getSectionName(editable) {
            return editable.getAttribute("data-ncms-block")
        }

        function flushSections() {
            var msections = Object.keys(sections)
            .filter(function (k) {
                return !!(sections[k] && sections[k].durty && sections[k].html);
            })
            .map(function (k) {
                return {
                    section: k,
                    html: sections[k].html
                }
            });
            msections.forEach(function (s) {
                delete sections[s.section];
            });
            // It will be very uncommon that `msections.length > 1`
            msections.forEach(function (s) {
                // send http request
                console.log("flush " + s.section);
                var req = new XMLHttpRequest();
                var data = JSON.stringify(s);
                req.onerror = function () {
                    console.error(
                        "Failed to save section: " + data +
                        " status: " + this.statusText);
                };
                console.log("window.parent=" + window.parent);
                var url = appRoot + "/rs/adm/am/ve/save";
                var parent = window.parent;
                if (parent != null && parent.ncms != null) {
                    url += "?__app=" + encodeURIComponent(parent.ncms.Application.UUID);
                }
                req.open("PUT", url);
                req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
                req.send(data);
            });
        }

        editor.subscribe("editableInput", function (data, editable) {
            var sn = getSectionName(editable);
            if (!sn) return;
            sections[sn] = sections[sn] || {};
            sections[sn].durty = true;
            sections[sn].html = editable.innerHTML;
        });

        editor.subscribe("blur", function (data, editable) {
            var sn = getSectionName(editable);
            if (!sn || !sections[sn] || !sections[sn].durty) return;
            sections[sn].html = editable.innerHTML;
            flushSections();
        });

        window.addEventListener("unload", function () {
            flushSections();
        });
    }

    window.addEventListener("load", init, false);
})();
