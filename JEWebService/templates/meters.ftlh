<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${MeterDirectoryName}</label>
                <ul>
					<#list listMeters as m>
					<li><a href="#" onclick="connect('./object?ID=${m.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${m.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Meter', ${MeterDirectoryID?c}, 'meters', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addMeter}</a></li>
                </ul>
        </ul>
    </div>
</nav>