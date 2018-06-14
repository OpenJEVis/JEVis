<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${LegalRegulationsDirectoryName}</label>
                <ul>
					<#list legalregulations as lr>
					<li><a href="#" onclick="connect('./object?ID=${lr.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${lr.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Legal Regulation', ${LegalRegulationsDirectoryID?c}, 'legalregulations', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addLegalRegulation}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>