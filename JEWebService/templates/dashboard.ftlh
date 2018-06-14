<div class="body-column"
     style="width: 100%; overflow: auto; background: linear-gradient(to bottom right, #a5b1c2, #d1d8e0, #a5b1c2); background-image: url('./resources/image?name=paper.png'); background-repeat: repeat;">
	<button id="button-show-charts" onclick="showCharts(${energysource1?c}, '${energysource1name}',
            '${listenergyconsumptionlastname?c}'<#list listenergyconsumption1last as l1>, ${l1?c!'0'}</#list>
            ,'${listenergyconsumptionlastlastname?c}'<#list listenergyconsumption1lastlast as ll1>, ${ll1?c!'0'}</#list>
            ,'${listenergyconsumptionlastlastlastname?c}'<#list listenergyconsumption1lastlastlast as lll1>, ${lll1?c!'0'}</#list>
                                   <#list lastTotalEnPIs as last>, ${last?c!'0'}</#list>
                                   <#list lastlastTotalEnPIs as lastlast>, ${lastlast?c!'0'}</#list>
                                   <#list lastlastlastTotalEnPIs as lastlastlast>, ${lastlastlast?c!'0'}</#list>
            ,${energysource2?c}, '${energysource2name}'<#list listenergyconsumption2last as l2>, ${l2?c!'0'}</#list>
																		   <#list listenergyconsumption2lastlast as ll2>, ${ll2?c!'0'}</#list>
																		   <#list listenergyconsumption2lastlastlast as lll2>, ${lll2?c!'0'}</#list>
            ,${energysource3?c}, '${(energysource3name)!''}'<#if listenergyconsumption3last??><#list listenergyconsumption3last as l3>, ${l3?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
																		   <#if listenergyconsumption3lastlast??><#list listenergyconsumption3lastlast as ll3>, ${ll3?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
																		   <#if listenergyconsumption3lastlastlast??><#list listenergyconsumption3lastlastlast as lll3>, ${lll3?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
            ,${energysource4?c}, '${(energysource4name)!''}'<#if listenergyconsumption4last??><#list listenergyconsumption4last as l4>, ${l4?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
																		   <#if listenergyconsumption4lastlast??><#list listenergyconsumption4lastlast as ll4>, ${ll4?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
																		   <#if listenergyconsumption4lastlastlast??><#list listenergyconsumption4lastlastlast as lll4>, ${lll4?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
            ,${energysource5?c}, '${(energysource5name)!''}'<#if listenergyconsumption5last??><#list listenergyconsumption5last as l5>, ${l5?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
																		   <#if listenergyconsumption5lastlast??><#list listenergyconsumption5lastlast as ll5>, ${ll5?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
																		   <#if listenergyconsumption5lastlastlast??><#list listenergyconsumption5lastlastlast as lll5>, ${lll5?c!'0'}</#list><#else>,0,0,0,0,0,0,0,0,0,0,0,0</#if>
            , '${janStr}', '${febStr}', '${marStr}', '${aprStr}', '${mayStr}', '${junStr}', '${julStr}', '${augStr}', '${sepStr}', '${octStr}', '${novStr}', '${decStr}'
            , '${totalEnPIs}');">Show Charts
    </button>
<#if energysource1 == true>
	<div style="width:100%;">
        <canvas id="canvasTotalEnPIs" style="margin: auto;"></canvas>
    </div>	
    <br>
    <br>
	<div>
        <h2>
            <center>${totalEnPIs}</center>
        </h2>
        <table class="nice-table" align="center">
            <tr>
				<#list tablecaptionsEnPIs.columns as colenpi>
                    <th>${colenpi.name}</th>
                </#list>
            </tr>
			<#list tabledataEnPIs as dataenpi>
                <#setting number_format="#,##0.00">
			<tr>
                <td>${dataenpi.year?c}</td>
                <td>${dataenpi.totalEnPIYearRound}</td>
                <td>${dataenpi.totalEnPIJanuary}</td>
                <td>${dataenpi.totalEnPIFebruary}</td>
                <td>${dataenpi.totalEnPIMarch}</td>
                <td>${dataenpi.totalEnPIApril}</td>
                <td>${dataenpi.totalEnPIMay}</td>
                <td>${dataenpi.totalEnPIJune}</td>
                <td>${dataenpi.totalEnPIJuly}</td>
                <td>${dataenpi.totalEnPIAugust}</td>
                <td>${dataenpi.totalEnPISeptember}</td>
                <td>${dataenpi.totalEnPIOctober}</td>
                <td>${dataenpi.totalEnPINovember}</td>
                <td>${dataenpi.totalEnPIDecember}</td>
            </tr>
            </#list>
        </table>
    </div>
	<br>
	<br>	
	<div style="width:100%;">
        <canvas id="canvasConsumption1" style="margin: auto;"></canvas>
    </div>	
    <br>
    <br>
</#if>
<#if energysource2 == true>
	<div style="width:100%;">
        <canvas id="canvasConsumption2" style="margin: auto;"></canvas>
    </div>	
    <br>
    <br>
</#if>
<#if energysource3 == true>
	<div style="width:100%;">
        <canvas id="canvasConsumption3" style="margin: auto;"></canvas>
    </div>	
    <br>
    <br>
</#if>
<#if energysource4 == true>
	<div style="width:100%;">
        <canvas id="canvasConsumption4" style="margin: auto;"></canvas>
    </div>	
    <br>
    <br>
</#if>
<#if energysource5 == true>
	<div style="width:100%;">
        <canvas id="canvasConsumption5" style="margin: auto;"></canvas>
    </div>	
    <br>
    <br>
</#if>
<#if energysource1 == true>
	<div>
        <h2>
            <center>${consumption}</center>
        </h2>
        <table class="nice-table total-bold nice-table-text-align-right-3-8" align="center">
            <tr>
			<#setting number_format="#,##0.##">
			<#list tablecaptionsForHeader.columns as col1>
				<th>${col1.name}</th>
            </#list>
            </tr>
			<#list tabledata1 as data1>
			<tr>
				<#list tablecaptions1.columns as col1>
				<#if col1?index gt 1>
				<td>${data1[col1.name]?string("#,##0.00")}</td>
                <#elseif col1?index == 0>
				<td>${data1[col1.name]?c}</td>
                <#else>
				<td>${data1[col1.name]}</td>
                </#if>
                </#list>
            </tr>
            </#list>
        </table>
        <table class="nice-table total-bold nice-table-text-align-right-3-8" align="center">
            <tr>
			<#list tablecaptionsForHeader.columns as col2>
                <th>${col2.name}</th>
            </#list>
            </tr>
			<#list tabledata2 as data2>
			<tr>
				<#list tablecaptions2.columns as col2>
				<#if col2?index gt 1>
				<td>${data2[col2.name]?string("#,##0.00")}</td>
                <#elseif col2?index == 0>
				<td>${data2[col2.name]?c}</td>
                <#else>
				<td>${data2[col2.name]}</td>
                </#if>
                </#list>
            </tr>
            </#list>
        </table>
        <table class="nice-table total-bold nice-table-text-align-right-3-8" align="center">
            <tr>
			<#list tablecaptionsForHeader.columns as col3>
                <th>${col3.name}</th>
            </#list>
            </tr>
			<#list tabledata3 as data3>
			<tr>
				<#list tablecaptions3.columns as col3>
				<#if col3?index gt 1>
				<td>${data3[col3.name]?string("#,##0.00")}</td>
                <#elseif col3?index == 0>
				<td>${data3[col3.name]?c}</td>
                <#else>
				<td>${data3[col3.name]}</td>
                </#if>
                </#list>
            </tr>
            </#list>
        </table>
    </div>
	<br>
	<br>
	
	
	<div>
        <h2>
            <center>${assetRegisterFor}${listenergyconsumptionlastname?c}</center>
        </h2>
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
                    <td>${asset[cola.name]}</td>
                </#list>
            </tr>
            </#list>
        </table>
    </div>
	<br>
	<br>
	<div>
        <h2>
            <center></center>
        </h2>
        <table class="nice-table nice-table-text-align-right-2-3" align="center">
            <tr>
			<#setting number_format="#,##0.##">
			<#list tablecaptionsForHeaderForAssetSums.columns as cols>
				<th>${cols.name}</th>
            </#list>
            </tr>
			<#setting number_format="#,##0.##">
			<#list sumAssets as sum>
			<tr>
				<#setting number_format="#,##0.##">
				<#list tablecaptionsAssetSums.columns as cols>
				<td><#if sum[cols.name]?string != '0'>${sum[cols.name]}</#if></td>
                </#list>
            </tr>
            </#list>
        </table>
    </div>
	</div>
</#if>
		