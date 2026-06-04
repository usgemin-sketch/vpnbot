# Android app

This is now a WireGuard-ready Android client for the Brawl VPN APK.

## Current status

- Custom visual identity instead of emoji.
- Kotlin + Jetpack Compose UI.
- Country-specific config storage inside the app.
- Real WireGuard tunnel integration via the official Android tunnel library.
- Connect / disconnect flow with Android VPN permission handling.
- Transfer and handshake status on the main screen.

## What still needs real infrastructure

To make production countries actually connect, paste real WireGuard configs into the country editor.
Each config must include valid:

- `PrivateKey`
- `Address`
- `PublicKey`
- `Endpoint`

The app side is now ready for those configs.
