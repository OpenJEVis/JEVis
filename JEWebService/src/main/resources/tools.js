function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires=" + d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/" + "; secure";
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

var lang;

function setLanguage(language) {
    window.lang = language;

}

function getLanguage(language) {
    return window.lang;
}

var currentNav;

function setCurrentNav(current) {
    window.currentNav = current;
}

function getCurrentNav() {
    return window.currentNav;
}

function loadJQueryFunctions() {
    $('.toggle').click(function () {
//        $(this).next('ul').slideToggle('500');
        $(this).find('svg').first().toggleClass('fa-angle-right fa-angle-down');
    });
}

var activeFormTab = "defaultTab";
var activeNavTab = "tabObjects";

function connect(url, bauth, target) {
    var xhr = new XMLHttpRequest();
    if (document.getElementById('loading') != null) {
        document.getElementById('loading').innerHTML = "<i class='fa fa-spinner fa-spin fa-2x'></i>";
    }
    xhr.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            if (document.getElementById('loading') != null) {
                document.getElementById('loading').innerHTML = "<i class='fa fa-spinner fa-spin fa-2x'></i>";
            }
            document.getElementById(target).innerHTML = xhr.responseText;
            if (target == 'bodyfornav') {
                cls();
                clearDashboard();
                document.getElementById('navbar-select-sites-id').getElementsByTagName('option')[0].selected = true;
                document.getElementById('navbar-select-sites-id').onchange();
            } else if (target == 'content-dash') {
                document.getElementById('content-nav').innerHTML = "";
                document.getElementById('content-nav').style.visibility = 'hidden';
                document.getElementById('content-dash-row').style.height = '100%';
                document.getElementById('content-form').innerHTML = "";
                document.getElementById('content-form').style.visibility = 'hidden';
                document.getElementById('button-show-charts').click();
                document.getElementById('button-show-charts').style.visibility = 'hidden';
                document.getElementById('content-dash').style.visibility = 'visible';

                document.getElementById('loading').innerHTML = "";
            } else if (target != 'navbar' && target != 'nav-site' && target != 'content-nav') {
                document.getElementById('main-view').style.visibility = 'visible';
                document.getElementById('content-dash-row').style.height = '0%';
                document.getElementById('content-nav').style.visibility = 'visible';
                document.getElementById('content-form').style.visibility = 'visible';

                document.getElementById('loading').innerHTML = "";
            } else if (target == 'content-nav') {
                document.getElementById('main-view').style.visibility = 'visible';
                document.getElementById('content-nav').style.visibility = 'visible';

                document.getElementById('loading').innerHTML = "";
            }
            if (target == 'content-nav' || target == 'content-form') {
                setCurrentNav(url);
            }
            loadJQueryFunctions();

            if (target == 'content-form') {
                document.getElementById(activeFormTab).click();
            }

            if (target == 'content-nav' && document.getElementById(activeNavTab) != null) {
                document.getElementById(activeNavTab).click();
            }
        }
    };
    if (target == 'content-form' || target == 'content-nav' || target == 'content-dash' || target == 'nav-site') {
        var lang = getLanguage();
        if (lang != "" && lang != null) {
            url += "&lang=" + lang;
        }
    }
    xhr.open("GET", encodeURI(url));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "text/html");
    xhr.send();
}

function login() {
    if (document.getElementById("input-username").value != "") {
        var user = document.getElementById("input-username").value;
        var pwd = document.getElementById("input-password").value;
        var token = user + ":" + pwd;
        var hash = btoa(token);
        var bauth = "Basic " + hash;
        var gbauth = getCookie("bauth");

        setCookie("username", user, 1);
        setCookie("password", pwd, 1);
        setCookie("bauth", bauth, 1);

        var theme = getParameterByName('theme');
        if (theme == "lcars.css") {
            connect("./navbar", bauth, "bodyfornav");
            document.getElementById('navigation-menu').className += ' lcars-column start-space lcars-u-1';
        } else {
            connect("./navbar", bauth, "bodyfornav");
        }
    }
}

function setContainerWidth(width) {
    var elements = document.getElementsByClassName("container");
    for (var i = 0; i < elements.length; i++) {
        elements[i].style.width = (width);
    }
}

function changeTheme(value) {
    window.open('./login?theme=' + value, '_self');
}

function getParameterByName(name, url) {
    if (!url)
        url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results)
        return null;
    if (!results[2])
        return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function updateQueryStringParameter(uri, key, value) {
    var re = new RegExp("([?|&])" + key + "=.*?(&|$)", "i");
    var separator = uri.indexOf('?') !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, '$1' + key + "=" + value + '$2');
    } else {
        return uri + separator + key + "=" + value;
    }
}

function addMinutes(date, minutes) {
    return new Date(date.getTime() + minutes * 60000);
}

function addDays(date, days) {
    return new Date(date.getTime() + days * 86400000);
}

function updateChart(chartName, fromDate, toDate, objectID, bauth) {
    if (fromDate != "" && toDate != "") {
        var from = new Date(fromDate);
        var to = new Date(toDate);

        var values = [];
        var xValues = [];

        var url = "./objects/" + objectID + "/attributes/Monitoring ID/samples";
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                var jsonResponse = JSON.parse(this.responseText);

                var monitoringID = jsonResponse[jsonResponse.length - 1].value;

                var fromDateISO = new Date(from.toISOString());
                var toDateISO = new Date(to.toISOString());
                var fromtz = fromDateISO.getTimezoneOffset() / 60;
                var totz = toDateISO.getTimezoneOffset() / 60;
                var fromDateForSamples = '' + fromDateISO.getFullYear() + '' + pad(fromDateISO.getMonth() + 1) + '' + pad(fromDateISO.getDate()) + 'T' + pad(fromDateISO.getHours() + fromtz) + pad(fromDateISO.getMinutes()) + pad(fromDateISO.getSeconds());
                var toDateForSamples = '' + toDateISO.getFullYear() + '' + pad(toDateISO.getMonth() + 1) + '' + pad(toDateISO.getDate()) + 'T' + pad(toDateISO.getHours() + totz) + pad(toDateISO.getMinutes()) + pad(toDateISO.getSeconds());
                var url = "./objects/" + monitoringID + "/attributes/Value/samples?from=" + fromDateForSamples + "&until=" + toDateForSamples;
                var xhr2 = new XMLHttpRequest();
                xhr2.onreadystatechange = function () {
                    if (this.readyState == 4 && this.status == 200) {
                        var json = JSON.parse(this.responseText);
                        for (var i in json) {
                            values.push(parseFloat(json[i].value));
                            xValues.push(new Date(json[i].ts).toDateString());
                        }

                        var XVALUES = xValues;
                        var config = {
                            type: 'line',
                            data: {
                                labels: xValues,
                                datasets: [{
                                    label: "",
                                    backgroundColor: window.chartColors.red,
                                    borderColor: window.chartColors.red,
                                    data: values,
                                    fill: false
                                }]
                            },
                            options: {
                                responsive: true,
                                title: {display: true, text: chartName},
                                legend: {display: false},
                                tooltips: {mode: 'index', intersect: false,},
                                hover: {mode: 'nearest', intersect: true},
                                scales: {
                                    xAxes: [{
                                        display: true,
                                        scaleLabel: {display: true, labelString: 'X-Axis'}
                                    }],
                                    yAxes: [{
                                        display: true,
                                        scaleLabel: {display: true, labelString: 'Y-Axis'},
                                        ticks: {beginAtZero: true}
                                    }]
                                }
                            }
                        };

                        var ctx = document.getElementById("equipment-canvas").getContext("2d");
                        window.myLine = new Chart(ctx, config);
                    }
                };
                xhr2.open("GET", encodeURI(url));
                xhr2.setRequestHeader("Authorization", bauth);
                xhr2.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
                xhr2.setRequestHeader("Accept", "application/json");
                xhr2.send();
            }
        };
        xhr.open("GET", encodeURI(url));
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.setRequestHeader("Accept", "application/json");
        xhr.send();
    }
}

function logo(bauth) {
    var url = "./resources/logo?org=true";
    var xhr = new XMLHttpRequest();
    xhr.responseType = "blob";
    xhr.open("GET", encodeURI(url));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "application/octet-stream");
    xhr.send();
    return xhr.responseText;
}

function createObject(jevisClass, parentObjectID, pathOfOrigin, siteName, bauth) {

    var newObject = prompt("Please enter new Name", "");
    if (newObject == null || newObject == "") {
        txt = "User cancelled the prompt.";
    } else {
        var json = JSON.stringify({"name": newObject, "jevisClass": jevisClass, "parent": parentObjectID});
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                if (siteName != "") {
                    connect('./' + pathOfOrigin + '?site=' + siteName, bauth, 'content-nav');
                    var jsonResponse = JSON.parse(xhr.responseText);
                    var newID = (jsonResponse["id"]);
                    connect('./object?ID=' + newID, bauth, 'content-form')
                } else {
                    connect('./' + pathOfOrigin, bauth, 'content-nav');
                    var jsonResponse = JSON.parse(xhr.responseText);
                    var newID = (jsonResponse["id"]);
                    connect('./object?ID=' + newID, bauth, 'content-form')
                }
            }
        };
        xhr.open("POST", encodeURI('./objects'));
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.send(json);
    }
}

function createRelationship(from, to, type, bauth) {
    var json = JSON.stringify({"from": from, "to": to, "type": type});
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var oldURL = getCurrentNav();
            console.log(oldURL);
            connect(oldURL, bauth, 'content-form');
        }
    };
    xhr.open("POST", encodeURI('./relationships'));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
    xhr.send(json);

}

function deleteRelationship(from, to, type, bauth) {
    if (confirm("")) {
        var url = "./relationships?from=" + from + "&to=" + to + "&type=" + type;
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                var oldURL = getCurrentNav();
                connect(oldURL, bauth, 'content-form');
            }
        };
        xhr.open("DELETE", url);
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.setRequestHeader("Accept", "application/json");
        xhr.send();
    }
}

function removeObject(objectID, bauth) {
    if (confirm("")) {
        var url = "./objects/" + objectID;
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                var oldURL = getCurrentNav();
                cls();
                connect(oldURL, bauth, 'content-nav');
            }
        };
        xhr.open("DELETE", url);
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.setRequestHeader("Accept", "application/json");
        xhr.send();
    }
}

function clearDashboard() {
    document.getElementById('content-dash-row').style.height = '0px';
    document.getElementById('content-dash').innerHTML = "";
    document.getElementById('content-dash').style.visibility = "hidden";
}

function cls() {
    document.getElementById('content-nav').innerHTML = "";
    document.getElementById('content-nav').style.visibility = "hidden";
    document.getElementById('content-form').innerHTML = "";
    document.getElementById('content-form').style.visibility = "hidden";
    document.getElementById('content-dash').innerHTML = "";
    document.getElementById('content-dash').style.visibility = "hidden";
}

function createInternalAudit(bauth, parent, siteName) {

    var newExternalAudit = prompt("Please enter new Audit Name", "");
    if (newExternalAudit == null || newExternalAudit == "") {
        txt = "User cancelled the prompt.";
    } else {
        var url = "./objects";
        var json = JSON.stringify({"name": newExternalAudit, "jevisClass": "Internal Audit", "parent": parent});
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                connect('./audits?site=' + siteName, bauth, 'content-main');
                var jsonResponse = JSON.parse(xhr.responseText);
                var newID = (jsonResponse["id"]);
                connect('./object?ID=' + newID, bauth, 'content-form');
            }
        };
        xhr.open("POST", encodeURI(url));
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.setRequestHeader("Accept", "application/json");
        xhr.onload = function () {
            var jsonResponse = JSON.parse(xhr.responseText);
            var newExtID = (jsonResponse["id"]);
            var isogeneral = JSON.stringify({"name": "01 General", "jevisClass": "01 General", "parent": newExtID});
            var isoplan = JSON.stringify({"name": "02 Plan", "jevisClass": "02 Plan", "parent": newExtID});
            var isodo = JSON.stringify({"name": "03 Do", "jevisClass": "03 Do", "parent": newExtID});
            var isocheck = JSON.stringify({"name": "04 Check", "jevisClass": "04 Check", "parent": newExtID});
            var isoact = JSON.stringify({"name": "05 Act", "jevisClass": "05 Act", "parent": newExtID});
            var xhr01 = new XMLHttpRequest();
            xhr01.open("POST", encodeURI(url));
            xhr01.setRequestHeader("Authorization", bauth);
            xhr01.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr01.setRequestHeader("Accept", "application/json");
            xhr01.send(isogeneral);
            var xhr02 = new XMLHttpRequest();
            xhr02.open("POST", encodeURI(url));
            xhr02.setRequestHeader("Authorization", bauth);
            xhr02.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr02.setRequestHeader("Accept", "application/json");
            xhr02.send(isoplan);
            var xhr03 = new XMLHttpRequest();
            xhr03.open("POST", encodeURI(url));
            xhr03.setRequestHeader("Authorization", bauth);
            xhr03.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr03.setRequestHeader("Accept", "application/json");
            xhr03.send(isodo);
            var xhr04 = new XMLHttpRequest();
            xhr04.open("POST", encodeURI(url));
            xhr04.setRequestHeader("Authorization", bauth);
            xhr04.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr04.setRequestHeader("Accept", "application/json");
            xhr04.send(isocheck);
            var xhr05 = new XMLHttpRequest();
            xhr05.open("POST", encodeURI(url));
            xhr05.setRequestHeader("Authorization", bauth);
            xhr05.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr05.setRequestHeader("Accept", "application/json");
            xhr05.send(isoact);
        };
        xhr.send(json);
    }
}

function createActionPlan(bauth, parent, siteName) {

    var newActionPlan = prompt("Please enter new Action Plan Name", "");
    if (newActionPlan == null || newActionPlan == "") {
        txt = "User cancelled the prompt.";
    } else {
        var url = "./objects";
        var json = JSON.stringify({"name": newActionPlan, "jevisClass": "Action Plan", "parent": parent});
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
            if (this.readyState == 4 && this.status == 200) {
                connect('./actionplans?site=' + siteName, bauth, 'content-nav');
                var jsonResponse = JSON.parse(xhr.responseText);
                var newID = (jsonResponse["id"]);
                connect('./object?ID=' + newID, bauth, 'content-form');
            }
        };
        xhr.open("POST", encodeURI(url));
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhr.setRequestHeader("Accept", "application/json");
        xhr.onload = function () {
            var jsonResponse = JSON.parse(xhr.responseText);
            var newExtID = (jsonResponse["id"]);
            var implementedactions = JSON.stringify({
                "name": "Implemented Actions Directory",
                "jevisClass": "Implemented Actions Directory",
                "parent": newExtID
            });
            var plannedactions = JSON.stringify({
                "name": "Planned Actions Directory",
                "jevisClass": "Planned Actions Directory",
                "parent": newExtID
            });
            var xhr01 = new XMLHttpRequest();
            xhr01.open("POST", encodeURI(url));
            xhr01.setRequestHeader("Authorization", bauth);
            xhr01.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr01.setRequestHeader("Accept", "application/json");
            xhr01.send(implementedactions);
            var xhr02 = new XMLHttpRequest();
            xhr02.open("POST", encodeURI(url));
            xhr02.setRequestHeader("Authorization", bauth);
            xhr02.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
            xhr02.setRequestHeader("Accept", "application/json");
            xhr02.send(plannedactions);
        };
        xhr.send(json);
    }
}

function pad(n) {
    return n < 10 ? '0' + n : n
}

function download(attName, formID, bauth, filename) {
    var url = "./objects/" + formID + "/attributes/" + attName + "/samples/files/latest";
    var xhr = new XMLHttpRequest();
    xhr.responseType = "blob";
    xhr.onload = function () {
        var a = document.createElement('a');
        a.href = window.URL.createObjectURL(xhr.response);
        a.download = filename;
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click();
        delete a;
    };
    xhr.open("GET", encodeURI(url));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "application/octet-stream");
    xhr.send();
}

function sendUnitToJEWebService(bauth, formID, attName, unit) {
    var now = new Date();
    var nowFormatted = now.toISOString();
    var xhr = new XMLHttpRequest();
    xhr.open("POST", encodeURI("./objects/" + formID + "/attributes/" + attName));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var json = {
        "type": attName,
        "inputUnit": {"formula": unit, "label": unit, "prefix": "None"},
        "displayUnit": {"formula": unit, "label": unit, "prefix": "None"}
    };
    var x = JSON.stringify(json);
    xhr.send(x);
}

function sendValueToJEWebService(bauth, formID, attName, value) {
    var now = new Date();
    var nowFormatted = now.toISOString();
    var xhr = new XMLHttpRequest();
    xhr.open("POST", encodeURI("./objects/" + formID + "/attributes/" + attName + "/samples"));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var json = [{"ts": nowFormatted, "value": value}];
    var x = JSON.stringify(json);
    xhr.send(x);
}

function sendNameToJEWebService(bauth, formID, value) {
    var xhrName = new XMLHttpRequest();
    xhrName.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            var oldURL = getCurrentNav();
            connect(oldURL, bauth, 'content-nav');
        }
    };
    xhrName.open("POST", encodeURI("./objects/" + formID));
    xhrName.setRequestHeader("Authorization", bauth);
    xhrName.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var jsonName = {"id": formID, "name": value};
    var xName = JSON.stringify(jsonName);
    xhrName.send(xName);
}

function sendDateToJEWebService(bauth, formID, attName, value) {
    var newdate = new Date(value);
    var now = new Date();
    var nowFormatted = now.toISOString();
    var formatdate = pad(newdate.getDate()) + "." + pad(newdate.getMonth() + 1) + "." + newdate.getFullYear();
    var xhr = new XMLHttpRequest();
    xhr.open("POST", encodeURI("./objects/" + formID + "/attributes/" + attName + "/samples"));
    xhr.setRequestHeader("Authorization", bauth);
    xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    var json = [{"ts": nowFormatted, "value": formatdate}];
    var x = JSON.stringify(json);
    xhr.send(x);
}

function sendFileToJEWebService(bauth, formID, attName, fileName, value) {
    if (fileName != "") {
        var now = new Date();
        fileName = fileName.replace(/.*[\/\\]/, '');
        now.setMilliseconds(0);
        var dateISO = new Date(now.toISOString());
        var tz = dateISO.getTimezoneOffset() / 60;
        var dateForFile = dateISO.getFullYear() + pad(dateISO.getMonth() + 1) + pad(dateISO.getDate()) + 'T' + pad(dateISO.getHours() + tz) + pad(dateISO.getMinutes()) + pad(dateISO.getSeconds());
        var xhr = new XMLHttpRequest();
        xhr.open("POST", encodeURI("./objects/" + formID + "/attributes/" + attName + "/samples/files/" + dateForFile + "?filename=" + fileName));
        xhr.setRequestHeader("Authorization", bauth);
        xhr.setRequestHeader("Content-Type", "application/octet-stream");
        xhr.send(value.files[0]);

    }
}

function showCharts(energysource1, energysource1name,
                    listenergyconsumptionlastname, l1Jan, l1Feb, l1Mar, l1Apr, l1May, l1Jun, l1Jul, l1Aug, l1Sep, l1Oct, l1Nov, l1Dec,
                    listenergyconsumptionlastlastname, ll1Jan, ll1Feb, ll1Mar, ll1Apr, ll1May, ll1Jun, ll1Jul, ll1Aug, ll1Sep, ll1Oct, ll1Nov, ll1Dec,
                    listenergyconsumptionlastlastlastname, lll1Jan, lll1Feb, lll1Mar, lll1Apr, lll1May, lll1Jun, lll1Jul, lll1Aug, lll1Sep, lll1Oct, lll1Nov, lll1Dec,
                    lastTotalEnPI, lastEnPIJan, lastEnPIFeb, lastEnPIMar, lastEnPIApr, lastEnPIMay, lastEnPIJun, lastEnPIJul, lastEnPIAug, lastEnPISep, lastEnPIOct, lastEnPINov, lastEnPIDec,
                    lastlastTotalEnPI, lastLastEnPIJan, lastLastEnPIFeb, lastLastEnPIMar, lastLastEnPIApr, lastLastEnPIMay, lastLastEnPIJun, lastLastEnPIJul, lastLastEnPIAug, lastLastEnPISep, lastLastEnPIOct, lastLastEnPINov, lastLastEnPIDec,
                    lastlastlastTotalEnPI, lastLastLastEnPIJan, lastLastLastEnPIFeb, lastLastLastEnPIMar, lastLastLastEnPIApr, lastLastLastEnPIMay, lastLastLastEnPIJun, lastLastLastEnPIJul, lastLastLastEnPIAug, lastLastLastEnPISep, lastLastLastEnPIOct, lastLastLastEnPINov, lastLastLastEnPIDec,
                    energysource2, energysource2name, l2Jan, l2Feb, l2Mar, l2Apr, l2May, l2Jun, l2Jul, l2Aug, l2Sep, l2Oct, l2Nov, l2Dec,
                    ll2Jan, ll2Feb, ll2Mar, ll2Apr, ll2May, ll2Jun, ll2Jul, ll2Aug, ll2Sep, ll2Oct, ll2Nov, ll2Dec,
                    lll2Jan, lll2Feb, lll2Mar, lll2Apr, lll2May, lll2Jun, lll2Jul, lll2Aug, lll2Sep, lll2Oct, lll2Nov, lll2Dec,
                    energysource3, energysource3name, l3Jan, l3Feb, l3Mar, l3Apr, l3May, l3Jun, l3Jul, l3Aug, l3Sep, l3Oct, l3Nov, l3Dec,
                    ll3Jan, ll3Feb, ll3Mar, ll3Apr, ll3May, ll3Jun, ll3Jul, ll3Aug, ll3Sep, ll3Oct, ll3Nov, ll3Dec,
                    lll3Jan, lll3Feb, lll3Mar, lll3Apr, lll3May, lll3Jun, lll3Jul, lll3Aug, lll3Sep, lll3Oct, lll3Nov, lll3Dec,
                    energysource4, energysource4name, l4Jan, l4Feb, l4Mar, l4Apr, l4May, l4Jun, l4Jul, l4Aug, l4Sep, l4Oct, l4Nov, l4Dec,
                    ll4Jan, ll4Feb, ll4Mar, ll4Apr, ll4May, ll4Jun, ll4Jul, ll4Aug, ll4Sep, ll4Oct, ll4Nov, ll4Dec,
                    lll4Jan, lll4Feb, lll4Mar, lll4Apr, lll4May, lll4Jun, lll4Jul, lll4Aug, lll4Sep, lll4Oct, lll4Nov, lll4Dec,
                    energysource5, energysource5name, l5Jan, l5Feb, l5Mar, l5Apr, l5May, l5Jun, l5Jul, l5Aug, l5Sep, l5Oct, l5Nov, l5Dec,
                    ll5Jan, ll5Feb, ll5Mar, ll5Apr, ll5May, ll5Jun, ll5Jul, ll5Aug, ll5Sep, ll5Oct, ll5Nov, ll5Dec,
                    lll5Jan, lll5Feb, lll5Mar, lll5Apr, lll5May, lll5Jun, lll5Jul, lll5Aug, lll5Sep, lll5Oct, lll5Nov, lll5Dec,
                    Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec,
                    totalEnPIs) {
    var MONTHS = [Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec];
    if (energysource1 == true) {
        var configConsumption1 = {
            type: 'line',
            data: {
                labels: MONTHS,
                datasets: [
                    {
                        label: listenergyconsumptionlastname,
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: [l1Jan, l1Feb, l1Mar, l1Apr, l1May, l1Jun, l1Jul, l1Aug, l1Sep, l1Oct, l1Nov, l1Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastname,
                        backgroundColor: window.chartColors.blue,
                        borderColor: window.chartColors.blue,
                        data: [ll1Jan, ll1Feb, ll1Mar, ll1Apr, ll1May, ll1Jun, ll1Jul, ll1Aug, ll1Sep, ll1Oct, ll1Nov, ll1Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastlastname,
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: [lll1Jan, lll1Feb, lll1Mar, lll1Apr, lll1May, lll1Jun, lll1Jul, lll1Aug, lll1Sep, lll1Oct, lll1Nov, lll1Dec],
                        fill: false
                    }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: energysource1name,
                    fontStyle: 'bold',
                    fontSize: 16
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        },
                        ticks: {beginAtZero: true}
                    }]
                }
            }
        };
        var configTotalEnPIs = {
            type: 'line',
            data: {
                labels: MONTHS,
                datasets: [
                    {
                        label: listenergyconsumptionlastname,
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: [lastEnPIJan, lastEnPIFeb, lastEnPIMar, lastEnPIApr, lastEnPIMay, lastEnPIJun, lastEnPIJul, lastEnPIAug, lastEnPISep, lastEnPIOct, lastEnPINov, lastEnPIDec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastname,
                        backgroundColor: window.chartColors.blue,
                        borderColor: window.chartColors.blue,
                        data: [lastLastEnPIJan, lastLastEnPIFeb, lastLastEnPIMar, lastLastEnPIApr, lastLastEnPIMay, lastLastEnPIJun, lastLastEnPIJul, lastLastEnPIAug, lastLastEnPISep, lastLastEnPIOct, lastLastEnPINov, lastLastEnPIDec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastlastname,
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: [lastLastLastEnPIJan, lastLastLastEnPIFeb, lastLastLastEnPIMar, lastLastLastEnPIApr, lastLastLastEnPIMay, lastLastLastEnPIJun, lastLastLastEnPIJul, lastLastLastEnPIAug, lastLastLastEnPISep, lastLastLastEnPIOct, lastLastLastEnPINov, lastLastLastEnPIDec],
                        fill: false
                    }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: totalEnPIs,
                    fontStyle: 'bold',
                    fontSize: 16
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        },
                        ticks: {beginAtZero: true}
                    }]
                }
            }
        };
        var ctxConsumption1 = document.getElementById("canvasConsumption1").getContext("2d");
        window.myLine = new Chart(ctxConsumption1, configConsumption1);
        var ctxTotalEnPIs = document.getElementById("canvasTotalEnPIs").getContext("2d");
        window.myLine = new Chart(ctxTotalEnPIs, configTotalEnPIs);
    }
    if (energysource2 == true) {
        var configConsumption2 = {
            type: 'line',
            data: {
                labels: MONTHS,
                datasets: [
                    {
                        label: listenergyconsumptionlastname,
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: [l2Jan, l2Feb, l2Mar, l2Apr, l2May, l2Jun, l2Jul, l2Aug, l2Sep, l2Oct, l2Nov, l2Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastname,
                        backgroundColor: window.chartColors.blue,
                        borderColor: window.chartColors.blue,
                        data: [ll2Jan, ll2Feb, ll2Mar, ll2Apr, ll2May, ll2Jun, ll2Jul, ll2Aug, ll2Sep, ll2Oct, ll2Nov, ll2Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastlastname,
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: [lll2Jan, lll2Feb, lll2Mar, lll2Apr, lll2May, lll2Jun, lll2Jul, lll2Aug, lll2Sep, lll2Oct, lll2Nov, lll2Dec],
                        fill: false
                    }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: energysource2name,
                    fontStyle: 'bold',
                    fontSize: 16
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        },
                        ticks: {beginAtZero: true}
                    }]
                }
            }
        };
        var ctxConsumption2 = document.getElementById("canvasConsumption2").getContext("2d");
        window.myLine = new Chart(ctxConsumption2, configConsumption2);
    }
    if (energysource3 == true) {
        var configConsumption3 = {
            type: 'line',
            data: {
                labels: MONTHS,
                datasets: [
                    {
                        label: listenergyconsumptionlastname,
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: [l3Jan, l3Feb, l3Mar, l3Apr, l3May, l3Jun, l3Jul, l3Aug, l3Sep, l3Oct, l3Nov, l3Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastname,
                        backgroundColor: window.chartColors.blue,
                        borderColor: window.chartColors.blue,
                        data: [ll3Jan, ll3Feb, ll3Mar, ll3Apr, ll3May, ll3Jun, ll3Jul, ll3Aug, ll3Sep, ll3Oct, ll3Nov, ll3Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastlastname,
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: [lll3Jan, lll3Feb, lll3Mar, lll3Apr, lll3May, lll3Jun, lll3Jul, lll3Aug, lll3Sep, lll3Oct, lll3Nov, lll3Dec],
                        fill: false
                    }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: energysource3name,
                    fontStyle: 'bold',
                    fontSize: 16
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        },
                        ticks: {beginAtZero: true}
                    }]
                }
            }
        };
        var ctxConsumption3 = document.getElementById("canvasConsumption3").getContext("2d");
        window.myLine = new Chart(ctxConsumption3, configConsumption3);
    }
    if (energysource4 == true) {
        var configConsumption4 = {
            type: 'line',
            data: {
                labels: MONTHS,
                datasets: [
                    {
                        label: listenergyconsumptionlastname,
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: [l4Jan, l4Feb, l4Mar, l4Apr, l4May, l4Jun, l4Jul, l4Aug, l4Sep, l4Oct, l4Nov, l4Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastname,
                        backgroundColor: window.chartColors.blue,
                        borderColor: window.chartColors.blue,
                        data: [ll4Jan, ll4Feb, ll4Mar, ll4Apr, ll4May, ll4Jun, ll4Jul, ll4Aug, ll4Sep, ll4Oct, ll4Nov, ll4Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastlastname,
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: [lll4Jan, lll4Feb, lll4Mar, lll4Apr, lll4May, lll4Jun, lll4Jul, lll4Aug, lll4Sep, lll4Oct, lll4Nov, lll4Dec],
                        fill: false
                    }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: energysource4name,
                    fontStyle: 'bold',
                    fontSize: 16
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        },
                        ticks: {beginAtZero: true}
                    }]
                }
            }
        };
        var ctxConsumption4 = document.getElementById("canvasConsumption4").getContext("2d");
        window.myLine = new Chart(ctxConsumption4, configConsumption4);
    }
    if (energysource5 == true) {
        var configConsumption5 = {
            type: 'line',
            data: {
                labels: MONTHS,
                datasets: [
                    {
                        label: listenergyconsumptionlastname,
                        backgroundColor: window.chartColors.red,
                        borderColor: window.chartColors.red,
                        data: [l5Jan, l5Feb, l5Mar, l5Apr, l5May, l5Jun, l5Jul, l5Aug, l5Sep, l5Oct, l5Nov, l5Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastname,
                        backgroundColor: window.chartColors.blue,
                        borderColor: window.chartColors.blue,
                        data: [ll5Jan, ll5Feb, ll5Mar, ll5Apr, ll5May, ll5Jun, ll5Jul, ll5Aug, ll5Sep, ll5Oct, ll5Nov, ll5Dec],
                        fill: false,
                    },
                    {
                        label: listenergyconsumptionlastlastlastname,
                        backgroundColor: window.chartColors.green,
                        borderColor: window.chartColors.green,
                        data: [lll5Jan, lll5Feb, lll5Mar, lll5Apr, lll5May, lll5Jun, lll5Jul, lll5Aug, lll5Sep, lll5Oct, lll5Nov, lll5Dec],
                        fill: false
                    }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: energysource5name,
                    fontStyle: 'bold',
                    fontSize: 16
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: ''
                        },
                        ticks: {beginAtZero: true}
                    }]
                }
            }
        };
        var ctxConsumption5 = document.getElementById("canvasConsumption5").getContext("2d");
        window.myLine = new Chart(ctxConsumption, configConsumption5);
    }
}

function openFormTab(evt, tabName) {
    // Declare all variables
    var i, tabcontent, tablinks;

    // Get all elements with class="tabcontent" and hide them
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the button that opened the tab
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
    activeFormTab = evt.currentTarget.id;
}

function openNavTab(evt, tabNavName) {
    // Declare all variables
    var i, tabnavcontent, tabnavlinks;

    // Get all elements with class="tabcontent" and hide them
    tabnavcontent = document.getElementsByClassName("tabnavcontent");
    for (i = 0; i < tabnavcontent.length; i++) {
        tabnavcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tabnavlinks = document.getElementsByClassName("tabnavlinks");
    for (i = 0; i < tabnavlinks.length; i++) {
        tabnavlinks[i].className = tabnavlinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the button that opened the tab
    document.getElementById(tabNavName).style.display = "block";
    evt.currentTarget.className += " active";
    activeNavTab = evt.currentTarget.id;
}