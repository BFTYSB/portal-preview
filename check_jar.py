import zipfile

jar_path = r"C:\Users\Administrator\Desktop\mod\V5\gradle\wrapper\gradle-wrapper.jar"
try:
    with zipfile.ZipFile(jar_path, 'r') as z:
        names = z.namelist()
        wrapper_files = [n for n in names if 'wrapper' in n.lower() or 'gradle' in n.lower()][:10]
        print(f"文件总数: {len(names)}")
        print("相关文件:")
        for f in wrapper_files:
            print(f"  {f}")
        main = [n for n in names if 'GradleWrapperMain' in n]
        print(f"\nGradleWrapperMain: {'找到' if main else '未找到'}")
        if main:
            print(f"  {main[0]}")
except Exception as e:
    print(f"错误: {e}")
