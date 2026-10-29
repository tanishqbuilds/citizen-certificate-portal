from pdfminer.high_level import extract_pages
from pdfminer.layout import LTTextContainer
import re

pdf_path = r"docs/DevOps_Miniproject_Report.pdf"

print("Searching for 'task 8' or 'task 08' in PDF...")
for page_layout in extract_pages(pdf_path):
    p_num = page_layout.pageid
    text = ""
    for element in page_layout:
        if isinstance(element, LTTextContainer):
            text += element.get_text()
    
    if re.search(r'task\s*0?8\b', text, re.IGNORECASE):
        print(f"Page {p_num} contains Task 8:")
        lines = [l.strip() for l in text.splitlines() if re.search(r'task\s*0?8\b', l, re.IGNORECASE)]
        print("  Lines:", lines)

