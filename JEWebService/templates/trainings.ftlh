<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${TrainingsDirectoryName}</label>
                <ul>
					<#list listYears as year>				
						<ul>
                            <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                    class="toggle" for="item-0-${year?index}"><i
                                    class="fa fa-angle-right"></i> ${year?c}</label>
                                <ul>
									<#list trainings as t>
                                        <li><a href="#"
                                               onclick="connect('./object?ID=${t.ID?c}', '${bauth}', 'content-form')"><i
                                                class="fa fa-edit"></i> ${t.name}</a></li>
                                    </#list>
                                </ul>
                            </li>
                        </ul>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Training', '${TrainingsDirectoryID?c}', 'trainings', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addTraining}</a></li>
                </ul>
        </ul>
    </div>
</nav>