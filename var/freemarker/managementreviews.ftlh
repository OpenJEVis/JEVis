<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${ManagementReviewsDirectoryName}</label>
                <ul>
					<#list listYears as year>				
					<ul>
                        <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                class="toggle" for="item-0-${year?index}"><i class="fa fa-angle-right"></i> ${year?c}
                        </label>
                            <ul>
								<#list managementreviews as m>
								<#if m.year == year>
								<li><a href="#"
                                       onclick="connect('./object?ID=${m.ID?c}', '${bauth}', 'content-form')"><i
                                        class="fa fa-edit"></i>  ${m.name}</a></li>
								<li class="add-item"><a href="managementreviews/print?site=${siteName}&id=${m.ID?c}"
                                                        target="blank"><i class="fa fa-print"></i> Print</a></li>
                                </#if>
                                </#list>
                            </ul>
                        </li>
                    </ul>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Management Review', ${ManagementReviewsDirectoryID?c}, 'managementreviews', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addManagementReview}</a></li>
                </ul>
        </ul>
    </div>
</nav>