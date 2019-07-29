@echo off
echo //-----------------------------------------------------------------------
echo //  Marikina Orthopedic Specialy Clinic - Sta. Lucia Health Care Centre
echo //  Usbong Social Systems, Inc. (USBONG.PH)
echo //
echo //  HTML File Automatic Encryption
echo //-----------------------------------------------------------------------
echo.
cd ..
set mainDirectory=%CD%
cd "add-on software"
set /p pass="Enter passphrase:"
echo %pass% > library/pass.bin
cd library
openssl enc -e -pbkdf2 -aes256 -in %mainDirectory%/"add-on software"/input/treatment/Report_Today_Input.html -out %mainDirectory%/"add-on software"/output/report.php -kfile pass.bin
del pass.bin
cd ..
copy output\report.php C:\xampp\htdocs\usbong_kms\application\views
"requirements\chrome.exe - Shortcut.lnk" "http://192.168.1.100:80/usbong_kms/"
PAUSE
