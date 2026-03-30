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

# 📱 Project Overview

**NFCampus** is an Android mobile application designed to modernize campus access systems by back up traditional **physical RFID access cards** with **smartphone-based NFC authentication**.

Students can simply **tap their phone on NFC readers** to gain access to campus facilities such as:

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

- **NFC Mobile Access** — Tap phone on ESP32-based readers
- **OCR Card Registration** — Scan physical cards with camera
- **One Device, One Account** — Hardware binding prevents sharing
- **Activity Logging** — Full audit trail in Firestore
- **QR Fallback** — Manual verification when NFC unavailable
- **Background Support** — Works when app isn't open

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

<h1 align="center">🛠 Technologies Used</h1>
<div align="center">

| Badge | Description |
|-------|-------------|
| <div align="center">![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)</div> | **Kotlin**: Primary language for Android development |
| <div align="center">![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)</div> | **Jetpack Compose**: Modern Android UI toolkit |
| <div align="center">![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)</div> | **Android Studio**: Official IDE for Android development |
| <div align="center">![NFC](https://img.shields.io/badge/NFC_Technology-0A0A0A?style=for-the-badge&logoColor=white)</div> | **NFC Technology**: Contactless authentication system |
| <div align="center">![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)</div> | **Figma**: UI/UX design and prototyping |
| <div align="center">![Arduino](https://img.shields.io/badge/Arduino_IDE-00979D?style=for-the-badge&logo=arduino&logoColor=white)</div> | **Arduino IDE**: Programming microcontrollers |
| <div align="center">![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)</div> | **Firebase & Firestore**: Backend services and real-time database |
| <div align="center">![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)</div> | **Node.js (Express.js)**: Backend API development |
| <div align="center">![ESP32](https://img.shields.io/badge/ESP32-000000?style=for-the-badge&logo=espressif&logoColor=white)</div> | **ESP32, PN532 & LCD**: Embedded hardware components |
| <div align="center">![ML Kit](https://img.shields.io/badge/Google_ML_Kit-4285F4?style=for-the-badge&logo=google&logoColor=white)</div> | **Google ML Kit & CameraX**: OCR and camera processing |
| <div align="center">![ZXing](https://img.shields.io/badge/ZXing-000000?style=for-the-badge&logoColor=white)</div> | **ZXing**: Barcode / QR code scanning |
| <div align="center">![Coil](https://img.shields.io/badge/Coil-FF6F00?style=for-the-badge&logo=android&logoColor=white)</div> | **Coil**: Image loading for Android |

</div>

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

<h1 align="center">🎥 Project Presentation</h1>

<p align="center">
  📊 <a href="./Presentation%20Slides/NFCampus%20(Android)%20-%20Final%20Presentation.pptx">View Slides</a> •
  🎬 <a href="https://youtu.be/fZcIQbSTMfs">Watch Demo</a>
</p>

---

<h1 align="center">🎨 UI / UX Design</h1>
<p align="center">
  📱 <a href="https://www.figma.com/design/y4SipWwkOrDogSPzE1BiCH/NFCampus---Android?node-id=0-1&t=UWFm8yZLmQSnHGer-1">View System Interface (Figma)</a>
</p>

---

<h1 align="center">👥 Team</h1>
<div align="center">

| 👤 Name | 🆔 Student ID | 🎯 Role |
|:------:|:-------------:|:--------:|
| **Eu Jin Yang** | BSSE2506021 | 🧑‍💻 Team Leader & Full-Stack Developer / NFC & Security Lead |
| **Ng Kean Chun** | BSSE2506036 | 📱 Project Analyst & OCR Developer / Frontend Specialist |
| **Teoh Yi Shan** | BSSE2506022 | 🔌 Client Communication Lead & Meeting Secretary / Documentation Coordinator |
| **Yong Ken** | BSSE2506025 | 📊 System Design & Planning Lead / UML Modeling Specialist |

</div>

---

# 📄 License

This project was developed for **academic purposes** under the **MAL2020 Computing Group Project**.
