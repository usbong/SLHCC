@echo off
echo //-----------------------------------------------------------------------
echo //  Marikina Orthopedic Specialy Clinic - Sta. Lucia Health Care Centre
echo //  Usbong Social Systems, Inc. (USBONG.PH)
echo //
echo //  Automatic File Decryption
echo //-----------------------------------------------------------------------
echo.
set mainDirectory=%CD%
set /p pass="Enter passphrase:"
echo %pass% > library/pass.bin
cd library
openssl enc -d -pbkdf2 -aes256 -in %mainDirectory%/input/EncryptedReportForTheDayTreatment.html -out %mainDirectory%/output/DecryptedReport.php -kfile pass.bin
del pass.bin
cd ..
explorer file:///%mainDirectory%/output/DecryptedReport.php