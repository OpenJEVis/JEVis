<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${TrainingCoursesDirectoryName}</label>
                <ul>
					<#list trainingcourses as tc>
					<li><a href="#" onclick="connect('./object?ID=${tc.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${tc.name}</a></li>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Training Course', '${TrainingCoursesDirectoryID?c}', 'trainingcourses', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addTrainingCourse}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>