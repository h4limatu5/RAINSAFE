/**
 * RAINSAFE - ESP32 Smart Laundry System (Anti-Echo & Analog LDR Integration V4.6)
 * Board: ESP32 Dev Module
 * Library: Firebase ESP Client (oleh Mobizt)
 * PIN MOTOR: IN1 -> GPIO 32, IN2 -> GPIO 33
 * PIN SENSOR: LDR AO -> GPIO 34 (Analog Input 1 - Aman Bersama WiFi)
 */

#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

// ======================== KONFIGURASI WIFI & FIREBASE ========================
#define WIFI_SSID "HUAWEI-7277"
#define WIFI_PASSWORD "TN9ANF4QRTD"
#define DATABASE_URL "https://rainsafe-777f2-default-rtdb.asia-southeast1.firebasedatabase.app"
#define API_KEY "AIzaSyCuYyF_D5nx6fqo3-NeHs-HjDVXJcR4poM"

// ==========================================
// CONFIGURASI PIN
// ==========================================
const int pinHujan  = 13; 
const int pinCahaya = 34; // Menggunakan Analog Input (AO) di ADC1 (G, V tetap)
const int pinIN1    = 32;
const int pinIN2    = 33;
const int pinENA    = -1; 

// ==========================================
// CONFIGURASI PARAMETER SENSOR & MEKANIK
// ==========================================
const unsigned long durasiMekanik = 3700; 
// Ambang LDR (dalam estimasi lux) — sensor AO kini pada GPIO34
const int thresholdGelapLux = 1000; // Gelap jika Lux >= threshold (sore/malam/tertutup)

// ==========================================
// STATE MANAGEMENT & TIMERS
// ==========================================
bool statusJemuranDiLuar = false; 
bool autoMode = true;             

bool motorBergerak = false;
unsigned long waktuMulaiMotor = 0;
bool targetStatusDiLuar = false;
bool catatKeFirebase = false;

FirebaseData fbdoStream;
FirebaseData fbdoSet;
FirebaseAuth auth;
FirebaseConfig config;

unsigned long lastSensorPublish = 0;
const unsigned long sensorPublishInterval = 5000;

void setupWiFi();
void setupFirebase();
int hitungLux(int rawADC);
void readAndPublishSensors();
void mulaiTarikJemuran();
void mulaiKeluarkanJemuran();
void stopMotor();
void streamCallback(FirebaseStream data);
void streamTimeoutCallback(bool timeout);

void setup() {
  Serial.begin(115200);

  pinMode(pinHujan, INPUT);
  
  // Mengatur range pembacaan ADC1 ke 0 - 3.3V (Resolusi 12-bit: 0 - 4095)
  analogSetAttenuation(ADC_11db); 

  // Pastikan pin LDR (analog) dikonfigurasi sebagai input
  pinMode(pinCahaya, INPUT);

  pinMode(pinIN1, OUTPUT);
  pinMode(pinIN2, OUTPUT);
  if (pinENA != -1) pinMode(pinENA, OUTPUT);

  stopMotor();
  setupWiFi();
  setupFirebase();

  Serial.println("Mensinkronkan status awal dari Firebase...");
  if (Firebase.ready()) {
    if (Firebase.RTDB.getString(&fbdoSet, "/control/laundry_status")) {
      String posisi = fbdoSet.stringData();
      statusJemuranDiLuar = (posisi == "out");
      Serial.printf("-> Status Awal: %s\n", statusJemuranDiLuar ? "DI LUAR" : "DI DALAM");
    }
    if (Firebase.RTDB.getBool(&fbdoSet, "/control/auto_mode")) {
      autoMode = fbdoSet.boolData();
      Serial.printf("-> Mode Awal: %s\n", autoMode ? "OTOMATIS" : "MANUAL");
    }
  }

  Serial.println("=================================");
  Serial.println("  RAINSAFE V4.6: PULL-DOWN FIXED ");
  Serial.println("=================================");
}

void loop() {
  if (Firebase.ready()) {
    Firebase.RTDB.readStream(&fbdoStream);

    if (millis() - lastSensorPublish >= sensorPublishInterval) {
      lastSensorPublish = millis();
      readAndPublishSensors();
    }
  }

  // ====================================================================
  // KONTROL MANUAL & OTOMATIS VIA SERIAL MONITOR
  // ====================================================================
  if (Serial.available()) {
    String cmd = Serial.readString(); 
    cmd.trim();                       
    
    Serial.printf("\n[DIAGNOSTIK] Serial menerima ketikan: '%s'\n", cmd.c_str());

    if (cmd.equalsIgnoreCase("in")) {
      Serial.println("[DIAGNOSTIK] -> OK! Memaksa motor MENARIK MASUK...");
      autoMode = false; 
      Firebase.RTDB.setBool(&fbdoSet, "/control/auto_mode", false);
      mulaiTarikJemuran();
    } 
    else if (cmd.equalsIgnoreCase("out")) {
      Serial.println("[DIAGNOSTIK] -> OK! Memaksa motor MENGELUARKAN KELUAR...");
      autoMode = false; 
      Firebase.RTDB.setBool(&fbdoSet, "/control/auto_mode", false);
      mulaiKeluarkanJemuran();
    }
    else if (cmd.equalsIgnoreCase("stop")) {
      Serial.println("[DIAGNOSTIK] -> OK! Memaksa MOTOR BERHENTI...");
      stopMotor();
    }
    else if (cmd.equalsIgnoreCase("auto")) {
      Serial.println("[DIAGNOSTIK] -> OK! Memaktifkan MODE OTOMATIS...");
      autoMode = true; 
      Firebase.RTDB.setBool(&fbdoSet, "/control/auto_mode", true);
    }
    else {
      Serial.println("[DIAGNOSTIK] -> Perintah tidak dikenal.");
    }
  }

  // ====================================================================
  // KONTROL MOTOR NON-BLOCKING
  // ====================================================================
  if (motorBergerak) {
    if (millis() - waktuMulaiMotor >= durasiMekanik) {
      stopMotor();
      statusJemuranDiLuar = targetStatusDiLuar;
      Serial.printf("--> [MOTOR] Selesai bergerak. Posisi saat ini: %s\n", 
                    statusJemuranDiLuar ? "DI LUAR" : "DI DALAM");
      
      if (catatKeFirebase) {
        catatKeFirebase = false;
        Firebase.RTDB.setString(&fbdoSet, "/control/laundry_status", statusJemuranDiLuar ? "out" : "in");
        Firebase.RTDB.setString(&fbdoSet, "/control/last_command_by", autoMode ? "auto" : "manual");
      }
    }
  }

  // ====================================================================
  // LOGIKA KEPUTUSAN CUACA & EMERGENCY OVERRIDE (ANALOG VALUE)
  // ====================================================================
  if (!motorBergerak) {
    int kondisiHujan = digitalRead(pinHujan);         // LOW = Hujan, HIGH = Kering
    int nilaiMentahLDR = analogRead(pinCahaya);       // Baca nilai ADC (0 - 4095)
    int nilaiLux = hitungLux(nilaiMentahLDR);         // Konversi ke perkiraan Lux

    // LOGIKA SEARAH: Semakin GELAP, nilai Lux semakin BESAR (Maksimal 1500)
    bool isGelap = (nilaiLux >= thresholdGelapLux);

    // EMERGENCY OVERRIDE: Jika MANUAL, jemuran di LUAR, dan HUJAN -> Paksa ubah ke MODE OTOMATIS
    if (!autoMode && statusJemuranDiLuar && kondisiHujan == LOW) {
      autoMode = true;
      Firebase.RTDB.setBool(&fbdoSet, "/control/auto_mode", true);
      Serial.println("\n[EMERGENCY] Mode MANUAL dibatalkan! Jemuran di luar & Hujan detected. Berpindah ke OTOMATIS...");
    }

    // Eksekusi Logika Otomatisasi Cuaca
    if (autoMode) {
      // KONDISI A: Hujan ATAU Gelap (Lux >= 1000) -> Jemuran masuk
      if (kondisiHujan == LOW || isGelap) {
        if (statusJemuranDiLuar) {
          Serial.printf("LOGIKA AUTO: Terdeteksi Hujan/Gelap! (Cahaya: %d lux)\n", nilaiLux);
          mulaiTarikJemuran();
        }
      } 
      // KONDISI B: Kering DAN Terang (Lux < 1000) -> Jemuran keluar
      else if (kondisiHujan == HIGH && !isGelap) {
        if (!statusJemuranDiLuar) {
          Serial.printf("LOGIKA AUTO: Terdeteksi Cuaca Cerah! (Cahaya: %d lux)\n", nilaiLux);
          mulaiKeluarkanJemuran();
        }
      }
    }
  }
}

void setupWiFi() {
  Serial.println("Menghubungkan ke WiFi...");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi terhubung!");
}

void setupFirebase() {
  Serial.println("Menghubungkan ke Firebase...");
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  auth.user.email = "admin@rainsafe.com";
  auth.user.password = "123456789";

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  if (!Firebase.RTDB.beginStream(&fbdoStream, "/control")) {
    Serial.printf("Gagal memulai Stream Firebase: %s\n", fbdoStream.errorReason().c_str());
  } else {
    Serial.println("Stream Firebase berhasil diinisialisasi.");
    Firebase.RTDB.setStreamCallback(&fbdoStream, streamCallback, streamTimeoutCallback);
  }
}

/**
 * Fungsi konversi nilai ADC Mentah ke representasi LUX (Konfigurasi Pull-Down murni)
 * Searah: ADC 0 (Terang) -> 0 Lux, ADC 4095 (Gelap Total) -> 1500 Lux
 */
int hitungLux(int rawADC) {
  int luxEstimate = map(rawADC, 0, 4095, 0, 1500);
  
  // Proteksi batasan nilai agar tetap dalam range 0 - 1500
  if (luxEstimate < 0) luxEstimate = 0;
  if (luxEstimate > 1500) luxEstimate = 1500;
  
  return luxEstimate;
}

void readAndPublishSensors() {
  int kondisiHujan = digitalRead(pinHujan);
  int nilaiMentahLDR = analogRead(pinCahaya);
  int nilaiLux = hitungLux(nilaiMentahLDR);

  int rainValue = (kondisiHujan == LOW) ? 100 : 0;
  String rainStatus = (kondisiHujan == LOW) ? "Hujan" : "Aman";
  String lightStatus = (nilaiLux >= thresholdGelapLux) ? "Gelap" : "Terang";

  FirebaseJson jsonRain, jsonLight;
  jsonRain.set("value", String(rainValue));
  jsonRain.set("unit", "%");
  jsonRain.set("status", rainStatus);
  jsonRain.set("time", "Real-time");
  Firebase.RTDB.setJSON(&fbdoSet, "/sensors/sensor_hujan", &jsonRain);

  jsonLight.set("value", String(nilaiLux)); 
  jsonLight.set("unit", "lux");
  jsonLight.set("status", lightStatus);
  jsonLight.set("time", "Real-time");
  Firebase.RTDB.setJSON(&fbdoSet, "/sensors/sensor_cahaya", &jsonLight);

  Serial.printf("[SENSOR] Hujan: %s | Cahaya: %d lux (%s) | Posisi: %s | Mode: %s\n", 
                rainStatus.c_str(), nilaiLux, lightStatus.c_str(),
                statusJemuranDiLuar ? "DI LUAR" : "DI DALAM",
                autoMode ? "OTOMATIS" : "MANUAL");
}

void mulaiTarikJemuran() {
  if (motorBergerak) return;
  Serial.println("--> [AKSI MOTOR] Menarik jemuran masuk...");
  digitalWrite(pinIN1, HIGH);
  digitalWrite(pinIN2, LOW);
  motorBergerak = true;
  waktuMulaiMotor = millis();
  targetStatusDiLuar = false;
  catatKeFirebase = true;
}

void mulaiKeluarkanJemuran() {
  if (motorBergerak) return;
  Serial.println("--> [AKSI MOTOR] Mengeluarkan jemuran...");
  digitalWrite(pinIN1, LOW);
  digitalWrite(pinIN2, HIGH);
  motorBergerak = true;
  waktuMulaiMotor = millis();
  targetStatusDiLuar = true;
  catatKeFirebase = true;
}

void stopMotor() {
  digitalWrite(pinIN1, LOW);
  digitalWrite(pinIN2, LOW);
  motorBergerak = false;
}

void streamCallback(FirebaseStream data) {
  String path = data.dataPath();
  String dataType = data.dataType();
  
  Serial.printf("\n[STREAM] Masuk -> Path: %s | Tipe: %s\n", path.c_str(), dataType.c_str());

  if (path == "/auto_mode") {
    if (dataType == "boolean") {
      autoMode = data.boolData();
      Serial.printf("   -> Mode Otomatis Berubah: %s\n", autoMode ? "AKTIF" : "NONAKTIF");
    }
  }
  
  else if (path == "/laundry_status" && !motorBergerak) {
    if (dataType == "string") {
      String newStatus = data.stringData();
      Serial.printf("   -> Perintah Tombol Aplikasi: %s\n", newStatus.c_str());
      
      bool adaPerubahanAsli = (newStatus == "in" && statusJemuranDiLuar) || (newStatus == "out" && !statusJemuranDiLuar);

      if (adaPerubahanAsli) {
        if (autoMode) {
          autoMode = false;
          Firebase.RTDB.setBool(&fbdoSet, "/control/auto_mode", false);
          Serial.println("   -> [OVERRIDE] Auto Mode dimatikan karena user menekan kontrol manual.");
        }

        if (newStatus == "in" && statusJemuranDiLuar) {
          mulaiTarikJemuran();
        } else if (newStatus == "out" && !statusJemuranDiLuar) {
          mulaiKeluarkanJemuran();
        }
      } else {
        Serial.println("   -> [INFO] Data diabaikan karena merupakan gema/laporan balik dari ESP32.");
      }
    }
  }
  
  else if (path == "/") {
    FirebaseJson *json = data.to<FirebaseJson *>();
    FirebaseJsonData jsonData;
    
    json->get(jsonData, "auto_mode");
    if (jsonData.success) {
      autoMode = jsonData.boolValue;
      Serial.printf("   -> [JSON Induk] Auto Mode: %s\n", autoMode ? "AKTIF" : "NONAKTIF");
    }

    json->get(jsonData, "laundry_status");
    if (jsonData.success) {
      String newStatus = jsonData.stringValue;
      Serial.printf("   -> [JSON Induk] Perintah Tombol: %s\n", newStatus.c_str());
      
      if (!motorBergerak) {
        bool adaPerubahanAsli = (newStatus == "in" && statusJemuranDiLuar) || (newStatus == "out" && !statusJemuranDiLuar);

        if (adaPerubahanAsli) {
          if (autoMode) {
            autoMode = false;
            Firebase.RTDB.setBool(&fbdoSet, "/control/auto_mode", false);
          }

          if (newStatus == "in" && statusJemuranDiLuar) {
            mulaiTarikJemuran();
          } else if (newStatus == "out" && !statusJemuranDiLuar) {
            mulaiKeluarkanJemuran();
          }
        } else {
          Serial.println("   -> [INFO] Data JSON Induk diabaikan (Gema detected).");
        }
      }
    }
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) Serial.println("Firebase Stream Timeout. Reconnecting...");
}