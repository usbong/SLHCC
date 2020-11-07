#!/bin/bash

# Auto-verify Paid HMO List with Master List
# author: SYSON, MICHAEL B.
# date created: 20201107
# last updated: 20201107
#
# Note:
# 1) Example output file in output folder: PT TREATMENT 2020verifiedMacroEnabledMasterListV61LibreOfficeCalc_201912
# where: 201912 : YYYYMM
# note: add underscore, i.e. "_" before year and month
#
# 2) outputNotes.txt is the output file for the System print-outs, i.e. written note output 
#

#TO-DO: -add: hmo folder
#Note: At present, we can use as input only one (1) file with paid hmo transactions
#TO-DO: -update: this

java -cp ./software: autoVerifyPaidHMOListwithMasterList input/*.csv > outputNotes.txt
