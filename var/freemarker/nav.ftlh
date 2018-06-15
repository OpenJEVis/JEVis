<a href="#">${energymanagement} <i class="fa fa-caret-down"></i></a>

<div class="fulldrop" id="fulldrop-menu">
    <div class="column">
        <h3><u>Dashboard</u></h3>
        <ul>
            <li><a href="#" onclick="cls(); connect('./dashboard?site=${n.siteName}', '${bauth}', 'content-dash');">Dashboard</a>
            </li>
        </ul>
    </div>

    <div class="column">
        <h3><u>${n.documentsDirName}</u></h3>
        <ul>
            <li><a href="#"
                   onclick="cls(); connect('./audits?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.auditDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./actionplans?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.actionPlanDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./announcements?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.announcementDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./energyteammembers?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.energyTeamDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./legalregulations?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.legalRegulationDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./managementmanual?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.managementManualDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./managementreviews?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.managementReviewDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./proceduraldocuments?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.proceduralDocumentsDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./trainingcourses?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.trainingCourseDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./trainings?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.trainingsDirName}</a>
            </li>
        </ul>
    </div>


    <div class="column">
        <h3><u>${n.energyPlanningDirName}</u></h3>
        <ul>
            <li><a href="#"
                   onclick="cls(); connect('./energysources?site=${n.siteName}', '${bauth}', 'content-nav'); connect('./esoverview?site=${n.siteName}', '${bauth}', 'content-form'); clearDashboard();">${n.energySourcesDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./production?site=${n.siteName}', '${bauth}', 'content-nav'); connect('./productionoverview?site=${n.siteName}', '${bauth}', 'content-form'); clearDashboard();">${n.performanceDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./energyflowcharts?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.energyFlowChartsDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./equipmentregister?site=${n.siteName}', '${bauth}', 'content-nav'); connect('./assetregister?site=${n.siteName}', '${bauth}', 'content-form'); clearDashboard();">${n.equipmentRegisterDirName}</a>
            </li>
        </ul>
    </div>


    <div class="column">
        <h3><u>${n.meetingsDirName}</u></h3>
        <ul>
            <li><a href="#"
                   onclick="cls(); connect('./meetings?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.meetingsDirName}</a>
            </li>
        </ul>
    </div>


    <div class="column">
        <h3><u>${n.monitoringRegisterDirName}</u></h3>
        <ul>
            <li><a href="#"
                   onclick="cls(); connect('./measuringpoints?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.measuringPointDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./meters?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.meterDirName}</a>
            </li>
            <li><a href="#"
                   onclick="cls(); connect('./stations?site=${n.siteName}', '${bauth}', 'content-nav'); clearDashboard();">${n.stationDirName}</a>
            </li>
        </ul>
    </div>

</div>