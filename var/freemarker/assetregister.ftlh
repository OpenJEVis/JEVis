<div>
    <a href="#" onclick="connect('./assetregister?site=${siteName}&year=${lastyear?c}', '${bauth}', 'content-form')"><i
            style="float: left" class="fa fa-angle-left fa-2x"></i></a>
    <h2 style="display: inline">
        <center>${assetRegister}${year?c}</center>
    </h2>
    <a href="#" onclick="connect('./assetregister?site=${siteName}&year=${nextyear?c}', '${bauth}', 'content-form')"><i
            style="display: inline; float: right" class="fa fa-angle-right fa-2x"></i></a>
    <table class="nice-table nice-table-text-align-right-3-8" align="center">
        <tr>
			<#setting number_format="#,##0.##">
			<#list tablecaptionsForHeaderForAssetRegister.columns as cola>
				<th>${cola.name}</th>
            </#list>
        </tr>
			<#list assetregister as asset>
			<tr>
				<#list tablecaptionsAssetRegister.columns as cola>
				<#if cola?index gte 3>
				<td>${asset[cola.name]?string("#,##0.00")}</td>
                <#else>
				<td>${asset[cola.name]}</td>
                </#if>
                </#list>
            </tr>
            </#list>
    </table>
</div>