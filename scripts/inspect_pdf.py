from pdfminer.high_level import extract_pages
from pdfminer.layout import LTTextContainer
import os

pdf_path = r"docs/DevOps_Miniproject_Report.pdf"

for page_layout in extract_pages(pdf_path, maxpages=10):
    page_num = page_layout.pageid
    print(f"=== PAGE {page_num} ===")
    page_text = ""
    for element in page_layout:
        if isinstance(element, LTTextContainer):
            page_text += element.get_text()
    first_lines = "\n".join([line for line in page_text.splitlines() if line.strip()][:6])
    print(first_lines)
    print()

