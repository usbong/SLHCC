#!/bin/bash

# Copyright 2022 SYSON, MICHAEL B.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.
#
# Generate Monthly Summary Report 
# for Each Medical Doctor of ALL Input Files
# Linux Personal Computer (PC)
#
# @company: USBONG
# @author: SYSON, MICHAEL B.
# @date created: 20220811
# @last modified: 20220811
# @website address: http://www.usbong.ph
#

java -cp ./software:./software/org.apache.commons.text.jar generateMonthlySummaryReportForEachMedicalDoctorOfAllInputFiles ./input/consultation/*
