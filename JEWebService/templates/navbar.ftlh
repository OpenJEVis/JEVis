<div class="top-header">
    <div class="container-nav">
        <label class="mobile_menu" for="mobile_menu">
            <span>Menu</span>
        </label>
        <input id="mobile_menu" type="checkbox">
        <ul class="nav" id="navigation-menu">
            <li><a href="" onclick="cls(); connect('./navbar', '${bauth}', 'bodyfornav'); return false;"><i
                    class="fa fa-home fa-2x"></i></a></li>
            <li id="loading"><i class='fa fa-spinner fa-spin fa-2x'></i></li>
				<#if SupMeetingsDirName != "">
				<li><a href="#"
                       onclick="cls(); connect('./superiormeetings?', '${bauth}', 'content-nav'); clearDashboard();">${SupMeetingsDirName}</a>
                </li>
                </#if>
            <select id="navbar-select-sites-id" class="navbar-select-sites"
                    onchange="cls(); connect('./nav?site=' + this.value, '${bauth}', 'nav-site'); connect('./dashboard?site=' + this.value, '${bauth}', 'content-dash');">
					<#list sites as s>
                        <option value="${s}">${s}</option>
                    </#list>
            </select>
            <li class="dropdown" id="nav-site">
            </li>
            <!-- <li style="float: right;"> -->
            <!-- <div> -->
            <!-- <select style="padding: 0px 0px; vertical-align: middle;" onchange="setContainerWidth(this.value)">		 -->
            <!-- <option value="100%">100%</option>					 -->
            <!-- <option value="90%">90%</option>						 -->
            <!-- <option value="75%">75%</option> -->
            <!-- <option value="50%">50%</option> -->
            <!-- </select> -->
            <!-- </div> -->
            <!-- </li> -->
            <li><a href="#" style="text-align: right;"
                   onclick="cls(); connect('./tree?', '${bauth}', 'content-nav'); clearDashboard();">JEVis Web</a>
            </li>
            <li id="logo"><a style="padding: 0px 0px; vertical-align: middle;" href=""
                             onclick="cls(); connect('./navbar', '${bauth}', 'bodyfornav'); return false;"><img
                    src="./resources/logo" height="46px"></a></li>
        </ul>

    </div>
</div>
	  <div class="container" id="main-view"
           style="padding: 0px; height: calc(100vh - 93px); overflow: hidden; visibility: hidden;">
          <div style="width: 100%; height: 100%; border-width: 0px;">
              <div class="row" id="content-dash-row">
                  <div id="content-dash"></div>
              </div>
              <div class="row">
                  <div class="body-column" id="content-nav"></div>

                  <div class="body-column data-form-container" id="content-form"></div>
              </div>

          </div>
      </div>