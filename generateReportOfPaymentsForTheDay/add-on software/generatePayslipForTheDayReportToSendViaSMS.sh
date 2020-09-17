mainDirectory=$(pwd)
# echo $mainDirectory
cd "$mainDirectory"
# cd "software"

# ls -l

#java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongReportMain http://localhost/ input/*.txt

# Java command using Windows Machine
# java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ halimbawa

# example output: 2020-09-17
# echo $(date +%F)

# example output: September 2020
# echo $(date +%B) $(date +%Y)
sMonthYear=$(date +%B)" "$(date +%Y)

# echo $sMonthYear

# Java command using Linux Machine
java -cp ./software:./software/org.json.jar:./software/org.apache.httpclient.jar:./software/org.apache.httpcore.jar:./software/org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ ./input/*"$sMonthYear".txt

# java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar ./software/UsbongReportMain http://localhost/ halimbawa


# java -cp .:org.json.jar:org.apache.httpclient.jar:org.apache.httpcore.jar:org.apache.commons-logging.jar UsbongReportMain

cd ..
# %2
#PAUSE
exit
