package com.sanad.portal.domain;

/** The four certificates in the MVP, with the metadata the UI and the officer need. */
public enum CertificateType {

    BIRTH("Birth certificate", "Birth", "BIR", 7,
            "Hospital birth record or discharge summary",
            "Proof of date and place of birth for school admission, passports and records.", "i-baby"),
    INCOME("Income certificate", "Income", "INC", 5,
            "Salary slip, Form 16 or ITR acknowledgement",
            "Annual family income declaration for scholarships, subsidies and fee waivers.", "i-wallet"),
    DOMICILE("Domicile certificate", "Domicile", "DOM", 10,
            "Address proof: utility bill, rent agreement or ration card",
            "Proof of permanent residence in the state for admissions and recruitment.", "i-home"),
    MARRIAGE("Marriage certificate", "Marriage", "MAR", 14,
            "Wedding invitation or a photograph of the ceremony",
            "Registration of marriage for visas, name changes and joint applications.", "i-heart");

    private final String label;
    private final String shortLabel;
    private final String code;
    private final int slaDays;
    private final String requiredDocument;
    private final String description;
    private final String icon;

    CertificateType(String label, String shortLabel, String code, int slaDays,
                    String requiredDocument, String description, String icon) {
        this.label = label;
        this.shortLabel = shortLabel;
        this.code = code;
        this.slaDays = slaDays;
        this.requiredDocument = requiredDocument;
        this.description = description;
        this.icon = icon;
    }

    public String getLabel() { return label; }
    public String getShortLabel() { return shortLabel; }
    public String getCode() { return code; }
    public int getSlaDays() { return slaDays; }
    public String getRequiredDocument() { return requiredDocument; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}
