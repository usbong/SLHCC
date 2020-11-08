@echo off
REM Auto-verify Paid HMO List with Master List (Windows Machine)
REM @company: USBONG SOCIAL SYSTEMS, INC. (USBONG)
REM @author: SYSON, MICHAEL B.
REM @date created: 20201107
REM @last updated: 20201108
REM
REM Notes:
REM 1) Example output file in output folder: PT TREATMENT 2020verifiedMacroEnabledMasterListV61LibreOfficeCalc_201912
REM where: 201912 : YYYYMM
REM note: add underscore, i.e. "_" before year and month
REM
REM 2) outputNotes.txt is the output file for the System print-outs, i.e. written note output 
REM

REM TO-DO: -add: hmo folder
REM Note: At present, we can use as input only one (1) file with paid hmo transactions
REM TO-DO: -update: this

java -cp ./software; autoVerifyPaidHMOListwithMasterList input/*.csv > outputNotes.txt

REM PAUSE