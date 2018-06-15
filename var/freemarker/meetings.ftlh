<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${MeetingDirectoryName}</label>
                <ul>
					<#list listYears as year>				
						<ul>
                            <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                    class="toggle" for="item-0-${year?index}"><i
                                    class="fa fa-angle-right"></i> ${year?c}</label>
                                <ul>
									<#list meetings as m>
									<#if m.year == year>
									<li><a href="#"
                                           onclick="connect('./object?ID=${m.ID?c}', '${bauth}', 'content-form')"><i
                                            class="fa fa-edit"></i> ${m.name}</a></li>
                                    </#if>
                                    </#list>
                                </ul>
                            </li>
                        </ul>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Meeting', ${MeetingDirectoryID?c}, 'meetings', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addMeeting}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>