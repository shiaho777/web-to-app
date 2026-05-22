# Release Guide for WebToApp

This guide provides the exact values to use when creating a release on GitHub.

## GitHub Release Fields

| Field | Value |
| --- | --- |
| **Tag version** | `v1.9.7-yasin-final` (Select the existing tag) |
| **Target branch** | `main` or `yasin-final-contribution` |
| **Release title** | `WebToApp v1.9.7: Enhanced NativeBridge & Cloud Build` |
| **Description** | (See "Release Description" section below) |
| **Binaries/Assets** | Upload `app/build/outputs/apk/release/app-release.apk` and optionally `app/build/outputs/bundle/release/app-release.aab` |

## Release Description

Copy and paste the following into the "Describe this release" field:

```markdown
## 🚀 Major Enhancements by Yasin Ullah

This release introduces critical infrastructure and API improvements to the WebToApp ecosystem.

### 🛠️ New NativeBridge APIs (56 Methods Verified)
- **Security Suite:** On-device detection of Developer Options, ADB, and Debug status.
- **Notification Engine:** Full control over immediate and scheduled notifications with custom channels and sound support.
- **Runtime & Lifecycle:** Background workers (WorkManager), foreground services, and app state monitoring.

### ☁️ CI/CD & Cloud Build
- **GitHub Actions Integration:** Build and sign production-ready APKs directly in the cloud without a local Android SDK.
- **Artifact Management:** Automated packaging and uploading of debug/release builds.

### 📚 Documentation
- Added a comprehensive **Release Guide**.
- Updated **NativeBridge Documentation** for developers.
- Verified validation reports included in the source.

---
**Build Note:** These artifacts were built using the new GitHub Actions pipeline and verified on physical hardware.
```

## How to Publish
1. Go to [New Release](https://github.com/yasinULLAH/web-to-app/releases/new).
2. Select the tag **v1.9.7-yasin-final**.
3. Fill in the Title and Description as provided above.
4. Drag and drop the APK (and AAB if needed) from your local path.
5. Click **Publish release**.
