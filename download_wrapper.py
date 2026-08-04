import urllib.request
import os

url = "https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar"
output = r"C:\Users\Administrator\Desktop\mod\V5\gradle\wrapper\gradle-wrapper.jar"

try:
    print("正在下载 gradle-wrapper.jar...")
    urllib.request.urlretrieve(url, output)
    size = os.path.getsize(output)
    print(f"下载成功！文件大小: {size} bytes")
except Exception as e:
    print(f"下载失败: {e}")
