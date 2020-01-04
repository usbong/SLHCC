cd /d %1%
set mainDirectory=%CD%
cd /d %mainDirectory%
java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFilesFromMasterList input/consultation/*%2*.txt input/treatment/*%2*.txt assets/*.txt
PAUSE