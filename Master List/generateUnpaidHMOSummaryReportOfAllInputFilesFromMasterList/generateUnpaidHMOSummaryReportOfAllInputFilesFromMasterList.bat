@ECHO OFF
REM
REM Copyright 2019~2021 SYSON, MICHAEL B.
REM
REM Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
REM
REM http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
REM
REM Auto-generate Unpaid HMO Summary Report of all input files from Master List using Linux Personal Computer (PC)
REM
REM company: USBONG
REM author: SYSON, MICHAEL B.
REM date created: 20200217
REM last updated: 20211019
REM website address: http://www.usbong.ph
REM
REM Notes:
REM HMO = Health Maintenance Organization
REM
REM Example input file: PT TREATMENT 2020verifiedMacroEnabledMasterListV61LibreOfficeCalc_201912
REM where: 201912 : YYYYMM
REM note: add underscore, i.e. "_" before year and month

REM Linux Machine Command
REM java -cp ./software: generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList input/treatment/*.csv input/consultation/*.csv assets/templates/generateUnpaidHMOSummaryReportOutputTemplate.html

REM Windows Machine Command
REM input .csv file format; update to use .txt file format if necessary
REM java -cp .\software: generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList input/treatment/*.csv input/consultation/*.csv assets/templates/generateUnpaidHMOSummaryReportOutputTemplate.html
java -cp .\software; generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList input/treatment/*.txt input/consultation/*.txt assets/templates/generateUnpaidHMOSummaryReportOutputTemplate.html

REM added by Mike, 20211019
REM date format: DDD MM/DD/YYYY; example: Tue 10/19/2021
set myYear=%date:~10,4%
REM echo %myYear%

set myMonth=%date:~4,2%
REM echo %myMonth%

set myDay=%date:~7,2%
REM echo %myDay%

REM echo %date%

REM /Y = Yes to Overwrite File if it exists
copy /Y "output\UnpaidHMOSummaryReportOutput.html" "assets\reports\UnpaidHMOSummaryReportOutputYear%myYear%V%myYear%%myMonth%%myDay%.html"

PAUSE