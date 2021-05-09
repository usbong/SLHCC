@echo off
REM Copyright 2020~2021 USBONG SOCIAL SYSTEMS, INC. (USBONG)
REM  
REM Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
REM http://www.apache.org/licenses/LICENSE-2.0
REM  
REM Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
REM
REM Generate Payslip for the Day Report to Send via Short Messaging Service (SMS) message from Windows Personal Computer (PC)
REM
REM @company: USBONG SOCIAL SYSTEMS, INC. (USBONG)
REM @author: SYSON, MICHAEL B.
REM @date created: 2020
REM @last updated: 20210303

REM mainDirectory=$(pwd)
set mainDirectory=%~dp0

REM echo $mainDirectory
REM cd "$mainDirectory"
cd /d %mainDirectory%

rem added by Mike, 20210303
rem no need to add quotation marks
echo input year: %1
set sMyDate=%1:~10,4%-%date:~4,2%-%date:~7,2%

REM NOTES: LINUX MACHINE
REM -----

REM cd "software"
REM ls -l

REM example output: 2020-09-17
REM echo $(date +%F)

REM example output: September 2020
REM echo $(date +%B) $(date +%Y)
REM sMonthYear=$(date +%B)" "$(date +%Y)

REM edited by Mike, 20200926
REM $sMonthYear=$(date +%B)" "$(date +%Y) REM Command using Linux Machine
REM set sMonthYear="September" rem "2020"
REM echo $sMonthYear

REM -----

rem edited by Mike, 20210303
rem set myYear=%date:~10,4%
set sMyYear=%sMyDate:~0,4%
echo %sMyYear%

rem reference: https://stackoverflow.com/questions/15469307/how-to-print-month-name-in-file-name-by-using-bat/48331435
rem answer by: Aacini, 20130318, edited 20170724
setlocal EnableDelayedExpansion
set m=100
for %%m in (January February March April May June July August September October November  December) do (
   set /A m+=1
   set month[!m:~-2!]=%%m
)
rem Change tokens=2 for DD/MM/YYYY date format
for /F "tokens=1 delims=/"  %%m in ("%sMyDate:~4,2%") do (
   set monthName=!month[%%m]!
)
echo %monthName%

set sMonthYear=%monthName%"*"%sMyYear%
echo %sMonthYear%

REM update input file location
rem edited by Mike, 20210509
set sInputFileLocation="C:\Usbong\unit\workbooks"
rem set sInputFileLocation="input/"

echo %sInputFileLocation%


REM Java command using Windows Machine
REM +added: last parameter for set date, format (YYYY-MM-DD): 2021-03-01
java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ "%sInputFileLocation%"\Treatment"*%sMonthYear%.txt" "%sInputFileLocation%"\Consultation"*%sMonthYear%.txt" %1

rem added by Mike, 20210303
rem %1
rem we use $1 due to %myDate% includes ~10,4... instructions 
