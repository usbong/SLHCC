mainDirectory=$(pwd)
# echo $mainDirectory
cd "$mainDirectory"
# cd "software"

# ls -l

#java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongReportMain http://localhost/ input/*.txt

# Java command using Windows Machine
# java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ halimbawa

# Java command using Linux Machine
java -cp ./software:./software/org.json.jar:./software/org.apache.httpclient.jar:./software/org.apache.httpcore.jar:./software/org.apache.commons-logging.jar UsbongSMSReportMain http://localhost/ ./input/*.txt

# java -cp .\software:.\software\org.json.jar:.\software\org.apache.httpclient.jar:.\software\org.apache.httpcore.jar:.\software\org.apache.commons-logging.jar ./software/UsbongReportMain http://localhost/ halimbawa


# java -cp .:org.json.jar:org.apache.httpclient.jar:org.apache.httpcore.jar:org.apache.commons-logging.jar UsbongReportMain

cd ..
# %2
#PAUSE
exit
