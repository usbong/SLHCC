REM
REM Copyright 2021 SYSON, MICHAEL B.
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM 
REM     http://www.apache.org/licenses/LICENSE-2.0
REM     
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM
REM @company: USBONG
REM @author: SYSON, MICHAEL B.
REM @date created: 2021
REM @last updated: 20211229
REM @website address: http://www.usbong.ph
REM
 
cd /d %1%
set mainDirectory=%CD%
cd /d %mainDirectory%

cd..

rem edited by Mike, 20211228
rem copy "C:\Usbong\unit\workbooks\Consultation*.txt" "add-on software\input\consultation"
rem copy "C:\Usbong\unit\workbooks\Treatment*.txt" "add-on software\input\treatment"
copy "C:\Usbong\unit\workbooks\Consultation*.txt" "input\consultation"
copy "C:\Usbong\unit\workbooks\Treatment*.txt" "input\treatment"

cd "add-on software"

REM edited by Mike, 20211226
REM java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles input/consultation/*%2*.txt input/treatment/*%2*.txt assets/*.txt

java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles input/consultation/*2021.txt input/treatment/*2021.txt assets/*.txt

cd /d %mainDirectory%

cd..

start "xl" excel.exe /e "generateAnnualReportWithMacro.xlsm" /p "myInputParam"

rem removed by Mike, 20210101
rem PAUSE