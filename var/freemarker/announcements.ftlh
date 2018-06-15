<nav class="vertical-navbar">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${AnnouncementsDirectoryName}</label>
                <ul>
				<#list listYears as year>				
					<ul>
                        <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                class="toggle" for="item-0-${year?index}"><i class="fa fa-angle-right"></i>    ${year?c}
                        </label>
                            <ul>
								<#list announcements as a>
								<#if a.year == year>
								<li><a href="#"
                                       onclick="connect('./object?ID=${a.ID?c}', '${bauth}', 'content-form')"><i
                                        class="fa fa-edit"></i> ${a.name}</a></li>
                                </#if>
                                </#list>
                            </ul>
                        </li>
                    </ul>
                </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Announcement', '${AnnouncementsDirectoryID?c}', 'announcements', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addAnnouncement}</a></li>
                </ul>
        </ul>
    </div>
</nav>