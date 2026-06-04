# Brawl VPN

MVP for selling access to a Telegram private channel with an Android APK inside.

## Project layout

- `apps/bot-api` - Telegram bot and backend for Telegram Stars payments.
- `apps/android-app` - Android APK source for the Brawl VPN client shell.
- `design/svg` - SVG source assets used instead of emoji in the product UI.

## MVP flow

1. User opens the Telegram bot.
2. User taps `Купить VPN навсегда`.
3. Bot sends an invoice for `100` Telegram Stars.
4. After successful payment, the bot creates a one-time invite link to a private channel.
5. User joins the private channel and downloads the APK.

## Important note

The Android app in this repository is a UI-first shell for the future VPN client. It does not yet establish a real VPN tunnel. The next step is connecting it to a real WireGuard or V2Ray/Xray backend that you control.

## Bot setup

Create `apps/bot-api/.env` from `.env.example` and fill in:

- `BOT_TOKEN`
- `BOT_USERNAME`
- `PRIVATE_CHANNEL_ID`
- `WEBHOOK_SECRET`
- `WEBHOOK_URL` or leave empty to use polling locally

## Railway deploy

Deploy `apps/bot-api` as the Railway service root.

## Android build

Open `apps/android-app` in Android Studio and let it sync Gradle. Then build an APK from Android Studio.
