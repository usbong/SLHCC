@echo OFF
REM ' Copyright 2019~2022 SYSON, MICHAEL B.
REM '
REM ' Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
REM '
REM ' http://www.apache.org/licenses/LICENSE-2.0
REM '
REM ' Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
REM '
REM ' @company: USBONG
REM ' @author: SYSON, MICHAEL B.
REM ' @date created: 2019
REM ' @date updated: 20220731; from 20220414
REM ' @website address: http://www.usbong.ph

cd /d %1%
set mainDirectory=%CD%
cd /d %mainDirectory%

REM 'edited by Mike, 20220731
REM cd assets/transactions/
cd "add-on software/assets/transactions/"

mkdir tempListBeforeProcessing
copy *List.txt "tempListBeforeProcessing/"

xcopy "tempListBeforeProcessing\*List.txt" "." /s /y

cd %mainDirectory%
	
REM edited by Mike, 20220414	
REM java -cp .\software;.\software\org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles input/treatment/*.txt input/consultation/*.txt assets/*.txt

REM 'added by Mike, 20220731
cd "add-on software/"

java -cp .\software;.\software\org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles input/treatment/*.txt input/consultation/*.txt assets/KnownDiagnosedCasesList.txt

REM 'edited by Mike, 20220731
cd assets/transactions/
REM cd "add-on software/assets/transactions/"


del *List.txt

rename *ListTemp.txt *List.txt

cd %mainDirectory%
cd ..
rem edited by Mike, 20210131
rem "add-on software"\requirements\"chrome.exe - Shortcut.lnk" file:///D:/2020/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html file:///D:/2020/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html file:///D:/2020/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html file:///D:/2020/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsConsultation.html file:///D:/2020/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsProcedure.html file:///D:/2020/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsTreatment.html
set myDateYear=%date:~10,4%

REM 'edited by Mike, 20220731
REM "add-on software"\requirements\"chrome.exe - Shortcut.lnk" file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsConsultation.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsProcedure.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsTreatment.html

"generateMonthlySummaryReport\add-on software"\requirements\"chrome.exe - Shortcut.lnk" file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsConsultation.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsProcedure.html file:///D:/%myDateYear%/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsTreatment.html


PAUSE