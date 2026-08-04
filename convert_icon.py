from PIL import Image

# 读取原图
src = r"C:\Users\Administrator\Desktop\b_9b9c4248b7e1c3ef58eebdc0be0ae432.jpg"
dst = r"C:\Users\Administrator\Desktop\mod\V5\src\main\resources\assets\portal-preview\icon.png"

img = Image.open(src).convert("RGBA")
# 缩放为 256x256 (Fabric 推荐尺寸)
img = img.resize((256, 256), Image.LANCZOS)
img.save(dst, "PNG")
print(f"✅ 图标已保存: {dst}")
print(f"   尺寸: 256x256 PNG")
