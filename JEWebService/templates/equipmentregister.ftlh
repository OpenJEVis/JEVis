<div class="css-treeview">
    <ul>
        <li><a href="#" onclick="connect('./assetregister?site=${siteName}', '${bauth}', 'content-form')"><i
                class="fa fa-clipboard-list"></i> ${assetRegister} ${year?c}</a>
        </li>
    </ul>
</div>
<nav class="vertical-navbar resticons">
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-thermometer-three-quarters" style="width: .875em;"
                                          aria-hidden="true"></i><input type="checkbox" id="item-0"/><label
                    class="toggle" for="item-0"><i class="fa fa-angle-right"></i> ${AirConditioningDirName}</label>
                <ul>
					<#list listAirConditioningEquipment as ace>
					<li><a href="#" onclick="connect('./object?ID=${ace.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${ace.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Air Conditioning', ${eqipmentAirConditioningDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false; "><i
                            class="fa fa-plus-square"></i> ${addAirConditioning}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-compress" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-1"/><label class="toggle" for="item-1"><i
                    class="fa fa-angle-right"></i> ${CompressorDirName}</label>
                <ul>
					<#list listCompressorEquipment as compressoreq>
					<li><a href="#" onclick="connect('./object?ID=${compressoreq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${compressoreq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Compressor', ${eqipmentCompressorDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addCompressor}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-snowflake" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-2"/><label class="toggle" for="item-2"><i
                    class="fa fa-angle-right"></i> ${CoolerDirName}</label>
                <ul>
					<#list listCoolerEquipment as coolereq>
					<li><a href="#" onclick="connect('./object?ID=${coolereq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${coolereq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Cooler', ${eqipmentCoolerDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addCooler}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-fire" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-3"/><label class="toggle" for="item-3"><i
                    class="fa fa-angle-right"></i> ${HeaterDirName}</label>
                <ul>
					<#list listHeaterEquipment as heatereq>
					<li><a href="#" onclick="connect('./object?ID=${heatereq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${heatereq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Heater', ${eqipmentHeaterDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addHeater}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-lightbulb" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-4"/><label class="toggle" for="item-4"><i
                    class="fa fa-angle-right"></i> ${LightingDirName}</label>
                <ul>
					<#list listLightingEquipment as lightingeq>
					<li><a href="#" onclick="connect('./object?ID=${lightingeq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${lightingeq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Lighting', ${eqipmentLightingDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addLighting}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-print" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-5"/><label class="toggle" for="item-5"><i
                    class="fa fa-angle-right"></i> ${OfficeDirName}</label>
                <ul>
					<#list listOfficeEquipment as officeeq>
					<li><a href="#" onclick="connect('./object?ID=${officeeq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${officeeq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Office Equipment', ${eqipmentOfficeDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addOfficeEquipment}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-utensils" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-6"/><label class="toggle" for="item-6"><i
                    class="fa fa-angle-right"></i> ${PantryDirName}</label>
                <ul>
					<#list listPantryEquipment as pantryeq>
					<li><a href="#" onclick="connect('./object?ID=${pantryeq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${pantryeq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Pantry Equipment', ${eqipmentPantryDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addPantryEquipment}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-cogs" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-7"/><label class="toggle" for="item-7"><i
                    class="fa fa-angle-right"></i> ${ProductionDirName}</label>
                <ul>
					<#list listProductionEquipment as productioneq>
					<li><a href="#" onclick="connect('./object?ID=${productioneq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${productioneq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Production Equipment', ${eqipmentProductionDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addProductionEquipment}</a></li>
                </ul>
            </li>
        </ul>
    </div>
    <div class="css-treeview">
        <ul>
            <li style="padding:  4px;"><i class="fa fa-cloud" style="width: .875em;" aria-hidden="true"></i><input
                    type="checkbox" id="item-8"/><label class="toggle" for="item-8"><i
                    class="fa fa-angle-right"></i> ${VentilationDirName}</label>
                <ul>
					<#list listVentilationEquipment as ventilationeq>
					<li><a href="#"
                           onclick="connect('./object?ID=${ventilationeq.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${ventilationeq.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Ventilation', ${eqipmentVentilationDirID?c}, 'equipmentregister', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addVentilation}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>