@echo off
echo //-----------------------------------------------------------------------
echo //  Marikina Orthopedic Specialy Clinic - Sta. Lucia Health Care Centre
echo //  Usbong Social Systems, Inc. (USBONG.PH)
echo //
echo //  HTML File Automatic Decryption
echo //-----------------------------------------------------------------------
echo.
set mainDirectory=%CD%
set /p pass="Enter passphrase:"
echo %pass% > library/pass.bin
cd library
openssl enc -d -pbkdf2 -aes256 -in %mainDirectory%/input/report.php -out %mainDirectory%/output/DecryptedReport.php -kfile pass.bin
del pass.bin
cd ..
explorer file:///%CD%/output/DecryptedReport.php