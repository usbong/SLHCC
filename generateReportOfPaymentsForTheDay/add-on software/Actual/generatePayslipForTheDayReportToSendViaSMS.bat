@echo off
REM mainDirectory=$(pwd)
set mainDirectory=%~dp0

REM echo $mainDirectory
REM cd "$mainDirectory"
cd /d %mainDirectory%

REM cd "software"

REM ls -l

REM java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongReportMain http://localhost/ input/*.txt

REM Java command using Windows Machine
REM java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ halimbawa

REM example output: 2020-09-17
REM echo $(date +%F)

REM example output: September 2020
REM echo $(date +%B) $(date +%Y)
REM sMonthYear=$(date +%B)" "$(date +%Y)

REM edited by Mike, 20200926
REM $sMonthYear=$(date +%B)" "$(date +%Y) REM Command using Linux Machine
REM set sMonthYear="September" rem "2020"
REM echo $sMonthYear

set myYear=%date:~10,4%

echo %myYear%

rem reference: https://stackoverflow.com/questions/15469307/how-to-print-month-name-in-file-name-by-using-bat/48331435
rem answer by: Aacini, 20130318, edited 20170724
setlocal EnableDelayedExpansion
set m=100
for %%m in (January February March April May June July August September October November  December) do (
   set /A m+=1
   set month[!m:~-2!]=%%m
)
rem Change tokens=2 for DD/MM/YYYY date format
rem edited by Mike, 20200921
rem for /F "tokens=1 delims=/"  %%m in ("%date:%") do (
for /F "tokens=1 delims=/"  %%m in ("%date:~4,2%") do (
   set monthName=!month[%%m]!
)
echo %monthName%

set sMonthYear=%monthName%"*"%myYear%
echo %sMonthYear%

REM Java command using Linux Machine
REM java -cp ./software:./software/org.json.jar:./software/org.apache.httpclient.jar:./software/org.apache.httpcore.jar:./software/org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ ./input/*"$sMonthYear".txt

REM Java command using Windows Machine
REM update input file location
REM C:\Usbong\unit\workbooks
set sInputFileLocation="C:\Usbong\unit\workbooks"

echo %sInputFileLocation%

rem java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ "%sInputFileLocation%\"Treatment"*%sMonthYear%"2020.txt"
REM edited by Mike, 20200926
REM java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ "%sInputFileLocation%"\Treatment"*"September*2020.txt"
java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ "%sInputFileLocation%"\Treatment"*%sMonthYear%.txt"

rem removed by Mike, 20200924
rem cd ..

REM %2
rem PAUSE
REM exit

rem removed by Mike, 20200924
rem added by Mike, 20200919
rem update: this
rem cd "D:\2020\add-on software\sendReportViaSMS\platform-tools_r30.0.4-windows\platform-tools"
rem cd "D:\2020\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"

rem adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "Kumusta!" --ez exit_on_sent true
rem adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "output/smsReport"*".txt" --ez exit_on_sent true

rem adb shell input keyevent 22
rem adb shell input keyevent 66
