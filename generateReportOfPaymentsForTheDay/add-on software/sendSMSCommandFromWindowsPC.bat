@echo off
REM Send Short Messaging Service (SMS) message from Windows Personal Computer (PC)
REM author: SYSON, MICHAEL B.
REM date created: 20200915
REM last updated: 20210113
REM
REM Notes:
REM 1) Download Android Software Development Kit (SDK) Platform Tools to execute Android Debug Bridge (ADB) Shell commands
REM https://developer.android.com/studio/releases/platform-tools;
REM last accessed: 20200914T1036
REM 2) Verify connected devices/emulators using ADB Command: adb shell
REM 3) Verify ADB commands using: adb
REM 4) Accept Rivest–Shamir–Adleman (RSA) pairing of mobile telephone device with PC
REM  --> This is after connecting the device with the PC via the Universal Serial Bus (USB) ports and cable
REM
REM References:
REM 1) https://stackoverflow.com/questions/17580199/sending-a-sms-on-android-through-adb;
REM last accessed: 20200914; question by: user790995, 20130710T2022
REM 2) https://stackoverflow.com/questions/7789826/adb-shell-input-events;
REM last accessed: 20200914; answer by: LionCoder, 20111213T0256; edited by Community, 20200317T0723

REM added by Mike, 20200919; edited by Mike, 20200924
Call generatePayslipForTheDayReportToSendViaSMS.bat

REM TO-DO: -update: this
REM echo $(date +%F)

REM TO-DO: -update: this
REM sSMSBodyValue=`cat "output/smsReport"$(date +%F)".txt"`
rem set sSMSBodyValue=`type "output/smsReport"*".txt"`

rem added by Mike, 20200924
rem edited by Mike, 20201002
rem set sDateToday=%date:~5%
rem echo %sDateToday%

rem set sMonthToday=0%date:~5,1%
rem echo %sMonthToday%

rem set sDayToday=%date:~7,2%
rem echo %sDayToday%

rem set sYearToday=%date:~10,4%
rem echo %sYearToday%

rem added by Mike, 20201002
set myDate=%date:~10,4%-%date:~4,2%-%date:~7,2%
rem set myDate="2020-10-01"
echo %myDate%

rem for %%i (type "D:\2020\add-on software\generatePayslipForTheDay\output\"smsReport2020-09-24.txt") do (set sSMSBodyValue=%%i)
rem set sSMSBodyValue=for(type "D:\2020\add-on software\generatePayslipForTheDay\output\"smsReport2020-09-24.txt")

rem for (type "D:\2020\add-on software\generatePayslipForTheDay\output\"smsReport2020-09-24.txt") do (set sSMSBodyValue=%%i)

rem print "D:\2020\add-on software\generatePayslipForTheDay\output\"smsReport2020-09-24.txt" | set sSMSBodyValue

rem set sSMSBodyValue="D:\2020\add-on software\generatePayslipForTheDay\output\"smsReport2020-09-24.txt"
rem for /f "tokens=2" %%V in ('tasklist.exe ^| findstr /i "%1" 2^>NUL') do @set "PID_LIST=!PID_LIST! /PID %%V"
rem for /f "delims=" %%V in ('type "D:\2020\add-on software\generatePayslipForTheDay\output\smsReport2020-09-24.txt"') do @set sSMSBodyValue=%%V
rem for /f "delims=" %%V in ('type "D:\2020\add-on software\generatePayslipForTheDay\output\smsReport"%sYearToday%-%sMonthToday%-%sDayToday%".txt"') do @set sSMSBodyValue=%%V

rem edited by Mike, 20201002
rem for /f "delims=" %%V in ('type "D:\2020\add-on software\generatePayslipForTheDay\output\smsReport"%sYearToday%-%sMonthToday%-%sDayToday%".txt"') do @set sSMSBodyValue=%%V

rem added by Mike, 20210113
rem output: 2021
set myDateYear=%date:~10,4%

rem edited by Mike, 20210113
rem for /f "delims=" %%V in ('type "D:\2020\add-on software\generatePayslipForTheDay\output\smsReport%myDate%.txt"') do @set sSMSBodyValue=%%V
for /f "delims=" %%V in ('type "D:\%myDateYear%\add-on software\generatePayslipForTheDay\output\smsReport%myDate%.txt"') do @set sSMSBodyValue=%%V

echo %sSMSBodyValue%

REM update file location
REM cd /home/unit_member/Documents/USBONG/Android/platform-tools
rem cd "D:\2020\add-on software\sendReportViaSMS\platform-tools_r30.0.4-windows\platform-tools"
rem edited by Mike, 20210113
rem cd "D:\2020\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"

rem edited by Mike, 20210113
rem cd "D:\2020\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"
rem cd "D:\2021\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"
rem cd "D:\2021\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"
cd "D:\%myDateYear%\add-on software\generatePayslipForTheDay\lib\platform-tools_r30.0.4-windows\platform-tools"

rem adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "Kumusta!" --ez exit_on_sent true
rem adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body "output/smsReport"*".txt" --ez exit_on_sent true
adb shell am start -a android.intent.action.SENDTO -d sms:639299527263 --es sms_body %sSMSBodyValue% --ez exit_on_sent true

adb shell input keyevent 22
rem adb shell input keyevent 66

rem pause
