<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${ProceduralDocumentsDirectoryName}</label>
                <ul>
					<#list listProceduralDocuments as lr>
					<li><a href="#" onclick="connect('./object?ID=${lr.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${lr.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Procedural Document', ${ProceduralDocumentsDirectoryID?c}, 'proceduraldocuments', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addProceduralDocument}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>