import os
import re
import subprocess
import markdown
import shutil

PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DOCS_DIR = os.path.join(PROJECT_ROOT, "docs")
OUTPUT_HTML = os.path.join(DOCS_DIR, "DevOps_Miniproject_Report.html")
OUTPUT_PDF = os.path.join(DOCS_DIR, "DevOps_Miniproject_Report.pdf")
OUTPUT_PDF_FINAL = os.path.join(DOCS_DIR, "DevOps_Miniproject_Report_Final.pdf")

tasks = [
    {
        "num": 1,
        "phase": "Phase 1",
        "title": "Problem Definition, Real-World Need & MVP Scope",
        "doc_file": os.path.join(DOCS_DIR, "01-problem-definition.md"),
        "doc_id": "CCRP-DOC-01",
        "images": []
    },
    {
        "num": 2,
        "phase": "Phase 2",
        "title": "Agile Methodology, Product Backlog & DevOps Workflow",
        "doc_file": os.path.join(DOCS_DIR, "02-agile-plan.md"),
        "doc_id": "CCRP-DOC-02",
        "images": []
    },
    {
        "num": 3,
        "phase": "Phase 3",
        "title": "Software Requirements Specification & System Architecture",
        "doc_file": os.path.join(DOCS_DIR, "03-srs-architecture.md"),
        "doc_id": "CCRP-DOC-03",
        "images": []
    },
    {
        "num": 4,
        "phase": "Phases 4 - 6",
        "title": "Version Control, Feature Branching, Merge Conflict Resolution & Release Management",
        "doc_file": os.path.join(DOCS_DIR, "04-git-and-collaboration.md"),
        "doc_id": "CCRP-DOC-04",
        "images": [
            ("phase-05-feature-branch/01-apply-form.png", "Figure 4.1: Citizen Certificate Application Form"),
            ("phase-05-feature-branch/02-apply-validation-errors.png", "Figure 4.2: Client & Server-side Form Validation Error Feedback"),
            ("phase-05-feature-branch/03-apply-filled.png", "Figure 4.3: Completed Application with Validated Document Upload"),
            ("phase-05-feature-branch/04-acknowledgement.png", "Figure 4.4: Submission Acknowledgement with Generated Tracking ID"),
            ("phase-05-feature-branch/05-track-result-dark.png", "Figure 4.5: Citizen Application Tracking View (Dark Mode)"),
            ("phase-06-mvp/10-officer-login.png", "Figure 4.6: Officer Authentication Portal"),
            ("phase-06-mvp/11-officer-login-error.png", "Figure 4.7: Officer Authentication Guardrail & Error Handling"),
            ("phase-06-mvp/12-officer-queue.png", "Figure 4.8: Officer Consolidated Review Queue with KPI Filters"),
            ("phase-06-mvp/13-request-detail-submitted.png", "Figure 4.9: Application Details Inspection & Document Preview"),
            ("phase-06-mvp/14-reject-requires-reason.png", "Figure 4.10: Guardrail Enforcing Mandatory Reason on Application Rejection"),
            ("phase-06-mvp/15-request-approved.png", "Figure 4.11: Approval Decision with Automatic Certificate Generation"),
            ("phase-06-mvp/16-certificate.png", "Figure 4.12: Issued Digital Certificate Specimen with Cryptographic SHA-256 Hash"),
            ("phase-06-mvp/17-citizen-approved.png", "Figure 4.13: Citizen View of Approved Application with Certificate Download"),
            ("phase-06-mvp/18-citizen-rejected.png", "Figure 4.14: Citizen View of Rejected Application Showing Official Reason")
        ]
    },
    {
        "num": 5,
        "phase": "Phases 7 - 8",
        "title": "Continuous Integration with Jenkins & Tomcat Deployment",
        "doc_file": os.path.join(DOCS_DIR, "05-jenkins-ci-and-tomcat.md"),
        "doc_id": "CCRP-DOC-05",
        "images": [
            ("phase-07-jenkins-ci/01-jenkins-dashboard.png", "Figure 5.1: Jenkins Configuration-as-Code Controller Dashboard"),
            ("phase-07-jenkins-ci/02-ci-job.png", "Figure 5.2: Automated CI Build Pipeline Job"),
            ("phase-07-jenkins-ci/03-build-2-status.png", "Figure 5.3: Successful Build Status & SCM Revision Tracking"),
            ("phase-07-jenkins-ci/05-test-results.png", "Figure 5.4: Automated Test Execution Results (65 Tests Passing)"),
            ("phase-08-pipeline-tomcat/01-pipeline-stage-view.png", "Figure 5.5: Multi-Stage Declarative Pipeline Stage View"),
            ("phase-08-pipeline-tomcat/02-build-3-summary.png", "Figure 5.6: Pipeline Build Summary with Archived Artifacts"),
            ("phase-08-pipeline-tomcat/02b-pipeline-graph-build-3.png", "Figure 5.7: Declarative Pipeline Stage Execution Graph"),
            ("phase-08-pipeline-tomcat/03-build-with-parameters.png", "Figure 5.8: Parameterized Deployment Trigger Interface"),
            ("phase-08-pipeline-tomcat/06-coverage-report.png", "Figure 5.9: JaCoCo Code Coverage Report (90.5% Coverage)"),
            ("phase-08-pipeline-tomcat/08-tomcat-staging-home.png", "Figure 5.10: Deployed Application on Apache Tomcat 11 Staging"),
            ("phase-08-pipeline-tomcat/09-tomcat-production-apply.png", "Figure 5.11: Production Deployment on Tomcat 11")
        ]
    },
    {
        "num": 6,
        "phase": "Phase 9",
        "title": "Automated Browser Testing with Selenium WebDriver & Selenium Grid",
        "doc_file": os.path.join(DOCS_DIR, "06-selenium-test-plan.md"),
        "doc_id": "CCRP-DOC-06",
        "images": [
            ("phase-09-selenium/local-failsafe-html-report.png", "Figure 6.1: Maven Failsafe HTML Test Execution Report"),
            ("phase-09-selenium/passed-journeys/CitizenApplicationJourney_citizenAppliesAndSeesSubmittedStatus_20261011-210842.png", "Figure 6.2: Automated Browser Journey CJ-1 - Citizen Application Submission"),
            ("phase-09-selenium/passed-journeys/OfficerApprovalJourney_approvalIssuesACertificate_20261011-210920.png", "Figure 6.3: Automated Browser Journey CJ-4 - Officer Review & Certificate Issuance"),
            ("phase-09-selenium/passed-journeys/OfficerRejectionJourney_rejectionReasonReachesTheCitizen_20261011-210939.png", "Figure 6.4: Automated Browser Journey CJ-5 - Officer Rejection with Reason"),
            ("phase-09-selenium/passed-journeys/StatusTrackingJourney_trackingRequiresBothFields_20261011-211003.png", "Figure 6.5: Automated Browser Journey CJ-6 - Status Tracking Verification")
        ]
    },
    {
        "num": 7,
        "phase": "Phase 10",
        "title": "Continuous Testing & Automated Staging Quality Gate",
        "doc_file": os.path.join(DOCS_DIR, "07-continuous-testing-quality-gate.md"),
        "doc_id": "CCRP-DOC-07",
        "images": [
            ("phase-10-quality-gate/01-build-7-failed-stages.png", "Figure 7.1: Quality Gate Activation - Build #7 Halted on Staging E2E Failure"),
            ("phase-10-quality-gate/02-build-7-test-report.png", "Figure 7.2: Selenium Test Failure Report Identifying Injected Defect"),
            ("phase-10-quality-gate/04-build-7-artifacts.png", "Figure 7.3: Automatically Captured Failure Screenshots in Jenkins"),
            ("phase-10-quality-gate/05-build-8-rerun-stages.png", "Figure 7.4: Green Quality Gate on Bug Fix - Build #8 Deployed to Production"),
            ("phase-10-quality-gate/06-pipeline-history-and-trend.png", "Figure 7.5: Jenkins Pipeline Build History & Test Trend Graph")
        ]
    },
    {
        "num": 8,
        "phase": "Phases 11 - 12",
        "title": "Containerization with Docker, Private Registry & Continuous Deployment",
        "doc_file": os.path.join(DOCS_DIR, "08-docker-and-continuous-deployment.md"),
        "doc_id": "CCRP-DOC-08",
        "images": [
            ("phase-12-container-cd/01-build-13-stages.png", "Figure 8.1: Docker Build, Layer Extraction & Registry Push Stages in Jenkins"),
            ("phase-12-container-cd/02-pipeline-stage-view.png", "Figure 8.2: End-to-End Container Pipeline Execution Timeline"),
            ("phase-12-container-cd/03-production-container-8085.png", "Figure 8.3: Live Production Container Serving on Port 8085 with Active Health Probes")
        ]
    },
    {
        "num": 9,
        "phase": "Phases 13 - 14",
        "title": "Configuration Management with Ansible, Idempotency & Reliability",
        "doc_file": os.path.join(DOCS_DIR, "09-configuration-management.md"),
        "doc_id": "CCRP-DOC-09",
        "images": []
    },
    {
        "num": 10,
        "phase": "Phase 15",
        "title": "Final Delivery Report, Multi-Environment Release & Roadmap",
        "doc_file": os.path.join(DOCS_DIR, "10-final-report.md"),
        "doc_id": "CCRP-DOC-10",
        "images": []
    }
]

CSS = """
@page {
  size: A4;
  margin: 18mm 18mm 18mm 18mm;
  @bottom-right {
    content: "Page " counter(page);
    font-size: 10pt;
    font-family: "Times New Roman", Times, serif;
    color: #444444;
  }
  @bottom-left {
    content: "DevOps Mini-Project: GetDocs Portal";
    font-size: 10pt;
    font-family: "Times New Roman", Times, serif;
    color: #444444;
  }
}

@page :first {
  @bottom-right {
    content: none;
  }
  @bottom-left {
    content: none;
  }
}

* {
  box-sizing: border-box;
}

body {
  font-family: "Times New Roman", Times, serif;
  color: #000000;
  line-height: 1.5;
  font-size: 12pt;
  margin: 0;
  padding: 0;
}

/* 1ST PAGE: OFFICIAL HEADER / COVER PAGE */
.header-page {
  page-break-before: avoid;
  page-break-after: always;
  break-after: page;
  box-sizing: border-box;
  height: 250mm;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  text-align: center;
  padding: 30px 25px 25px 25px;
  border: 1px solid #000000;
  margin: 0;
}

.header-top {
  margin-top: 5px;
  text-align: center !important;
}

.inst-name {
  font-family: "Times New Roman", Times, serif;
  font-size: 15pt;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #000000;
  margin: 0 0 6px 0;
  text-align: center !important;
}

.course-name {
  font-family: "Times New Roman", Times, serif;
  font-size: 13pt;
  font-weight: normal;
  color: #222222;
  margin: 0 0 4px 0;
  text-align: center !important;
}

.acad-year {
  font-family: "Times New Roman", Times, serif;
  font-size: 12pt;
  color: #333333;
  margin: 0;
  text-align: center !important;
}

.header-middle {
  margin: auto 0;
  padding: 15px 0;
  text-align: center !important;
}

.mini-report-prefix {
  font-family: "Times New Roman", Times, serif;
  font-size: 12pt;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: #333333;
  margin: 0 0 12px 0;
  text-align: center !important;
}

.main-report-title {
  font-family: "Times New Roman", Times, serif;
  font-size: 20pt;
  font-weight: bold;
  color: #000000;
  letter-spacing: 0.5px;
  margin: 0 0 14px 0;
  border: none !important;
  border-bottom: none !important;
  padding-bottom: 0 !important;
  text-align: center !important;
}

.divider-line {
  width: 110px;
  height: 1.5px;
  background-color: #000000;
  margin: 0 auto 16px auto;
}

.project-name {
  font-family: "Times New Roman", Times, serif;
  font-size: 16pt;
  font-weight: bold;
  color: #000000;
  margin: 0 0 8px 0;
  border: none !important;
  text-align: center !important;
}

.header-bottom {
  margin-bottom: 5px;
  text-align: center !important;
}

.header-bottom .meta-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 22px;
  border: none !important;
}

.header-bottom .meta-table td {
  border: none !important;
  background: none !important;
  padding: 4px 8px;
  vertical-align: top;
  font-size: 12pt;
}

.header-bottom .meta-col-left * {
  text-align: left !important;
}

.header-bottom .meta-col-right * {
  text-align: right !important;
}

.meta-heading {
  font-family: "Times New Roman", Times, serif;
  font-size: 12pt;
  font-weight: bold;
  text-decoration: underline;
  margin-bottom: 8px;
}

.meta-line {
  font-family: "Times New Roman", Times, serif;
  margin: 3px 0;
  font-size: 12pt;
}

.submission-note {
  font-family: "Times New Roman", Times, serif;
  font-size: 11pt;
  font-style: italic;
  color: #333333;
  border-top: 1px solid #777777;
  padding-top: 10px;
  text-align: center !important;
}

/* 2ND PAGE: TABLE OF CONTENTS */
.toc-page {
  page-break-before: always;
  break-before: page;
  page-break-after: always;
  break-after: page;
  padding: 10px 0;
}

.toc-title {
  font-size: 14pt;
  font-weight: bold;
  color: #000000;
  border-bottom: 1.5px solid #000000;
  padding-bottom: 6px;
  margin-bottom: 20px;
}

.toc-table {
  width: 100%;
  border-collapse: collapse;
}

.toc-table th {
  background: none !important;
  color: #000000 !important;
  font-weight: bold;
  text-align: left;
  padding: 8px 6px;
  border-top: 1.5px solid #000000;
  border-bottom: 1.5px solid #000000;
  border-left: none;
  border-right: none;
  font-size: 12pt;
}

.toc-table td {
  padding: 8px 6px;
  border-bottom: 1px solid #e0e0e0;
  border-left: none;
  border-right: none;
  background: none !important;
  font-size: 12pt;
}

/* Task Sections */
.task-section {
  page-break-before: always;
  break-before: page;
  padding-top: 10px;
}

.task-header-simple {
  border-bottom: 1.5px solid #000000;
  padding-bottom: 6px;
  margin-bottom: 20px;
}

.task-number {
  font-size: 12pt;
  font-weight: bold;
  text-transform: uppercase;
  color: #000000;
  margin-bottom: 2px;
}

.task-title {
  margin: 0 0 4px 0;
  font-size: 14pt;
  font-weight: bold;
  color: #000000;
  border-bottom: none !important;
}

.task-doc-id {
  font-size: 11pt;
  font-style: italic;
  color: #444444;
}

/* Headings within content - ALL 14pt BOLD as requested */
h1, h2, h3, h4, h5, h6 {
  font-family: "Times New Roman", Times, serif;
  font-size: 14pt;
  font-weight: bold;
  color: #000000;
  margin-top: 20px;
  margin-bottom: 8px;
  page-break-after: avoid;
}

.task-body h1 {
  border-bottom: 1px solid #777777;
  padding-bottom: 4px;
}

/* Content paragraphs & lists - ALL 12pt as requested */
p, li, div, span {
  font-family: "Times New Roman", Times, serif;
  font-size: 12pt;
  color: #000000;
}

p {
  margin: 0 0 10px 0;
  text-align: justify;
}

ul, ol {
  margin: 0 0 12px 0;
  padding-left: 28px;
}

li {
  margin-bottom: 4px;
}

/* Blockquotes */
blockquote {
  border-left: 3px solid #000000;
  background: #ffffff;
  color: #000000;
  margin: 14px 0;
  padding: 8px 14px;
  font-style: italic;
}

/* Tables - NO black background! Simple borders */
table {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
  font-size: 11pt;
  page-break-inside: auto;
}

table th {
  background-color: #ffffff !important;
  color: #000000 !important;
  font-weight: bold;
  text-align: left;
  padding: 7px 8px;
  border-top: 1.5px solid #000000;
  border-bottom: 1.5px solid #000000;
  border-left: 1px solid #cccccc;
  border-right: 1px solid #cccccc;
  font-size: 11pt;
}

table td {
  background-color: #ffffff !important;
  color: #000000;
  padding: 6px 8px;
  border: 1px solid #cccccc;
  vertical-align: top;
  font-size: 11pt;
}

table tr td {
  background-color: #ffffff !important;
}

/* Code & Pre - Clean, NO dark background */
pre {
  background-color: #ffffff !important;
  color: #000000 !important;
  padding: 10px 12px;
  border: 1px solid #888888;
  border-radius: 2px;
  font-family: "Courier New", Courier, monospace;
  font-size: 10.5pt;
  line-height: 1.35;
  overflow-x: auto;
  margin: 14px 0;
  page-break-inside: avoid;
}

code {
  font-family: "Courier New", Courier, monospace;
  font-size: 11pt;
  background-color: #ffffff;
  color: #000000;
  padding: 1px 3px;
  border: 1px solid #cccccc;
}

pre code {
  background: transparent;
  color: inherit;
  padding: 0;
  border: none;
}

/* Gallery & Figures */
.evidence-gallery {
  margin-top: 24px;
  border-top: 1.5px solid #000000;
  padding-top: 16px;
  page-break-inside: auto;
}

.gallery-title {
  font-size: 14pt;
  font-weight: bold;
  color: #000000;
  margin-bottom: 14px;
}

.figure-card {
  margin: 18px 0;
  text-align: center;
  page-break-inside: avoid;
  border: 1px solid #cccccc;
  background: #ffffff;
  padding: 10px;
}

.figure-card img {
  max-width: 90%;
  max-height: 480px;
  height: auto;
  border: 1px solid #888888;
}

.figure-caption {
  margin-top: 8px;
  font-size: 11pt;
  font-weight: bold;
  color: #000000;
  font-family: "Times New Roman", Times, serif;
}

/* Mermaid */
.mermaid {
  text-align: center;
  margin: 16px 0;
  background: #ffffff;
  padding: 10px;
  border: 1px solid #cccccc;
  page-break-inside: avoid;
}
"""

def clean_markdown(md_text, doc_dir):
    def replace_img(match):
        alt = match.group(1)
        src = match.group(2)
        if not src.startswith("http") and not src.startswith("file:"):
            abs_path = os.path.abspath(os.path.join(doc_dir, src)).replace('\\', '/')
            return f"![{alt}](file:///{abs_path})"
        return match.group(0)

    md_text = re.sub(r'!\[(.*?)\]\((.*?)\)', replace_img, md_text)
    
    # Strip any top-level single H1 if it duplicates our task header
    lines = md_text.split('\n')
    new_lines = []
    skipped_first_h1 = False
    for line in lines:
        if line.startswith('# ') and not skipped_first_h1:
            skipped_first_h1 = True
            continue
        new_lines.append(line)
        
    return '\n'.join(new_lines)

def build_report():
    print("Converting Markdown files...")
    md_parser = markdown.Markdown(extensions=['tables', 'fenced_code'])
    
    task_html_blocks = []
    toc_rows = []
    
    for task in tasks:
        print(f"Processing Task {task['num']}: {task['title']}")
        with open(task['doc_file'], 'r', encoding='utf-8') as f:
            raw_md = f.read()
            
        cleaned_md = clean_markdown(raw_md, os.path.dirname(task['doc_file']))
        html_content = md_parser.convert(cleaned_md)
        md_parser.reset()
        
        # Format mermaid code blocks
        html_content = re.sub(r'<pre><code class="language-mermaid">([\s\S]*?)</code></pre>', r'<pre class="mermaid">\1</pre>', html_content)
        
        # Build task evidence gallery if any images
        gallery_html = ""
        if task['images']:
            gallery_html += '<div class="evidence-gallery">'
            gallery_html += f'<div class="gallery-title">Verification Evidence & Screenshots (Task {task["num"]})</div>'
            for rel_img, caption in task['images']:
                img_abs = os.path.abspath(os.path.join(DOCS_DIR, "evidence", rel_img)).replace('\\', '/')
                if os.path.exists(os.path.join(DOCS_DIR, "evidence", rel_img)):
                    gallery_html += f'''
                    <div class="figure-card">
                        <img src="file:///{img_abs}" alt="{caption}" />
                        <div class="figure-caption">{caption}</div>
                    </div>
                    '''
            gallery_html += '</div>'
            
        task_block = f'''
        <section class="task-section" id="task-{task['num']}">
            <div class="task-header-simple">
                <div class="task-number">TASK {task['num']:02d} &bull; {task['phase'].upper()}</div>
                <h1 class="task-title">{task['title']}</h1>
                <div class="task-doc-id">Reference Document: {task['doc_id']}</div>
            </div>
            <div class="task-body">
                {html_content}
                {gallery_html}
            </div>
        </section>
        '''
        task_html_blocks.append(task_block)
        
        toc_rows.append(f'''
        <tr>
            <td style="width:14%; font-weight:bold;">Task {task['num']:02d}</td>
            <td style="width:72%;"><strong>{task['title']}</strong><br><span style="font-size:11pt; color:#444444;">{task['phase']} &bull; Document ID: {task['doc_id']}</span></td>
            <td style="width:14%; text-align:right;"><a href="#task-{task['num']}" style="color:#000000; text-decoration:underline;">Section {task['num']:02d} &rarr;</a></td>
        </tr>
        ''')

    mermaid_src = os.path.abspath(os.path.join(DOCS_DIR, 'mermaid.min.js')).replace('\\', '/')
    full_html = f'''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>DevOps Miniproject Report - Tanishq Kulkarni</title>
    <style>
        {CSS}
    </style>
    <script src="file:///{mermaid_src}"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {{
            if (window.mermaid) {{
                mermaid.initialize({{ startOnLoad: true, theme: 'neutral' }});
            }}
        }});
    </script>
</head>
<body>

    <!-- 1ST PAGE: OFFICIAL HEADER PAGE (COVER PAGE) -->
    <div class="header-page">
        <div class="header-top">
            <div class="inst-name">DEPARTMENT OF COMPUTER ENGINEERING</div>
            <div class="course-name">DevOps Laboratory &bull; Mini-Project Report</div>
            <div class="acad-year">Academic Year 2025&ndash;2026</div>
        </div>

        <div class="header-middle">
            <div class="mini-report-prefix">A MINI-PROJECT REPORT ON</div>
            <div class="main-report-title">DEVOPS MINI-PROJECT REPORT</div>
            <div class="divider-line"></div>
            <div class="project-name">GetDocs &mdash; Citizen Certificate Request Portal</div>
        </div>

        <div class="header-bottom">
            <table class="meta-table">
                <tr>
                    <td class="meta-col-left" style="width: 50%;">
                        <div class="meta-heading">SUBMITTED BY:</div>
                        <div class="meta-line"><strong>Name:</strong> Tanishq Kulkarni</div>
                        <div class="meta-line"><strong>Roll No:</strong> 24102A2007</div>
                        <div class="meta-line"><strong>Branch / Div:</strong> CMPN / A</div>
                    </td>
                    <td class="meta-col-right" style="width: 50%;">
                        <div class="meta-heading">PROJECT GUIDE:</div>
                        <div class="meta-line"><strong>Prof. Snehal Andhare</strong></div>
                        <div class="meta-line">Department of Computer Engineering</div>
                    </td>
                </tr>
            </table>

            <div class="submission-note">
                Submitted in partial fulfillment of the requirements for the Degree of Bachelor of Engineering in Computer Engineering
            </div>
        </div>
    </div>

    <!-- 2ND PAGE: TABLE OF CONTENTS / EXECUTIVE INDEX -->
    <div class="toc-page">
        <h2 class="toc-title">Table of Tasks & Executive Index</h2>
        <table class="toc-table">
            <thead>
                <tr>
                    <th style="width: 14%;">Task No</th>
                    <th style="width: 72%;">Task Title & Scope</th>
                    <th style="width: 14%; text-align:right;">Reference Link</th>
                </tr>
            </thead>
            <tbody>
                {''.join(toc_rows)}
            </tbody>
        </table>
    </div>

    <!-- PAGES 3+: TASK CONTENTS (STRICT SEQUENTIAL ORDER TASK 1 TO TASK 10) -->
    {''.join(task_html_blocks)}

</body>
</html>
'''

    print(f"Writing updated HTML to {OUTPUT_HTML}...")
    with open(OUTPUT_HTML, "w", encoding="utf-8") as f:
        f.write(full_html)
    print("HTML report successfully written.")

    print(f"Rendering simplified Times New Roman PDF via Headless Edge...")
    edge_path = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
    cmd = [
        edge_path,
        "--headless",
        "--disable-gpu",
        "--no-pdf-header-footer",
        f"--print-to-pdf={OUTPUT_PDF}",
        OUTPUT_HTML
    ]
    subprocess.run(cmd, check=True)
    if os.path.exists(OUTPUT_PDF):
        size_mb = os.path.getsize(OUTPUT_PDF) / (1024 * 1024)
        print(f"PDF successfully updated at: {OUTPUT_PDF} ({size_mb:.2f} MB)")
        shutil.copyfile(OUTPUT_PDF, OUTPUT_PDF_FINAL)
        print(f"Fresh copy created at: {OUTPUT_PDF_FINAL}")
    else:
        print("Error: PDF was not created.")

if __name__ == "__main__":
    build_report()
