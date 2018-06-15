<div>
    <a href="#" onclick="connect('./esoverview?site=${siteName}&year=${lastyear?c}', '${bauth}', 'content-form')"><i
            style="float: left" class="fa fa-angle-left fa-2x"></i></a>
    <h2>
        <center>${consumption} ${year?c}</center>
    </h2>
    <a href="#" onclick="connect('./esoverview?site=${siteName}&year=${nextyear?c}', '${bauth}', 'content-form')"><i
            style="display: inline; float: right" class="fa fa-angle-right fa-2x"></i></a>
    <table class="nice-table total-bold nice-table-text-align-right-3-8" align="center">
        <tr>
			<#setting number_format="#,##0.##">
			<#list tablecaptionsForHeader.columns as col1>
				<th>${col1.name}</th>
            </#list>
        </tr>
			<#list listConsumptionsNames as name>
			<tr>
                <td>${year?c}</td>
                <td>${listConsumptionsNames[name?index]}</td>
                <td>${listConsumptions[name?index]?string("#,##0")}</td>
                <td>${listShareOfTotal[name?index]?string("#,##0.00")}</td>
                <td>${listCosts[name?index]?string("#,##0.00")}</td>
                <td>${listCostRelated[name?index]?string("#,##0.00")}</td>
                <td>${listCO2Emissions[name?index]?string("#,##0")}</td>
                <td>${listCO2ShareOfTotal[name?index]?string("#,##0.00")}</td>
            </tr>
            </#list>
        <tr>
            <td>${year?c}</td>
            <td>${total}</td>
            <td>${totalConsumption?string("#,##0")}</td>
            <td></td>
            <td>${totalCost?string("#,##0.00")}</td>
            <td></td>
            <td>${totalCO2?string("#,##0")}</td>
            <td></td>
        </tr>
    </table>
    <br/>
    <br/>
</div>