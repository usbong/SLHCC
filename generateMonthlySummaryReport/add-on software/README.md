# GENERATE MONTHLY SUMMARY REPORT
## ADD-ON SOFTWARE INSTALLER
### REQUIREMENTS:
1) Java Standard Edition (SE) 8: [Java Run-time Environment (JRE)](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or [Java Sofware Development Kit (JDK)](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)<br />
2) Windows Operating System (OS) 
3) Google Chrome (Browser)
#### NOTES:
a) At present, the add-on software does not connect to the internet, and is only accessible in the local network.<br />
--> Therefore, the 8th version should suffice.<br /><br />
b) If you are using Windows OS 32-bit (x86) on your computer, you must download the appropriate Java Standard Edition (SE) for that OS.<br />
--> It can either be the Java Run-time Environment (JRE) or the Java Sofware Development Kit (JDK).<br />
--> If you will need to modify the Java code, we recommend that you download the JDK.<br />
--> Otherwise, the JRE should suffice.<br /><br />
c) The pre-compiled Java class should be executable on other OS's, e.g. Linux.<br />
--> However, you may have to rewrite the batch file, "generateMonthlySummaryReport.bat", using BShell (Bash Shell).<br />
--> This is to be able to execute it on, for example, Linux.<br /> 

### COMMANDS NOTES:
a) Given: Current Directory = inside <b>"generateMonthlySummaryReport"</b> folder<br />
--> Inside is where <b>"generateMonthlySummaryReportWithMacro.xlsm"</b> MS EXCEL workbook file is located.<br />
<br />
b) In CMD (Command) Prompt:<br />
--> The following are ways to execute the Google Chrome (Browser) with the output HTML files as input...<br /><br />
i) <b>"add-on software"\requirements\\"chrome.exe - Shortcut.lnk" file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html file:///%CD%/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html</b>
<br /><br />
ii) <b>C:\...\Local\Google\Chrome\Application\chrome.exe "file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputTreatment.html"</b><br />
<b>C:\Users\User\AppData\Local\Google\Chrome\Application\chrome.exe "file:///%CD%/add-on%%20software/output/MonthlySummaryReportOfUnclassifiedDiagnosedCasesOutput.html"</b><br />
<b>C:\Users\User\AppData\Local\Google\Chrome\Application\chrome.exe "file:///%CD%/add-on%%20software/output/MonthlySummaryReportOutputConsultation.html"</b><br />
<br />
--> Update the location of <b>"chrome.exe"</b> to where it is in the computer.<br />
--> The directory where <b>"chrome.exe"</b> is located must not have space in its folder names; otherwise, you must add quotation marks, e.g. "add-on software".<br />
--> <b>"%CD%"</b>, i.e. the current directory, must not have space in its folder names.<br />
<br />
c) MS EXCEL VBA (Visual Basic for Applications)<br />
--> The inputWorkbookPath = path where the <b>"generateMonthlySummaryReportWithMacro.xlsm"</b> MS EXCEL workbook file is located.<br />
<br />
<b>ChDir inputWorkbookPath & "\add-on software\output"<br />
Shell "C:\...\Local\Google\Chrome\Application\chrome.exe" & " " & inputWorkbookPath & "\add-on%20software\output\MonthlySummaryReportOutputTreatment.html", vbNormalFocus<br />
</b><br />
--> Update the location of <b>"chrome.exe"</b> to where it is in the computer.<br />
--> The directory where <b>"chrome.exe"</b> is located must not have space in its folder names; otherwise, you must add quotation marks, e.g. "add-on software".<br />
