#!/bin/bash

# Auto-generate Unpaid HMO Summary Report of all input files from Master List using Linux Personal Computer (PC)
# author: SYSON, MICHAEL B.
# date created: 20201106
# last updated: 20201106
#
# Notes:
# HMO = Health Maintenance Organization
#
# Example input file: PT TREATMENT 2020verifiedMacroEnabledMasterListV61LibreOfficeCalc_201912
# where: 201912 : YYYYMM
# note: add underscore, i.e. "_" before year and month

java -cp ./software: generateUnpaidHMOSummaryReportOfAllInputFilesFromMasterList input/treatment/*.txt 

