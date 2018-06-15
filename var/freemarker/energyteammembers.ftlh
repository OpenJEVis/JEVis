<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${EnergyTeamMemberDirectoryName}</label>
                <ul>
                    <li><a href="#"
                           onclick="connect('./object?ID=${energymanager.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${energymanager.name}</a></li>
					<#list energyteammembers as etm>
					<li><a href="#" onclick="connect('./object?ID=${etm.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${etm.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Energy Team Member', '${EnergyTeamMemberDirectoryID?c}', 'energyteammembers', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addEnergyTeamMember}</a></li>

                </ul>
            </li>
        </ul>
    </div>
</nav>