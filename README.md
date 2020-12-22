![](https://github.com/qqdog1/bsr_data_downloader/workflows/Java%20CI/badge.svg)

# 事前設定  
## 1. 設定 bsr.conf 檔案.  
bsr_download_folder=你要下載到哪個資料夾放  
google_drive_folder_id=你的google drive資料夾，下載完後將會把檔案上傳到這邊

![](/pic/fid.jpg)  
你可以在這個地方找到自己的folder id

## 2. 修改 log4j2.xml  
修改log4j2.xml檔案 如果有需要

## 3. 下載 google api credentials
Go to https://developers.google.com/drive/api/v3/quickstart/java
![](/pic/g01.jpg)  
![](/pic/g02.jpg)  
將檔案下載後放到./config資料夾內

## 4. 啟動這個程式.

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
      
* keytool 預設密碼為 changeit
