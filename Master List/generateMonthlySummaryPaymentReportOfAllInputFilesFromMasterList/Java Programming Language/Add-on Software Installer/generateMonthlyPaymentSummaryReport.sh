#!/bin/bash

#Windows Machine
#java -cp .\software; generateMonthlyPaymentSummaryReportOfAllInputFilesFromMasterList input/treatment/*.txt input/consultation/*.txt

#Linux Machine
#java -cp ./software: generateMonthlyPaymentSummaryReportOfAllInputFilesFromMasterList input/treatment/*.csv input/consultation/*.csv

#java -cp ./software: generateMonthlyPaymentSummaryReportOfAllInputFilesFromMasterList input/treatment/*.txt input/consultation/*.txt

java -cp ./software: generateMonthlyPaymentSummaryReportOfAllInputFilesFromMasterList input/treatment/*.txt

#PAUSE
