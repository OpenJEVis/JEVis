<html>
<head>
    <meta charset="utf-8"/>
    <link rel="stylesheet" href="../resources/styles">
    <link rel="stylesheet" href="../resources/navbar.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/3.2.1/css/font-awesome.limitData.css"/>
    <title>Action Plan</title>
    <style>
        canvas {
            -moz-user-select: none;
            -webkit-user-select: none;
            -ms-user-select: none;
        }

        #nice-table {
            font-family: "Trebuchet MS", Arial, Helvetica, sans-serif;
            border-collapse: collapse;
            width: 100%;
        }

        #nice-table td, #nice-table th {
            border: 1px solid #ddd;
            padding: 8px;
        }

        #nice-table tr:nth-child(even) {
            background-color: #f2f2f2;
        }

        .total-bold tr:nth-last-child(1) {
            font-weight: bold;
        }

        #nice-table tr:hover {
            background-color: #ddd;
        }

        #nice-table td:nth-child(1), td:nth-child(2) {
            text-align: center;
        }

        #nice-table td:nth-child(3), td:nth-child(4), td:nth-child(5), td:nth-child(6), td:nth-child(7), td:nth-child(8) {
            text-align: right;
        }

        #nice-table th {
            padding-top: 12px;
            padding-bottom: 12px;
            text-align: center;
            background-color: #3867d6;
            color: white;
        }

        @media print {
            @page {
                size: 21cm 29.7cm;
                margin: 27mm 25mm 20mm 20mm; /* change the margins as you want them to be. */
            }

            * {
                overflow: visible !important;
            }
        }


    </style>
</head>

<body style="overflow: auto; background: linear-gradient(to bottom right, #a5b1c2, #d1d8e0, #a5b1c2); background-image: url('../resources/image?name=paper.png'); background-repeat: repeat;">
<div class="">
    <div>
        <h1>${managementReview.name}</h1>
        <h2>${managementReview.reviewdate}</h2>
    </div>
    <br/>
    <br/>
    <div>
        <h2>Participants</h2>
        <table id="nice-table">
            <tr>
                <th>No.</th>
                <th>Name</th>
            </tr>
				<#list managementReview.participants?split("\n") as participant>
				<tr>
                    <td>${participant?index+1}</td>
                    <td>${participant}</td>
                </tr>
                </#list>
        </table>
    </div>
    <br/>
    <br/>
    <div>
        <h2>Content</h2>
			<#list managementReview.content?split("\n") as c>
			<p>${c}</p>
            </#list>
    </div>
    <br/>
    <br/>
    <div>
        <h2>${actionPlan.implementedactionsdirname}</h2>
        <table id="nice-table">
            <tr>
                <th>Name</th>
                <th>Investment Costs</th>
                <th>Payback Time</th>
                <th>Savings Potential<br/>Capital</th>
                <th>Savings Potential<br/>CO2</th>
                <th>Savings Potential<br/>Energy</th>
            </tr>
				<#list actionPlan.implementedactions as ia>
				<tr>
                    <td>${ia.name}</td>
                    <td>${ia.investmentcosts}</td>
                    <td>${ia.paybacktime}</td>
                    <td>${ia.savingspotentialcapital}</td>
                    <td>${ia.savingspotentialco2}</td>
                    <td>${ia.savingspotentialenergy}</td>
                </tr>
                </#list>
        </table>
    </div>
    <br/>
    <br/>
    <div>
        <h2>${actionPlan.plannedactionsdirname}</h2>
        <table id="nice-table">
            <tr>
                <th>Name</th>
                <th>Investment Costs</th>
                <th>Payback Time<br/>a</th>
                <th>Savings Potential<br/>Capital</th>
                <th>Savings Potential<br/>CO2</th>
                <th>Savings Potential<br/>Energy</th>
            </tr>
				<#list actionPlan.plannedactions as pa>
				<tr>
                    <td>${pa.name}</td>
                    <td>${pa.investmentcosts}</td>
                    <td>${pa.paybacktime}</td>
                    <td>${pa.savingspotentialcapital}</td>
                    <td>${pa.savingspotentialco2}</td>
                    <td>${pa.savingspotentialenergy}</td>
                </tr>
                </#list>
        </table>
    </div>
    <br/>
    <br/>
    <table>
        <tr>
            <td style="width: 250px; border: 1px solid #000; border-top-style: none; border-left-style: none; border-right-style: none;">${organisationLocation}
                ,
            </td>
            <td></td>
        </tr>
    </table>
    <br>
    <br>
</div>
</body>
</html>