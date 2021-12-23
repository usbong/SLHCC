#!/bin/bash
#
# Copyright 2021 SYSON, MICHAEL B.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
#     
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# Generate Annual Year End Summary Report of All Input Files (.csv format) 
# from Unit workbook (non-Master List) using Linux Personal Computer (PC)
#
# company: USBONG
# author: SYSON, MICHAEL B.
# date created: 20211223
# last updated: 20211223
# website address: http://www.usbong.ph
#


mainDirectory=$(pwd)
# echo $mainDirectory
cd "$mainDirectory"
# cd "software"

# Java command using Windows Machine
# java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ halimbawa

# Java command using Windows Machine
#java -cp .\software;.\software\org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles input/consultation/*.csv input/treatment/*.csv assets/*.txt

# Java command using Linux Machine
java -cp ./software:./software/org.apache.commons.text.jar generateAnnualYearEndSummaryReportOfAllInputFiles input/consultation/*.csv input/treatment/*.csv assets/*.txt

exit
