#include <SPI.h>
#include <Adafruit_PN532.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// ============================================================================
// CONFIGURATION
// ============================================================================

// SPI Pins for ESP32
#define PN532_SCK  18
#define PN532_MOSI 23
#define PN532_MISO 19
#define PN532_SS   5

// LCD Configuration
#define I2C_SDA     21
#define I2C_SCL     22
#define LCD_ADDRESS 0x27
#define LCD_COLUMNS 16
#define LCD_ROWS    2

// AID for HCE Service (10 bytes)
static const uint8_t HCE_AID[] = {
  0xA0, 0x00, 0x00, 0x00, 0x62, 0x03, 0x01, 0x0C, 0x06, 0x01
};
#define AID_LENGTH 10

// Authorized UIDs
static const char* AUTHORIZED_CARDS[] = {
  "04A3B2C1", // Demo UID
  "06805A99",  // Jin's Physical Card UID
  "76BA1399", // Yi Shan's Physical Card UID
  "A02765BF"  // Client Physical Card UID
};
#define NUM_AUTHORIZED_CARDS (sizeof(AUTHORIZED_CARDS) / sizeof(AUTHORIZED_CARDS[0]))

// ============================================================================
// GLOBAL OBJECTS
// ============================================================================

Adafruit_PN532 nfc(PN532_SS);
LiquidCrystal_I2C lcd(LCD_ADDRESS, LCD_COLUMNS, LCD_ROWS);

// Enum for device type detection
enum DeviceType {
  DEVICE_PHYSICAL_CARD,
  DEVICE_HCE_PHONE,
  DEVICE_UNKNOWN
};

// ============================================================================
// DISPLAYs
// ============================================================================

void displayInitMessage() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("   NFC Reader   ");
  lcd.setCursor(0, 1);
  lcd.print("  Version 2.0   ");
  delay(2000);
}

void displayScanning() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(" Ready to Scan! ");
  lcd.setCursor(0, 1);
  lcd.print("Tap Card / Phone");
}

void displayUID(const String& uid, DeviceType deviceType) {
  lcd.clear();
  
  // Display device type on first line
  lcd.setCursor(0, 0);
  if (deviceType == DEVICE_PHYSICAL_CARD) {
    lcd.print("      UID:      ");
  } else if (deviceType == DEVICE_HCE_PHONE) {
    lcd.print("      UID:      ");
  } else {
    lcd.print("      UID:      ");
  }
  
  // Display UID on second line
  lcd.setCursor(0, 1);
  if (uid.length() > 16) {
    // If UID is too long, display first part with ellipsis
    String displayUid = uid.substring(0, 13) + "...";
    lcd.print(displayUid);
  } else {
    // Center the UID
    int padding = (16 - uid.length()) / 2;
    if (padding > 0) {
      for (int i = 0; i < padding; i++) {
        lcd.print(" ");
      }
    }
    lcd.print(uid);
  }
}

void displayAccessGranted(DeviceType deviceType) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(" ACCESS GRANTED ");
  
  lcd.setCursor(0, 1);
  if (deviceType == DEVICE_PHYSICAL_CARD) {
    lcd.print(" Welcome Card! ");
  } else if (deviceType == DEVICE_HCE_PHONE) {
    lcd.print(" Welcome Phone! ");
  } else {
    lcd.print("  Welcome!  ");
  }
}

void displayAccessDenied(const String& reason) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(" ACCESS DENIED ");
  
  lcd.setCursor(0, 1);
  if (reason.length() > 16) {
    String displayReason = reason.substring(0, 16);
    lcd.print(displayReason);
  } else {
    // Center the reason text
    int padding = (16 - reason.length()) / 2;
    if (padding > 0) {
      for (int i = 0; i < padding; i++) {
        lcd.print(" ");
      }
    }
    lcd.print(reason);
  }
}

void displayWelcomeMessage(DeviceType deviceType) {
  lcd.clear();
  
  if (deviceType == DEVICE_PHYSICAL_CARD) {
    lcd.setCursor(0, 0);
    lcd.print(" Card Detected! ");
    lcd.setCursor(0, 1);
    lcd.print(" Reading... ");
  } else if (deviceType == DEVICE_HCE_PHONE) {
    lcd.setCursor(0, 0);
    lcd.print(" Phone Detected!");
    lcd.setCursor(0, 1);
    lcd.print(" Connecting...  ");
  } else {
    lcd.setCursor(0, 0);
    lcd.print("Device Detected!");
    lcd.setCursor(0, 1);
    lcd.print(" Identifying... ");
  }
  delay(1000); // Show message briefly
}

void displayCheckingAccess() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Checking Access");
  lcd.setCursor(0, 1);
  lcd.print("Please Wait...");
  delay(2000); // Simulate checking process
}

void displayRemoveDevice(int countdown) {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(" Remove Device ");
  lcd.setCursor(0, 1);
  lcd.print(" Ready in ");
  lcd.print(countdown);
  lcd.print("s   ");
}

// ============================================================================
// AUTHORIZATION FUNCTIONS
// ============================================================================

bool isAuthorized(const String& uid) {
  String cleanUid = uid;
  cleanUid.replace(":", "");
  cleanUid.replace(" ", "");
  cleanUid.toUpperCase();
  
  Serial.print("Checking authorization for UID: ");
  Serial.println(cleanUid);
  
  for (uint8_t i = 0; i < NUM_AUTHORIZED_CARDS; i++) {
    String authCard = AUTHORIZED_CARDS[i];
    authCard.replace(":", "");
    authCard.replace(" ", "");
    authCard.toUpperCase();
    
    if (cleanUid.equalsIgnoreCase(authCard)) {
      Serial.println("UID authorized");
      return true;
    }
  }
  
  Serial.println("UID not authorized");
  return false;
}

// ============================================================================
// UID EXTRACTION FUNCTIONS
// ============================================================================

String extractUidFromHceResponse(uint8_t* response, uint8_t responseLen) {
  if (responseLen < 2) return "";
  
  // Check if response ends with 90 00 (success)
  if (response[responseLen-2] != 0x90 || response[responseLen-1] != 0x00) {
    return "";
  }
  
  // UID is all bytes except the last 2 status bytes
  String rfidUid = "";
  for (int i = 0; i < responseLen - 2; i++) {
    if (response[i] < 0x10) rfidUid += "0"; // Padding
    rfidUid += String(response[i], HEX);
  }
  rfidUid.toUpperCase();
  
  return rfidUid;
}

String getPhysicalCardUID() {
  uint8_t uid[7];
  uint8_t uidLength;
  
  // Read physical card UID
  if (nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, uid, &uidLength)) {
    String cardUid = "";
    for (uint8_t i = 0; i < uidLength; i++) {
      if (uid[i] < 0x10) cardUid += "0";
      cardUid += String(uid[i], HEX);
    }
    cardUid.toUpperCase();
    return cardUid;
  }
  
  return "";
}

// ============================================================================
// DEVICE DETECTION FUNCTIONS
// ============================================================================

DeviceType detectDeviceType(String& detectedUid) {
  // First try to read as HCE device
  uint8_t selectApdu[5 + AID_LENGTH] = { //uint8_t selectApdu[11 + AID_LENGTH] = { 
    0x00, 0xA4, 0x04, 0x00,  // SELECT command
    AID_LENGTH               // Lc: Length of AID
  };
  
  // Copy AID into APDU
  memcpy(&selectApdu[5], HCE_AID, AID_LENGTH);
  
  uint8_t response[32];
  uint8_t responseLen = sizeof(response);
  
  // Send APDU command
  if (nfc.inDataExchange(selectApdu, 5 + AID_LENGTH, response, &responseLen)) { //if (nfc.inDataExchange(selectApdu, sizeof(selectApdu), response, &responseLen)) {
    String hceUid = extractUidFromHceResponse(response, responseLen);
    if (hceUid.length() > 0) {
      detectedUid = hceUid;
      Serial.print("HCE Phone detected. UID: ");
      Serial.println(hceUid);
      return DEVICE_HCE_PHONE;
    }
  }
  
  // If not an HCE device, try reading as standard NFC card
  String physicalUid = getPhysicalCardUID();
  if (physicalUid.length() > 0) {
    detectedUid = physicalUid;
    Serial.print("Physical card detected. UID: ");
    Serial.println(physicalUid);
    return DEVICE_PHYSICAL_CARD;
  }
  
  // If get here, device type is unknown
  detectedUid = "";
  return DEVICE_UNKNOWN;
}

// ============================================================================
// SETUP FUNCTION
// ============================================================================

void setup() {
  // Initialize Serial
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("\n╔════════════════════════════════════╗");
  Serial.println("║     NFC Access Control System      ║");
  Serial.println("║     Dual Mode: Cards & Phones      ║");
  Serial.println("╚════════════════════════════════════╝\n");
  
  // Initialize I2C for LCD
  Wire.begin(I2C_SDA, I2C_SCL);
  
  // Initialize LCD
  lcd.init();
  lcd.backlight();
  
  // Show startup animation
  for (int i = 0; i < 12; i++) {
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("Starting System");
    lcd.setCursor(i, 1);
    lcd.print(">>>");
    delay(200);
  }
  
  displayInitMessage();
  
  // Initialize SPI for PN532
  SPI.begin(PN532_SCK, PN532_MISO, PN532_MOSI, PN532_SS);
  
  // Initialize NFC reader
  if (!nfc.begin()) {
    Serial.println("ERROR: PN532 not found!");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print(" ERROR: PN532 ");
    lcd.setCursor(0, 1);
    lcd.print(" Not Found! ");
    while (1) {
      delay(1000);
    }
  }
  
  // Check if PN532 is connected
  uint32_t versionData = nfc.getFirmwareVersion();
  if (!versionData) {
    Serial.println("ERROR: PN532 not responding!");
    lcd.clear();
    lcd.setCursor(0, 0);
    lcd.print("PN532 Not Found");
    lcd.setCursor(0, 1);
    lcd.print("Check Connection");
    while (1) {
      delay(1000);
    }
  }
  
  // Display firmware info
  Serial.print("Found PN5"); 
  Serial.print((versionData >> 24) & 0xFF, HEX);
  Serial.print(" v");
  Serial.print((versionData >> 16) & 0xFF, DEC);
  Serial.print('.');
  Serial.println((versionData >> 8) & 0xFF, DEC);
  
  // Configure PN532
  nfc.SAMConfig();
  nfc.setPassiveActivationRetries(0xFF);
  
  Serial.println("System Initialized Successfully");
  Serial.println("Status: Ready to scan cards or phones\n");
  
  displayScanning();
}

// ============================================================================
// MAIN LOOP
// ============================================================================

void loop() {
  // Wait for a target (Phone or Card) to enter the field
  if (nfc.inListPassiveTarget()) {
    Serial.println("\n════════════════════════════════════");
    Serial.println("          DEVICE DETECTED");
    Serial.println("════════════════════════════════════");
    
    // Detect device type and get UID
    String detectedUid = "";
    DeviceType deviceType = detectDeviceType(detectedUid);
    
    if (detectedUid.length() > 0) {
      // Show welcome message based on device type
      displayWelcomeMessage(deviceType);
      delay(500);
      
      // Display the UID
      displayUID(detectedUid, deviceType);
      delay(1500);
      
      // Show checking access message
      displayCheckingAccess();
      
      // Check authorization
      if (isAuthorized(detectedUid)) {
        Serial.println("╔════════════════════════════════════╗");
        Serial.println("║           ACCESS GRANTED           ║");
        if (deviceType == DEVICE_PHYSICAL_CARD) {
          Serial.println("║          Welcome Card User!        ║");
        } else {
          Serial.println("║         Welcome Phone User!        ║");
        }
        Serial.println("╚════════════════════════════════════╝");
        
        displayAccessGranted(deviceType);
        delay(4000);

      } else {
        Serial.println("╔════════════════════════════════════╗");
        Serial.println("║           ACCESS DENIED            ║");
        Serial.println("║        Unauthorized Device         ║");
        Serial.println("╚════════════════════════════════════╝");
        
        displayAccessDenied("Not Authorized");
        delay(4000);
      }
    } else {
      Serial.println("ERROR: Could not read device UID");
      displayAccessDenied("Read Error");
      delay(3000);
    }
    
    // Wait for device to be removed
    Serial.println("\nPlease remove device...");
    for (int i = 3; i > 0; i--) {
      displayRemoveDevice(i);
      delay(1000);
    }
    
    // Clear display and show scanning again
    Serial.println("Ready for next scan\n");
    displayScanning();
  }
  
  // Small delay before next scan
  delay(200);
}