@echo off
set mainDirectory=%~dp0
cd /d %mainDirectory%
cd output/
del *.txt
cd ..

rem added by Mike, 20200921
rem set myDate=%date:~10,4%%date:~4,2%%date:~7,2%
rem set myTime=%time:~0,2%%time:~3,2%

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

java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ input/""Treatment*%monthName%" "%myYear%.txt""

PAUSE
rem exit