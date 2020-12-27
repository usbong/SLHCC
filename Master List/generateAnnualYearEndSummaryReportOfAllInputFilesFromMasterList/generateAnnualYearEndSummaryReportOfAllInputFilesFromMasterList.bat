rem edited by Mike, 20201227
rem java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFilesFromMasterList input/consultation/*.txt input/treatment/*.txt assets/*.txt

java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFilesFromMasterList input/consultation/*.csv input/treatment/*.csv assets/*.txt

PAUSE