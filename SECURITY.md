# Security deployment checklist

The repository is safe-by-default for public source control, but the running kiosk also depends on Firebase console configuration and Android device-owner provisioning. Do not call a release production-secure until every deployment item below is complete.

## Accepted pre-production risks

- The admin PIN remains the publicly known value `1234`. Persistent rate limiting cannot protect a known PIN; replace it before placing a kiosk in public use.
- App Check is intentionally omitted. A manually allowlisted anonymous UID controls Firebase access, but requests are not attested as coming from an untampered APK.
- Orders and stock mutations still originate in the client. A compromised allowlisted kiosk can submit false orders or decrement stock; only a trusted backend can make these operations authoritative.
- The static InstaPay/GCash QR cannot verify settlement. `CUSTOMER_REPORTED_PAID` records only the customer's claim.

## Anonymous kiosk UID registration

1. Enable Anonymous Authentication in Firebase and launch a newly installed kiosk.
2. Copy the UID shown on the app's registration screen. The anonymous user is created automatically and also appears under Firebase Authentication users.
3. Replace or extend every `REPLACE_WITH_KIOSK_UID_*` condition in `database.rules.json` locally with the approved kiosk UIDs.
4. Run the emulator tests against the populated rules, supplying the first two configured UIDs as `KIOSK_UID_1` and `KIOSK_UID_2`.
5. Deploy the reviewed rules, return to the kiosk, and tap **Check registration**.
6. Restore the placeholder version before committing to a public repository. Remove deployed UIDs when a device is retired, lost, cleared, or reinstalled; reinstallation normally creates a different anonymous UID.

Never commit real deployment UIDs, administrator emails, or a populated private rules copy to the public repository.

## Realtime Database rules

The tracked `database.rules.json` is deny-by-default and uses placeholder UIDs. Its child-level rules provide the required behavior:

- Only allowlisted UIDs can read categories, app settings, and inventory.
- Clients cannot edit menu content or appearance settings.
- Stock can only move downward, must remain numeric, and cannot become negative.
- Orders are create-only, tied to `submittedByUid`, UUID-addressed, field-limited, and restricted to known payment method/status pairs.
- A kiosk can read only its own existing orders.
- Unknown top-level data, including `users`, is denied because this Android app does not use it.

Do not retain a parent `.write` rule on `branch2`. Realtime Database permissions cascade: a broad parent grant cannot be revoked by stricter child rules and would let every allowlisted kiosk rewrite menus, settings, logs, and arbitrary inventory data. Likewise, do not use `auth != null` for a shared `users` node unless every anonymous account is intentionally allowed to read and modify it.

Run the rules tests with JDK 21 and Node.js 20 or 22:

```bash
cd firebase-tests
npm ci --ignore-scripts
npm audit --audit-level=high
npm run test:emulator

# When database.rules.json contains real local UIDs:
KIOSK_UID_1='first-uid' KIOSK_UID_2='second-uid' npm run test:emulator
```

The suite covers unauthenticated and unregistered access, atomic order/stock updates, immutable orders, cross-kiosk reads, malformed orders, and invalid stock mutations.

## Data handling and retention

Order logs contain a customer name, kiosk UID, line items, total, payment method/status, and timestamp. Before deployment, the operator must choose and document a retention period, restrict staff access through Firebase IAM, and establish a deletion process for expired records and customer requests. Do not include order/customer data in diagnostics or GitHub issues.

Firebase Analytics and Crashlytics are not bundled. Application logs are enabled only in debuggable builds, and release shrinking removes Timber calls.

## Device and network deployment

- Provision the app as device owner on a reset/unprovisioned device and confirm release ordering is blocked before provisioning.
- Confirm lock task, persistent home, status bar/keyguard controls, overlay blocking, system-error-dialog blocking, safe-boot/factory-reset restrictions, and authenticated restriction removal.
- Confirm `FLAG_SECURE`, disabled backups, a non-exported boot receiver, and HTTPS-only traffic in the merged release manifest.
- Remote images must use HTTPS and belong to the exact Firebase Storage bucket from `google-services.json`; all rejected or missing URLs render a bundled local placeholder.
- Do not distribute debug APKs because debug builds trust user-installed certificate authorities and permit non-device-owner development mode.

## Credentials and GitHub release

- Revoke every PayMongo key that was ever stored locally or included in an old APK. Source deletion is not revocation.
- Keep `google-services.json`, `local.properties`, `keystore.properties`, and signing keystores ignored and outside the repository.
- Restrict the release keystore, retain an offline backup, and verify the bundled merchant QR before each release.
- Run `bash scripts/security_scan.sh`, unit tests, lint, Firebase rules tests, and a minified release build before pushing.
- Inspect the final APK and merged manifest; confirm there are no PayMongo credentials/endpoints, telemetry SDKs, unexpected exported components, or third-party placeholder hosts.

Firebase guidance: [Realtime Database Security Rules](https://firebase.google.com/docs/database/security) and [anonymous authentication](https://firebase.google.com/docs/auth/android/anonymous-auth). App Check remains recommended for a future production-hardening release: [Play Integrity provider](https://firebase.google.com/docs/app-check/android/play-integrity-provider).
