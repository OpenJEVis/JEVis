<html>
<head>
    <meta charset="utf-8"/>
    <link rel="stylesheet" href="./resources/styles">
    <link rel="stylesheet" href="./resources/styles?theme=${themeValue}">
    <script src="./resources/scripts?name=fontawesome-all.js" type="text/javascript" defer></script>
    <script src="./resources/scripts?name=jquery.js" type="text/javascript"></script>
    <script src="./resources/scripts?name=tools.js" type="text/javascript"></script>
    <script src="./resources/chartbundle" type="text/javascript"></script>
    <style type="text/css">/* Chart.js */
    @-webkit-keyframes chartjs-render-animation {
        from {
            opacity: 0.99
        }
        to {
            opacity: 1
        }
    }

    @keyframes chartjs-render-animation {
        from {
            opacity: 0.99
        }
        to {
            opacity: 1
        }
    }

    .chartjs-render-monitor {
        -webkit-animation: chartjs-render-animation 0.001s;
        animation: chartjs-render-animation 0.001s;
    }</style>
    <script src="./resources/utils" type="text/javascript"></script>
    <script type="text/javascript">
        window.onload = function () {
            var check = getCookie("bauth");
            if (check != "") {
                alert("Welcome again!");
                connect("./navbar", bauth, "bodyfornav");
            }
            var theme = getParameterByName('theme');
            if (theme == "lcars.css") {
                document.getElementById('bodyfornav').className += ' lcars-app-container';
            }
        }
    </script>
    <title>ISO 50001</title>
</head>

<body>
<div class="body-background" id="bodyfornav" style="height: 100%;">
    <select style="float: right" onchange="changeTheme(this.value);">
        <option value="${themeValue}">${themeName}</option>
				<#list themes as t>
				<option value="${t.fileName}">${t.name}</option>
                </#list>
    </select>
    <div class="center-login" style="background: #fff; border: 1px solid black; padding: 20px;">
        <a style="padding: 0px 0px; vertical-align: middle;" href="" onclick=""><img src="./resources/logo"
                                                                                     height="46px"></a>
        <br/><br/>
        <form id="form-login" onsubmit="login(); return false;" style="width: 200px;">
            <div class="wrapper" style="width: 200px;"><input class="input input-login" placeholder="${username}"
                                                              type="text" id="input-username"
                                                              style="background-color: rgb(250, 255, 189); background-image: none; color: rgb(0, 0, 0); height: auto;"><span
                    class="underline"></span></div>
            <br/>
            <div class="wrapper" style="width: 200px;"><input class="input input-login" placeholder="${password}"
                                                              type="password" id="input-password"
                                                              style="background-color: rgb(250, 255, 189); background-image: none; color: rgb(0, 0, 0); height: auto;"><span
                    class="underline"></span></div>
            <br/>
            <select name="select-name" id="select-lang-id" onchange="setLanguage(this.value);">
                <option id="option-00">English</option>
					<#list lang as l>
					<option id="option-${l?index}" value="${l.fileName}">${l.name}</option>
                    </#list>
            </select>
            <input type="submit" id="input-submit" value="${submit}" style="float: right;">
        </form>
    </div>
</div>
</body>
</html>