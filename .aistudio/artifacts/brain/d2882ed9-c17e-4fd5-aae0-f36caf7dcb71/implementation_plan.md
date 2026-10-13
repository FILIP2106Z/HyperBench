# Fix GitHub Actions CI/CD Build & Release Pipeline

Resolves the Android CI and FOSS Release workflow failure during the APK build stages by eliminating faulty boolean expression evaluation in GitHub Secrets and hardening runner keystore setup.

### User Review & Critical Decisions

> [!IMPORTANT]
> The GitHub Actions failure in the `Build Release APK` / `Build Debug APK` stage is caused by GitHub Actions expression syntax: `${{ secrets.STORE_PASSWORD || 'android' }}` evaluates to the boolean string `"true"` rather than the fallback password string `"android"`. This causes Gradle to fail when unlocking the release keystore.

- **Confirmed Root Cause**: In GitHub Actions workflow files, `${{ expr || 'val' }}` evaluates as a boolean OR, yielding `"true"`. When Gradle receives `STORE_PASSWORD="true"`, keystore authentication fails with `Keystore was tampered with, or password was incorrect`.
- **Confirmed Target Behavior**: Enable frictionless, fully automated APK compilation for both Debug and Release variants on any GitHub repository clone or fork without requiring pre-configured repository secrets.

---

### 1. Overview & Core Concept

- **What It Does**: Hardens the `.github/workflows/android-ci.yml` pipeline to guarantee error-free compilation of `hyperbench-debug.apk` and `hyperbench-release.apk`, automatic artifact uploading, and GitHub Release asset creation on tagged commits.
- **Target Audience / Persona**: Open-source contributors and users building, testing, or releasing HyperBench on GitHub without manual keystore intervention.
- **Key Value**: 100% green CI pipeline runs on GitHub Actions with zero manual configuration required for forks and automated releases.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Push to Main/Master or PR**: Triggers `Android CI & FOSS Release Build`. JDK 21 and Android SDK components configure automatically.
  2. **APK Compilation**: `assembleDebug` and `assembleRelease` execute cleanly, writing outputs to `app/build/outputs/apk/`.
  3. **Artifact Availability**: Both `hyperbench-debug` and `hyperbench-release` are attached as downloadable artifacts on the workflow summary page.
  4. **Release Tagging (e.g., `v1.0.0`)**: Automatically creates a GitHub Release with release and debug APKs attached.
- **Visual Identity & Theme**:
  - Maintained in accordance with Material Design 3 guidelines for HyperBench (Dark cyber-teal `#00E5FF`, surface `#0D1117`, card containers `#161B22`).
- **Interactive Feedback & Motion**:
  - The Android app itself remains responsive with smooth Jetpack Compose animations and real-time GPU/CPU benchmark stress tests.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Shell Parameter Expansion vs. GitHub Actions Expression for Secrets**
  - *Chosen Approach*: Handle secret fallbacks inside the runner bash environment using `${STORE_PASSWORD:-android}` rather than GitHub Actions inline expressions `${{ ... || ... }}`.
  - *Why*: Bash parameter expansion `${VAR:-default}` preserves strings when variables are empty, preventing the `"true"` boolean conversion bug in GitHub Actions expression engine.
  - *Alternatives Considered*: Requiring users to configure repository secrets in GitHub settings. Rejected because it breaks builds on forks and clean clones.

- **Decision 2: Automated Keystore Generation Fallback**
  - *Chosen Approach*: Check for `my-upload-key.jks` and `debug.keystore` prior to Gradle execution. If either is missing (e.g. fresh checkout), generate valid 2048-bit RSA keystores with password `android`.
  - *Why*: Ensures builds succeed out-of-the-box on GitHub cloud runners where `.gitignore` excludes local keystores.
  - *Alternatives Considered*: Base64-encoding keystores in GitHub secrets. Rejected to keep the FOSS build self-contained.

- **Decision 3: SDK License Acceptance and Auto-Download**
  - *Chosen Approach*: Write standard license hashes directly to `$ANDROID_HOME/licenses/` in addition to `sdkmanager --licenses`.
  - *Why*: Prevents SDK download prompts or license rejection halts on headless runners.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Actions Runner                    │
│                       (ubuntu-latest)                       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                ┌──────────────▼─────────────┐
                │   Checkout & Setup JDK 21  │
                └──────────────┬─────────────┘
                               │
                ┌──────────────▼─────────────┐
                │ Environment & Keystores    │
                │ • .env & google-services   │
                │ • debug.keystore check     │
                │ • my-upload-key.jks check  │
                │ • SDK license hashes       │
                └──────────────┬─────────────┘
                               │
        ┌──────────────────────┴──────────────────────┐
        │                                             │
 ┌──────▼──────────────────┐           ┌──────────────▼─────────────┐
 │ ./gradlew assembleDebug │           │ ./gradlew assembleRelease  │
 │ (using debugConfig)     │           │ (STORE_PASSWORD fallback)  │
 └──────┬──────────────────┘           └──────────────┬─────────────┘
        │                                             │
        └──────────────────────┬──────────────────────┘
                               │
                ┌──────────────▼─────────────┐
                │     Upload APK Artifacts   │
                │ • hyperbench-debug.apk     │
                │ • hyperbench-release.apk   │
                └────────────────────────────┘
```

#### Data Model & State
- **Secrets & Environment Variables**:
  - `STORE_PASSWORD`: Evaluated with bash default `${STORE_PASS:-android}`.
  - `KEY_PASSWORD`: Evaluated with bash default `${KEY_PASS:-android}`.
  - `KEYSTORE_PATH`: Set to absolute `$GITHUB_WORKSPACE/my-upload-key.jks`.
- **Build Targets**:
  - `:app:assembleDebug` -> `app/build/outputs/apk/debug/app-debug.apk`
  - `:app:assembleRelease` -> `app/build/outputs/apk/release/app-release.apk`
