cd /d %1%
set mainDirectory=%CD%
cd /d %mainDirectory%

cd..

copy "C:\Usbong\unit\workbooks\Consultation*.txt" "add-on software\input\consultation"

copy "C:\Usbong\unit\workbooks\Treatment*.txt" "add-on software\input\treatment"

cd "add-on software"

java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles input/consultation/*%2*.txt input/treatment/*%2*.txt assets/*.txt

cd /d %mainDirectory%

cd..

start "xl" excel.exe /e "generateAnnualReportWithMacro.xlsm" /p "myInputParam"

rem removed by Mike, 20210101
rem PAUSE