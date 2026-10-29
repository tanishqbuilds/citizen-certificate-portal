from pdfminer.high_level import extract_pages
from pdfminer.layout import LTTextContainer
import re

pdf_path = r"docs/DevOps_Miniproject_Report.pdf"

print("Scanning all pages for Task headers...")
for page_layout in extract_pages(pdf_path):
    p_num = page_layout.pageid
    text = ""
    for element in page_layout:
        if isinstance(element, LTTextContainer):
            text += element.get_text()
    
    matches = re.findall(r'TASK\s+\d+', text, re.IGNORECASE)
    if matches:
        first_line = [l.strip() for l in text.splitlines() if l.strip()][:3]
        print(f"Page {p_num}: {matches} -> {first_line}")
    elif p_num <= 5:
        first_line = [l.strip() for l in text.splitlines() if l.strip()][:2]
        print(f"Page {p_num}: (no task header) -> {first_line}")

