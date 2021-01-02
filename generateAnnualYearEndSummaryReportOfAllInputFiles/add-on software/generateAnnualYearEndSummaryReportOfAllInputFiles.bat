cd /d %1%
set mainDirectory=%CD%
cd /d %mainDirectory%

rem added by Mike, 20210102
rem note: if exist c:\folder\ del c:\folder\
rem without "if exist", Computer via Command Prompt shall notify us that no "input" folder exists to delete
rem "/Q" to remove "are you sure? Y/N"
del /Q input
mkdir input
mkdir "input\consultation"
mkdir "input\treatment"

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