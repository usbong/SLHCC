# GENERATE MONTHLY SUMMARY REPORT OF PAYMENTS (HMO and NON-HMO/CASH)
## ADD-ON SOFTWARE INSTALLER
### REQUIREMENTS:
1) Java Standard Edition (SE) 8: [Java Run-time Environment (JRE)](https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) or [Java Sofware Development Kit (JDK)](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)<br/>
2) Operating System (OS): Windows, Linux, Unix-like Operating Systems<br/>
--> Verified: Windows 7 Ultimate Service Pack 1, LUBUNTU 20.04 LTS

#### NOTES:
a) At present, the add-on software does not connect to the internet, and is only accessible in the local network.<br/>
--> Therefore, the 8th version should suffice.<br/><br/>
b) If you are using Windows OS 32-bit (x86) on your computer, please download the appropriate Java Standard Edition (SE) for that OS.<br/>
--> It can either be the Java Run-time Environment (JRE) or the Java Sofware Development Kit (JDK).<br/>
--> If you will need to modify the Java code, we recommend that you download the JDK.<br/>
--> Otherwise, the JRE should suffice.<br/><br/>
c) The pre-compiled Java class is executable on OS's, e.g. Windows and Linux.<br/>
--> Windows Machine (Batch Command Prompt): Please use "generateMonthlySummaryReportOfPayments.bat"<br/>
--> Linux Machine (Bash Shell): Please use "generateMonthlySummaryReportOfPayments.sh"<br/>
--> This is to be able to execute it on, for example, Linux.<br/>
d) If there is no blank row after the last transaction row in each of your input files, please add it.<br/>
e) The input files are Tab-delimited .txt files.<br/>
--> [Libre Office Calc: Save As Tab Delimited File Filter Settings](https://github.com/usbong/KMS/blob/master/Notes/libreOfficeCalcFileSaveAsTabDelimitedFileFilterSettings.jpg)
