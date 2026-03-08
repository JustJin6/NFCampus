# NFCampus — Smart NFC Campus Access System

> **An Android-based NFC campus access solution that replaces physical student cards with a secure mobile credential system.**

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

**NFCampus** is an Android mobile application designed to modernize campus access systems by replacing traditional **physical RFID access cards** with **smartphone-based NFC authentication**.

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
- Replaces physical campus access cards
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

# 📂 Repository Structure

```
NFCampus/
│
├── master/                    # Android application source code
│   ├── Kotlin files
│   ├── XML layouts
│   └── Android resources
│
├── Document/                  # Project documentation
│   ├── Client Meeting Minutes - NFCampus (Android).pdf
│   ├── Figma_Link.md
│   ├── Individual Contributions to the Group.pdf
│   ├── LSEP - NFCampus (Android).pdf
│   ├── Proposal - NFCampus (Android).pdf
│   ├── UML Diagram - NFCampus (Android).pdf
│   ├── Video Presentation - NFCampus (Android).pdf
│   └── Weekly Progress Presentation - NFCampus (Android)
│
├── PPTX & Video/              # Presentation materials
│   ├── Video Presentation - NFCampus (Android).pptx
│   ├── Video_Link.md
│   ├── Video_Link (Physical).md
│   └── Weekly Progress Presentation - NFCampus (Android).pptx
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
git clone https://github.com/<your-repository>/NFCampus.git
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

- **Presentation Slides:** Available in the repository  
- **Video Demonstration:** See `Video_Link.md`

---

# 🎨 UI/UX Design

The system interface was designed using **Figma**.

Figma prototype link can be found in:

```
Document/Figma_Link.md
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
