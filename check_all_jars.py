import zipfile

paths = [
    r"C:\Users\Administrator\Downloads\gradle-wrapper.jar",
    r"C:\Users\Administrator\IdeaProjects\HelloWorkPlugin\gradle\wrapper\gradle-wrapper.jar"
]

for p in paths:
    try:
        with zipfile.ZipFile(p, 'r') as z:
            names = z.namelist()
            has_main = any('GradleWrapperMain' in n for n in names)
            print(f"✅ {p}")
            print(f"   文件有效，包含 {len(names)} 个条目")
            print(f"   GradleWrapperMain: {'有' if has_main else '无'}")
    except Exception as e:
        print(f"❌ {p}")
        print(f"   错误: {e}")
