@echo off
REM Copyright 2020~2021 USBONG SOCIAL SYSTEMS, INC. (USBONG)
REM 
REM Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
REM
REM http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
REM
REM Send Short Messaging Service (SMS) message from Windows Personal Computer (PC)
REM
REM @company: USBONG SOCIAL SYSTEMS, INC. (USBONG)
REM @author: SYSON, MICHAEL B.
REM @date created: 20200915
REM @last updated: 20210509
REM
REM Notes:
REM 1) Download Android Software Development Kit (SDK) Platform Tools to execute Android Debug Bridge (ADB) Shell commands
REM https://developer.android.com/studio/releases/platform-tools;
REM last accessed: 20200914T1036
REM 2) Verify connected devices/emulators using ADB Command: adb shell
REM 3) Verify ADB commands using: adb
REM 4) Accept Rivest–Shamir–Adleman (RSA) pairing of mobile telephone device with PC
REM  --> This is after connecting the device with the PC via the Universal Serial Bus (USB) ports and cable
rem 5) Set value of "myDate" container if not report for today, e.g. set myDate=2021-03-03
REM
REM References:
REM 1) https://stackoverflow.com/questions/17580199/sending-a-sms-on-android-through-adb;
REM last accessed: 20200914; question by: user790995, 20130710T2022
REM 2) https://stackoverflow.com/questions/7789826/adb-shell-input-events;
REM last accessed: 20200914; answer by: LionCoder, 20111213T0256; edited by Community, 20200317T0723

rem added by Mike, 20210303
set myDate=%date:~10,4%-%date:~4,2%-%date:~7,2%
rem note: no need to add quotation marks; put rem command in new line
rem set myDate=2021-03-01
echo %myDate%

rem added by Mike, 20210303
rem set sYearToday=%date:~10,4%
rem edited by Mike, 20210509
set sYearToday=%myDate:~0,4%
rem set sYearToday=%date:~10,4%

echo %sYearToday%

REM added by Mike, 20200919; edited by Mike, 20200924
Call generatePayslipForTheDayReportToSendViaSMS.bat %myDate%

rem added by Mike, 20210302
rem update folder location
rem edited by Mike, 20210509
rem cd "D:\Usbong\SLHCC\Reports\platform-tools"
cd "C:\Usbong\unit\Reports\lib\platform-tools_r30.0.4-windows\platform-tools"

rem edited by Mike, 20210303
rem update file location
rem for /f "delims=" %%V in ('type "D:\%sYearToday%\add-on software\generatePayslipForTheDay\output\smsReport%myDate%.txt"') do @set sSMSBodyValue=%%V

rem edited by Mike, 20210509
rem for /f "delims=" %%V in ('type "D:\Usbong\SLHCC\Reports\add-on software\output\smsReport%myDate%.txt"') do @set sSMSBodyValue=%%V
for /f "delims=" %%V in ('type "D:\%sYearToday%\add-on software\generatePayslipForTheDay\output\smsReport%myDate%.txt"') do @set sSMSBodyValue=%%V


echo %sSMSBodyValue%

rem NOTES:
rem -----
rem set sSMSBodyValue=SLHCC,2021-01-04,PT,Total:8,CashTotalFee:1071.43,CashTotalNetFee:712.50,HMOTotalFee:3835.72,HMOTotalNetFee:2175.38

REM update file location

rem removed by Mike, 20210302
rem cd "D:\%sYearToday%\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"

rem adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "Kumusta!" --ez exit_on_sent true
rem adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "output/smsReport"*".txt" --ez exit_on_sent true
rem -----

adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body %sSMSBodyValue% --ez exit_on_sent true

adb shell input keyevent 22
rem adb shell input keyevent 66

pause
