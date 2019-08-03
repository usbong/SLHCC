set mainDirectory=%CD%
cd assets/transactions/
copy *List.txt *ListTemp.txt
cd /d %mainDirectory%
java -cp .\software;.\software\org.apache.commons.text.jar generateMonthlySummaryReportWithDiagnosedCasesOfAllInputFiles input/treatment/*.txt input/consultation/*.txt assets/*.txt
cd assets/transactions/
del *List.txt
rename *ListTemp.txt *List.txt
cd %mainDirectory%
cd ..
"add-on software"\requirements\"chrome.exe - Shortcut.lnk" file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html file:///%CD%/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html file:///%CD%/add-on%%20software/output/MonthlyStatisticsConsultation.html file:///%CD%/add-on%%20software/output/MonthlyStatisticsProcedure.html file:///%CD%/add-on%%20software/output/MonthlyStatisticsTreatment.html
PAUSE