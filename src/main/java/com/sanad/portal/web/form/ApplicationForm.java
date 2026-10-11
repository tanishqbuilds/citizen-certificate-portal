package com.sanad.portal.web.form;

import com.sanad.portal.domain.CertificateType;
import com.sanad.portal.web.validation.ConsistentApplication;
import com.sanad.portal.web.validation.NationalId;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Citizen application form (CCR-3, CCR-4). The document is bound separately as a MultipartFile. */
@ConsistentApplication
public class ApplicationForm {

    static final String NAME_PATTERN = "^[\\p{L} .'-]+$";

    @NotNull(message = "Choose the certificate you need.")
    private CertificateType certificateType;

    @NotBlank(message = "Enter the applicant’s full name.")
    @Size(min = 3, max = 100, message = "The name must be between 3 and 100 characters.")
    @Pattern(regexp = NAME_PATTERN, message = "The name can contain letters, spaces, dots, apostrophes and hyphens only.")
    private String applicantName;

    @NotNull(message = "Enter the date of birth.")
    @Past(message = "The date of birth must be in the past.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Enter an email address — you will need it to track the request.")
    @Email(message = "Enter a valid email address, like name@example.com.")
    @Size(max = 254, message = "The email address is too long.")
    private String email;

    @NotBlank(message = "Enter a mobile number.")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a 10-digit Indian mobile number starting with 6, 7, 8 or 9.")
    private String phone;

    @NotBlank(message = "Enter the 12-digit national ID.")
    @NationalId
    private String nationalId;

    @NotBlank(message = "Enter the residential address.")
    @Size(min = 10, max = 300, message = "The address must be between 10 and 300 characters.")
    private String address;

    @NotBlank(message = "Enter the district.")
    @Size(min = 2, max = 60, message = "The district must be between 2 and 60 characters.")
    private String district;

    @NotBlank(message = "Enter the PIN code.")
    @Pattern(regexp = "^[1-9]\\d{5}$", message = "Enter a valid 6-digit PIN code.")
    private String pinCode;

    @NotBlank(message = "Tell us why you need the certificate.")
    @Size(min = 10, max = 300, message = "The purpose must be between 10 and 300 characters.")
    private String purpose;

    @PositiveOrZero(message = "Annual income cannot be negative.")
    @Max(value = 100_000_000, message = "Annual income must be at most ₹10,00,00,000.")
    private Long annualIncome;

    @Size(max = 100, message = "The spouse’s name must be at most 100 characters.")
    @Pattern(regexp = "^$|" + NAME_PATTERN, message = "The spouse’s name can contain letters, spaces, dots, apostrophes and hyphens only.")
    private String spouseName;

    @AssertTrue(message = "Please confirm that the information is true.")
    private boolean declaration;

    /** Trims text inputs so that "  " never passes as a value and stored data is clean. */
    public void normalize() {
        applicantName = trim(applicantName);
        email = email == null ? null : email.trim().toLowerCase();
        phone = phone == null ? null : phone.replaceAll("[\\s-]", "");
        address = trim(address);
        district = trim(district);
        pinCode = trim(pinCode);
        purpose = trim(purpose);
        spouseName = trim(spouseName);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim().replaceAll("\\s{2,}", " ");
    }

    public CertificateType getCertificateType() { return certificateType; }
    public void setCertificateType(CertificateType certificateType) { this.certificateType = certificateType; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public Long getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Long annualIncome) { this.annualIncome = annualIncome; }
    public String getSpouseName() { return spouseName; }
    public void setSpouseName(String spouseName) { this.spouseName = spouseName; }
    public boolean isDeclaration() { return declaration; }
    public void setDeclaration(boolean declaration) { this.declaration = declaration; }
}
