<div class="tab">
    <button class="tablinks" onclick="openFormTab(event, 'Attributes')" id="defaultTab">${tabAttributes}</button>
    <button class="tablinks" onclick="openFormTab(event, 'Permissions')" id="tabPermissions">${tabPermissions}</button>
</div>
<div id="Attributes" class="tabcontent">
    <form id="form-${FormID?c}" class="data-form">
        <div align="left" style="width: auto; table-layout: fixed;">
            <div class="row">
                <div class="col-2">
                    <p>${objectname}</p>
                </div>
                <div class="col-3">
                    <div class="wrapper"><input class="input" type="text" value="${name}"
                                                onchange="sendNameToJEWebService('${bauth}', ${FormID?c}, this.value);"
                                                id="id-name">
                        <span class="underline">
                        </span>
                    </div>
                </div>
                <div class="col-3"></div>
            </div>
<#list attributes as att>
    <#if att.type == "Text" || att.type == "TextEnum">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2"><p>${att.transname}</p></div>
        <#else>
		<div class="col-2"><p>${att.name}</p></div>
        </#if>
        <div class="col-3">
            <div class="wrapper"><input class="input" type="text" value="${att.value}"
                                        onchange="sendValueToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value)"
                                        id="id-${att.name?replace(' ', '')}">
                <span class="underline"></span>
            </div>
        </div>
    </div>
    <#elseif att.type == "TextArea">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2"><p>${att.transname}</p></div>
        <#else>
		<div class="col-2"><p>${att.name}</p></div>
        </#if>
        <div class="col-9">
            <textarea style="min-width: 250px;" id="id-${att.name?replace(' ', '')}"
                      onchange="sendValueToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value)"
                      rows="10" class="boxsizingBorder">${att.value}</textarea>
        </div>
    </div>
    <#elseif att.type == "Date">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2"><p>${att.transname}</p></div>
        <#else>
		<div class="col-2"><p>${att.name}</p></div>
        </#if>
        <div class="col-3">
            <div class="wrapper">
                <input class="input" type="date" id="id-${att.name?replace(' ', '')}"
                       onchange="sendDateToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value)"
                       value="${att.value}">
                <span class="underline"></span>
            </div>
        </div>
    </div>
    <#elseif att.type == "Long">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2"><p>${att.transname}</p></div>
        <#else>
		<div class="col-2"><p>${att.name}</p></div>
        </#if>
        <div class="col-3">
            <div class="wrapper">
                <input class="input" type="number"
                       value=${att.longValue?c} onchange="sendValueToJEWebService('${bauth}',
                       ${FormID?c}, '${att.name}', this.value)" id="id-${att.name?replace(' ', '')}">
                <span class="underline"></span>
            </div>
        </div>
    </div>
    <#elseif att.type == "Double">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2"><p>${att.transname}</p></div>
        <#else>
		<div class="col-2"><p>${att.name}</p></div>
        </#if>
        <div class="col-3">
            <div class="wrapper">
                <input class="input" type="number" step=".01"
                       value=${att.doubleValue?c} onchange="sendValueToJEWebService('${bauth}',
                       ${FormID?c}, '${att.name}', this.value)" id="id-${att.name?replace(' ', '')}">
                <span class="underline"></span>
            </div>
        </div>
        <div class="col-3">
            <select class="data-form-select" id="id-${att.name?replace(' ', '')}-unit"
                    onchange="sendUnitToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value )">
                <option value="${att.unit}">${att.unit}</option>
                <option value="">(none)</option>
					<#list att.unithelp as uh>
					<option value="${uh}">${uh}</option>
                    </#list>
            </select>
        </div>
    </div>
    <#elseif att.type == "File">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2">
            <p>${att.transname}</p>
        </div>
        <#else>
		<div class="col-2">
            <p>${att.name}</p>
        </div>
        </#if>
		<#if att.file != "">
            <div class="col-4">
                <p style="word-break: break-all;">${att.file}, ${uploaded}: ${att.value}</p>
            </div>
							<div class="col-2">
                                <button class="data-form-button"
                                        onclick="download('${att.name}', ${FormID?c}, '${bauth}', '${att.file}')"
                                        type="button">${buttonDownload}
                                </button>
                            </div>
							<div class="col-3">
                                <div class="fileContainer">
                                    <span>${buttonUpload}</span>
                                    <input type="file" class="data-form-button"
                                           onchange="sendFileToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value, this)"
                                           id="id-${att.name?replace(' ', '')}">
                                </div>
                            </div>
        <#else>
            <div class="col-3">
                <div class="fileContainer">
                    <span>${buttonUpload}</span>
                    <input type="file"
                           onchange="sendFileToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value, this)"
                           id="id-${att.name?replace(' ', '')}">
                </div>
            </div>
        </#if>
    </div>
    <#elseif att.type == "Boolean">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2">
            <p>${att.transname}</p>
        </div>
        <#else>
		<div class="col-2">
            <p>${att.name}</p>
        </div>
        </#if>
        <div class="col-3">
            <p><input type="checkbox" value="${att.booleanValue?c}"
                      onchange="sendValueToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.checked)"
                      id="id-${att.name?replace(' ', '')}"
                                     <#if att.booleanValue?c == "true">checked</#if>></p>
        </div>
    </div>
    <#elseif att.type == "Selection">
	<div class="row"></div>

    <#elseif att.type == "Password">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2">
            <p>${att.transname}</p>
        </div>
        <#else>
		<div class="col-2">
            <p>${att.name}</p>
        </div>
        </#if>
        <div class="col-3">
            <p><input type="password" value="${att.value}"
                      onchange="sendValueToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value)"
                      id="id-${att.name?replace(' ', '')}"></p>
        </div>
    </div>
    <#elseif att.type == "ObjectTarget">
	<div class="row">
		<#if translated?c == "true">
		<div class="col-2">
            <p>${att.transname}</p>
        </div>
        <#else>
		<div class="col-2">
            <p>${att.name}</p>
        </div>
        </#if>
        <div class="col-3">
            <p>
                <select class="select-link" id="id-${att.name?replace(' ', '')}"
                        onchange="sendValueToJEWebService('${bauth}', ${FormID?c}, '${att.name}', this.value)">
				<#if att.longValue == 0>
				<option value="">${att.longValue?c} : ${att.targetname}</option>
                <#else>
				<option value="${att.longValue?c}">${att.longValue?c} : ${att.targetname}</option>
                </#if>
                    <option value="">(none)</option>
				<#list att.othelp as oth>
				<option value="${oth.id?c}">${oth.id?c} : ${oth.name} : ${oth.classname}</option>
                </#list>
                </select>
            </p>
        </div>
		<#if att.name == "Measuring Point">
		<div class="col-1">From:</div>
            <div class="col-2">
                <input type="date" id="id-from" value="2010-01-01"
                       onchange="updateChart('${name}', this.value, document.getElementById('id-until').value, document.getElementById('id-MeasuringPoint').value, '${bauth}');">
            </div>
		<div class="col-1">Until:</div>
            <div class="col-2">
                <input type="date" id="id-until" value=""
                       onchange="updateChart('${name}', document.getElementById('id-from').value, this.value, document.getElementById('id-MeasuringPoint').value, '${bauth}');">
            </div>
        </#if>
    </div>
    <#else>
    </#if>
</#list>
            </table>
            <br/><br/>
            <input type="button" class="data-form-button data-form-remove-button" value="${buttonDelete}"
                   onclick="removeObject(${FormID?c}, '${bauth}')"/>
            <div style="width:75%;">
                <canvas id="equipment-canvas"></canvas>
            </div>
            <br><br>
        </div>
    </form>
</div>
<div id="Permissions" class="tabcontent">
    <div class="row">
        <div class="col-2">
            <p>${objectname}</p>
        </div>
        <div class="col-3">
            <p>${name}</p>
        </div>
    </div>
    <#list permissions as p>
        <div class="row">
            <div class="col-2">
                <p>${p.name}</p>
            </div>
            <div class="col-3">
                <input type="button" class="data-form-button" value="${delete}"
                       onclick="deleteRelationship(${p.from?c}, ${p.to?c}, ${p.type}, '${bauth}'); return false;"/>
            </div>
        </div>
    </#list>
    <div class="row">
        <div class="col-2">
            <p>${addpermission}</p>
        </div>
        <div class="col-3">
            <select class="select-link"
                    onchange="createRelationship(${FormID?c}, this.value, 100, '${bauth}'); return false;">
                <option value="">(none)</option>
                <#list userGroups as ug>
                <option value="${ug.id?c}">${ug.name}</option>
                </#list>
            </select>
        </div>
    </div>
</div>