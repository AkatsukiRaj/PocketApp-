# Pocket (பாக்கெட்) - Personal Digital Vault & Organizer for Android

A modern, senior-friendly Android application designed for storing important photos, medical prescriptions, documents (PDF, Excel, Word), smart notes, reminders with alarms, home screen shortcuts, and two-way sharing with WhatsApp and other apps.

---

## 🌟 Key Features

1. **📸 Photos & Album Vault**:
   - Organized storage for prescriptions, medical reports, bills, receipts, and personal photos.
   - Built-in categorization (`Medical`, `Bills`, `ID Proofs`, `Personal`, `General`).

2. **📄 Documents Vault (PDF & Excel)**:
   - High-security local storage for Aadhaar, PAN card, land deeds, bank statements, and spreadsheets (`.xlsx`, `.xls`, `.pdf`, `.docx`).
   - One-tap view and export.

3. **📌 Front Screen Shortcuts & Glance AppWidget**:
   - Place a responsive widget on the phone's front home screen for 1-tap instant access to frequently needed documents or today's medicines.

4. **📝 Smart Notes**:
   - Clean, high-legibility notes for grocery lists, doctor's recommendations, and daily thoughts.

5. **⏰ Reminders & Alarms (நினைவூட்டல்)**:
   - Exact alarm scheduling using Android `AlarmManager`.
   - Loud notification alerts with custom sound and vibration pattern so elderly users never miss medicine timings or bill deadlines.

6. **🔄 Two-Way Sharing (WhatsApp Integration)**:
   - **Pocket ➔ WhatsApp/Others**: 1-click share via secure `FileProvider`.
   - **WhatsApp/Others ➔ Pocket**: When a document or photo is received in WhatsApp or viewed in Gallery, tap "Share", choose **Pocket**, and it saves directly to the designated category!

7. **👴 Senior-Friendly Modern UI**:
   - Big touch targets (minimum 54dp).
   - High-contrast color palette (Indigo, Amber, Emerald, Slate).
   - Bilingual Tamil & English labels for zero confusion.

---

## 🚀 How to Open and Run in Android Studio

1. Open **Android Studio** (Ladybug / Iguana or later).
2. Click **Open** and select the folder:
   ```
   C:\Users\Admin\.gemini\antigravity\scratch\PocketApp
   ```
3. Android Studio will automatically sync Gradle dependencies using Gradle 8.10.2.
4. Connect an Android phone (or start an emulator running Android 8.0 / API 26 or higher).
5. Click the green **Run (▶)** button.

---

## 📁 Architecture Overview

- **UI Layer**: Jetpack Compose + Material 3 + Jetpack Glance (for Home Widget)
- **Architecture**: MVVM with Kotlin Coroutines & Flow
- **Local Database**: Room Database with TypeConverters
- **Storage**: App-internal secure scoped storage (`context.filesDir/vault/`)
- **Inter-App Sharing**: `androidx.core.content.FileProvider` + `Intent.ACTION_SEND`
- **Alarm System**: `AlarmManager` with `setExactAndAllowWhileIdle` + `NotificationChannel`
