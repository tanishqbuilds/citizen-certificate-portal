import os, re

with open("docs/DevOps_Miniproject_Report.html", "r", encoding="utf-8") as f:
    text = f.read()

print("HTML length:", len(text))
print("Cover page at:", text.find("cover-page"))
print("TOC page at:", text.find("toc-page"))

matches = re.findall(r'<h1 class="task-title">(.*?)</h1>', text)
for i, m in enumerate(matches, 1):
    print(f"Task {i}: {m}")

