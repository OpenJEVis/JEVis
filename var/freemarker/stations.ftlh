<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${StationDirectoryName}</label>
                <ul>
					<#list listStations as s>
					<li><a href="#" onclick="connect('./object?ID=${s.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${s.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Station', ${StationDirectoryID?c}, 'stations', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addStation}</a></li>

                </ul>
        </ul>
    </div>
</nav>