@echo off
echo //-----------------------------------------------------------------------
echo //  Marikina Orthopedic Specialy Clinic - Sta. Lucia Health Care Centre
echo //  Usbong Social Systems, Inc. (USBONG.PH)
echo //
echo //  Automatic File Encryption
echo //-----------------------------------------------------------------------
echo.
set mainDirectory=%CD%
set /p pass="Enter passphrase:"
echo %pass% > library/pass.bin
cd library
openssl enc -e -pbkdf2 -aes256 -in %mainDirectory%/input/treatment/ReportForTheDay.txt -out %mainDirectory%/output/EncryptedReportForTheDayTreatment.html -kfile pass.bin
del pass.bin
cd ..
explorer file:///%CD%/output/EncryptedReportForTheDayTreatment.html