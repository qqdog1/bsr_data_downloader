![](https://github.com/qqdog1/bsr_data_downloader/workflows/Java%20CI/badge.svg)

# 1. Before start  
Set bsr.conf file.  
bsr_download_folder=where you want to put your files  

Modify log4j2.xml file if you want.  
  
Then just run this program.

# 遇到 "PKIX path building failed"
## 1.下載憑證
![https://www.twse.com.tw/zh/](/pic/1.jpg)  
* 到https://www.twse.com.tw/zh/  
* 點選左上角小鎖  
* 點選"憑證"  

![](/pic/2.jpg)  
* 點選"詳細資料"  
* 點選"複製到檔案"  

![](/pic/3.jpg)  
* 按下下一步
* 選擇BASE64編碼
* 儲存

![](/pic/4.jpg)  
* 到https://bsr.twse.com.tw/bshtm/  
* 重複上述步驟，儲存bsr網頁的憑證  

## 2.匯入憑證  
* 打開command line  
* 到JAVA_HOME/jre/lib/security  
* 輸入下列指令匯入憑證 -file 後方請指定前面下載的憑證  

      keytool -import -alias twse -keystore cacerts -file D:\twse.cer
      keytool -import -alias bsr -keystore cacerts -file D:\bsr.cer 
