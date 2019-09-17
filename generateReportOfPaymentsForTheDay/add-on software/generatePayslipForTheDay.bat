set mainDirectory=%~dp0
cd /d %mainDirectory%
cd output/
del *.txt
cd ..
java -cp .\software;.\software\org.json.jar;.\software\org.apache.httpclient.jar;.\software\org.apache.httpcore.jar;.\software\org.apache.commons-logging.jar UsbongHTTPConnect http://localhost/ input/*.txt
#PAUSE
exit