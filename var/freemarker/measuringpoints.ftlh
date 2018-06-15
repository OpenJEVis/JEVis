<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${MeasuringPointsDirectoryName}</label>
                <ul>
					<#list listMeasuringPoints as mp>
					<li><a href="#" onclick="connect('./object?ID=${mp.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${mp.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Measuring Point', ${MeasuringPointsDirectoryID?c}, 'measuringpoints', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addMeasuringPoint}</a></li>
                </ul>
        </ul>
    </div>
</nav>