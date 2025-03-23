# AuthenticatorLib

A lightweight Java library for generating TOTP (Time-based One-Time Password) codes, compatible with Google Authenticator and other standard OTP apps. Includes tools for Base32 secret generation, OTP code generation, and support for otpauth:// URIs (used in QR code setup).

[![](https://jitpack.io/v/oyin25/AuthenticatorLib.svg)](https://jitpack.io/#oyin25/AuthenticatorLib)

---

## Features

- Generate TOTP verification codes using HMAC-SHA1, SHA256, or SHA512
- Create secure Base32-encoded secret keys
- Generate `otpauth://` URIs for QR code setup
- Fully compatible with Google Authenticator, Microsoft Authenticator, and Authy
- Optional: QR code generation support via third-party libs (e.g., QRGen or ZXing)
- No AndroidX or external dependencies required

---

## Installation

### Step 1: Add JitPack to your project

#### If you're using `settings.gradle` (Groovy)
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### Or in `build.gradle` (legacy projects)
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the library to your dependencies

```groovy
dependencies {
    implementation 'com.github.oyin25:AuthenticatorLib:v1.2.1'
}
```

---

## Usage

### 1. Generate a Random Base32 Secret Key
```java
String secret = VerificationCodeUtil.Base32.generateRandomSecret();
```

### 2. Generate a 6-digit OTP Code (default)
```java
String code = VerificationCodeUtil.generateCode(secret);
```

### 3. Generate with custom settings (e.g., 8-digit codes, HmacSHA256)
```java
String customCode = VerificationCodeUtil.generateCode(secret, 8, 30, "HmacSHA256");
```

### 4. Generate otpauth:// URI (used for QR Code setup)
```java
String otpUri = VerificationCodeUtil.getOtpAuthUrl("your@email.com", "YourApp", secret);
// otpauth://totp/your@email.com?secret=XXXXXX&issuer=YourApp
```

You can use this URI with any QR generator library to display a scannable QR for setup in Google Authenticator.

---

## QR Code Generation (Optional)

Use a third-party library like [QRGen](https://github.com/kenglxn/QRGen) or [ZXing](https://github.com/zxing/zxing) to convert the `otpauth://` URI into a QR image:

```java
// QRGen example (for Android)
Bitmap qr = QRCode.from(otpUri).bitmap();
imageView.setImageBitmap(qr);
```

---

## Output Sample

```java
String secret = VerificationCodeUtil.Base32.generateRandomSecret();
// => "ABCD1234ABCD5678"

String code = VerificationCodeUtil.generateCode(secret);
// => "982374"

String otpUrl = VerificationCodeUtil.getOtpAuthUrl("user@domain.com", "MyApp", secret);
// => "otpauth://totp/user@domain.com?secret=ABCD1234ABCD5678&issuer=MyApp"
```

---

## Compatibility

- Google Authenticator
- Microsoft Authenticator
- Authy
- FreeOTP
- and other TOTP-compatible apps

---

## License

```
MIT License

Copyright (c) 2025 oyin25

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## Author

Made with purpose by [@oyin25](https://github.com/oyin25).  
Feel free to star the repo, fork it, or submit improvements!
