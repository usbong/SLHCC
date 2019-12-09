cd /d %1%
set mainDirectory=%CD%
cd /d %mainDirectory%
cd assets/transactions/
mkdir tempListBeforeProcessing
copy *List.txt "tempListBeforeProcessing/"

xcopy "tempListBeforeProcessing\*List.txt" "." /s /y

cd %mainDirectory%
		
java -cp .\software;.\software\org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles input/treatment/*.txt input/consultation/*.txt assets/*.txt

cd assets/transactions/
del *List.txt

rename *ListTemp.txt *List.txt

cd %mainDirectory%
cd ..
"add-on software"\requirements\"chrome.exe - Shortcut.lnk" file:///D:/2019/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html file:///D:/2019/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html file:///D:/2019/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html file:///D:/2019/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsConsultation.html file:///D:/2019/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsProcedure.html file:///D:/2019/add-on%%20software/generateMonthlySummaryReport/add-on%%20software/output/MonthlyStatisticsTreatment.html
PAUSE