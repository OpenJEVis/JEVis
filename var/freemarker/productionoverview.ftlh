<div>
    <a href="#"
       onclick="connect('./productionoverview?site=${siteName}&year=${lastyear?c}', '${bauth}', 'content-form')"><i
            style="float: left" class="fa fa-angle-left fa-2x"></i></a>
    <h2>
        <center>${production} ${year?c}</center>
    </h2>
    <a href="#"
       onclick="connect('./productionoverview?site=${siteName}&year=${nextyear?c}', '${bauth}', 'content-form')"><i
            style="display: inline; float: right" class="fa fa-angle-right fa-2x"></i></a>
    <table class="nice-table total-bold nice-table-text-align-right-3-8" align="center">
        <tr>
			<#setting number_format="#,##0.##">
			<#list tablecaptionsForHeader.columns as col1>
				<th>${col1.name}</th>
            </#list>
        </tr>
			<#list listProduction as p>
			<tr>
                <td>${year?c}</td>
                <td>${p.name}</td>
                <td>${productionValues[p?index]?string("#,##0")}</td>
                <td>${shareOfTotalProduction[p?index]?string("#,##0.00")}</td>
            </tr>
            </#list>
        <tr>
            <td>${year?c}</td>
            <td>${total}</td>
            <td>${totalProduction?string("#,##0")}</td>
            <td></td>
        </tr>
    </table>
    <br/>
    <br/>
</div>