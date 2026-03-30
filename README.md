<table align="center">
  <tr>
    <td align="center">
      <img src="assets/PCN Logo.png" alt="Peninsula College - The Ship Campus Logo" width="250">
    </td>
    <td align="center">
      <img src="assets/NFCampus Logo.png" alt="NFCampus Logo" width="125">
    </td>
    <td align="center">
      <img src="assets/UoP Logo.png" alt="University of Plymouth Logo" width="320">
    </td>
  </tr>
</table>

<h1 align="center">NFCampus — Smart NFC Campus Access System</h1>
<p align="center">An Android-based NFC campus access solution that acts as a secure mobile backup to traditional student identification cards.</p>

**Course:** MAL2020 Computing Group Project  
**Platform:** Android (Kotlin)

---

# 👥 Group Members

| Name | Student ID |
|-----|-----|
| **Eu Jin Yang** | BSSE2506021 |
| **Ng Kean Chun** | BSSE2506036 |
| **Teoh Yi Shan** | BSSE2506022 |
| **Yong Ken** | BSSE2506025 |

---

# 📱 Project Overview

**NFCampus** is an Android mobile application designed to modernize campus access systems by back up traditional **physical RFID access cards** with **smartphone-based NFC authentication**.

Students and staff can simply **tap their phone on NFC readers** to gain access to campus facilities such as:

- Classrooms
- Laboratories
- Libraries
- Restricted staff areas

This solution reduces problems such as:

- Lost or forgotten access cards
- Card damage or replacement costs
- Administrative overhead for card management

---

# 🎯 Key Features

## 📲 NFC Mobile Access
- Tap phone on NFC reader to authenticate
- Back up physical campus access cards
- Secure credential verification

## 🔔 Access Alerts & Notifications
- Receive alerts when access is denied
- Real-time status updates

## ⚙️ Background NFC Support
- Works even when the app is **not open**
- Can be enabled by setting the app as the **Default Payment App**

## 🔐 Secure Authentication
- User identity validation
- Protection against unauthorized access

---

# 📂 Project Structure

```
NFCampus
app/
├── manifests/
│   └── AndroidManifest.xml
├── kotlin+java/
│   └── com.example.nfcampus/
│        ├── dialog/
│        │   ├── LoginVerificationDialog.kt
│        │   └── RegistrationVerificationDialog.kt
│        ├── gui/
│        │   ├── about/
│        │   │   ├── PrivacyPolicyScreen.kt
│        │   │   └── TermsOfServiceScreen.kt
│        │   ├── access_NFC/
│        │   │   ├── LinkedCardScreen.kt
│        │   │   └── NFCTroubleshootingScreen.kt
│        │   ├── account_security/
│        │   │   ├── ChangeEmailPasswordScreen.kt
│        │   │   ├── EmailChangeScreen.kt
│        │   │   └── PasswordChangeScreen.kt
│        │   ├── components/
│        │   │   ├── CaptureImageContract.kt
│        │   │   ├── NFCSetupStep.kt
│        │   │   ├── QRCodeGenerator.kt
│        │   │   ├── RegistrationFormStep.kt
│        │   │   └── StudentCardScanStep.kt
│        │   ├── CardScannerScreen.kt
│        │   ├── ForgotPasswordScreen.kt
│        │   ├── LoginScreen.kt
│        │   ├── MainScreen.kt
│        │   ├── ProfileScreen.kt
│        │   ├── RegisterScreen.kt
│        │   ├── ScanCardContract.kt
│        │   ├── ScannerActivity.kt
│        │   └── SettingsScreen.kt
│        ├── model/
│        │   └── User.kt
│        ├── nfc/
│        │   └── HCECardService.kt
│        ├── repository/
│        │   ├── ActivityLogRepository.kt
│        │   └── UserRepository.kt
│        ├── util/
│        │   ├── ImageProcessor.kt
│        │   ├── NotificationHelper.kt
│        │   └── ScannedData.kt
│        ├── viewmodel/
│        │   ├── AuthViewModel.kt
│        │   ├── EmailChangeViewModel.kt
│        │   ├── ForgotPasswordViewModel.kt
│        │   └── PasswordChangeViewModel.kt
│        └── MainActivity.kt
└── res/
     ├── drawable/
     │   ├── ic_check.xml
     │   ├── ic_forgotpassword.xml
     │   ├── ic_launcher_background.xml
     │   ├── ic_launcher_foreground.xml
     │   ├── ic_nfcampus.xml
     │   ├── ic_nfctroubleshooting.xml
     │   └── ic_sentemail.xml
     └── xml/
          ├── apduservice.xml
          ├── backup_rules.xml
          ├── data_extraction_rules.xml
          ├── file_paths.xml
          ├── network_security_config.xml
          └── nfc_tech_filter.xml
```

---

# 🛠 Technologies Used

| Technology | Purpose |
|------------|---------|
| **Kotlin** | Android application development |
| **XML** | Android UI layout design |
| **Android Studio** | Development environment |
| **NFC Technology** | Contactless authentication |
| **Figma** | UI/UX design prototype |

---

# 🚀 Getting Started

## Prerequisites

- Android Studio
- Android device with NFC support
- Minimum Android SDK required by the project

---

## Clone the Repository

```bash
git clone https://github.com/JustJin6/NFCampus.git
```

---

## Run the Application

1. Open the project in **Android Studio**
2. Sync Gradle dependencies
3. Connect an **NFC-enabled Android device**
4. Run the application

---

# 🧪 Testing

## NFC Access Testing

1. Install the app on an **NFC-enabled device**
2. Enable **NFC in device settings**
3. Tap the phone on the NFC reader
4. Verify authentication response

---

## Background NFC Testing

1. Set the app as **Default Payment App**
2. Lock the phone or switch to another app
3. Tap phone on NFC reader
4. Verify that the system still responds

---

# 📑 Project Documentation

All project documentation can be found in the **Document branch**, including:

- Project Proposal
- UML Diagrams
- Client Meeting Minutes
- Weekly Progress Reports
- Individual Contribution Report
- Presentation Slides

---

# 🎥 Project Presentation

- **Presentation Slides:** Available in the repository [`Presentation Slides/NFCampus (Android) - Final Presentation`](#)  
- **Video Demonstration:** https://youtu.be/fZcIQbSTMfs

---

# 🎨 UI/UX Design

The system interface was designed using **Figma**.

Figma prototype link can be found in:

```
assets/Figma_Link.md
```

---

# 🔐 Security Considerations

- Secure NFC authentication mechanism
- Protection against unauthorized access
- Controlled access management

---

# 📄 License

This project was developed for **academic purposes** under the **MAL2020 Computing Group Project**.

---

# 🙏 Acknowledgements

- Course instructors and supervisors
- Android development community
- NFC technology documentation and resources

---

⭐ **NFCampus — Simplifying campus access with NFC technology.**
