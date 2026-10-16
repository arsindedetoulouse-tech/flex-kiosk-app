# Device Owner provisioning (development)

Provisioning the application as Device Owner is a **development and testing procedure**. It is not a
production enrollment flow.

The application exposes no UI for this. Until a later OpenSpec change adds one, provisioning is done
over ADB against a resettable test device.

## Prerequisites

Android only allows `Device Owner` to be assigned to a device that satisfies all of the following.
If any condition fails, `dpm set-device-owner` rejects the request and the application stays
`NotProvisioned`.

- The device is **factory reset**, or has no accounts, no users other than the primary user, and no
  existing device owner.
- The device is connected over ADB with USB debugging authorized.
- The application is installed **after** the last factory reset. An app already on the device before
  provisioning is rejected with `Not allowed to set the device owner because there are already several
  users on the device` or a `FAILED_*` code.
- The device is not enrolled in another EMM/MDM solution.

Use an emulator or a dedicated test handset that you can erase. Do not attempt this on a personal
device: `dpm set-device-owner` cannot be undone without another factory reset.

## Values used below

Take these from the build rather than assuming them:

| Value | Where |
|---|---|
| Application id | `applicationId` in `app/build.gradle.kts` (inherited from the `android.application` convention plugin) |
| Receiver | `com.arsinde.flexkioskapp.core.devicepolicy.admin.KioskDeviceAdminReceiver` |

To read the real application id from the built APK:

```bash
aapt dump badging app/build/outputs/apk/debug/app-debug.apk | grep package
```

## 1. Build and install

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

On a fresh device this is the first and only installation, so the app is a provisioning candidate.

## 2. Assign Device Owner

The `--user current` flag restricts the assignment to the primary user:

```bash
adb shell dpm set-device-owner \
  com.arsinde.flexkioskapp/.core.devicepolicy.admin.KioskDeviceAdminReceiver
```

A successful command prints `Success. Device owner set to package name 'com.arsinde.flexkioskapp'`.
Any other output is a failure — record it rather than assuming success.

## 3. Verify

Confirm with the platform:

```bash
adb shell dumpsys device_policy | grep -i "device owner"
```

The output must name `com.arsinde.flexkioskapp` as the device owner.

Confirm with the application. `DeviceOwnerStatusProvider.getStatus()` returns:

| Situation | Result |
|---|---|
| App installed normally, never provisioned | `NotProvisioned` |
| `dpm set-device-owner` succeeded | `DeviceOwner` |
| The framework could not answer (for example `SecurityException`) | `Error(cause)` |

`Error` is never treated as success. There is no on-screen diagnostic yet, so this status is verified
by the unit tests in `core:devicepolicy` and by `dumpsys device_policy` during development.

## 4. Verify an ordinary installation

On a different, unprovisioned device with the same APK, `dumpsys device_policy` must **not** list the
application as device owner, and the Home Screen must keep working. An unprovisioned installation is
a supported state, not a failure.

## Removing the provisioning

There is no supported way to release Device Owner from the application, and the shell cannot do it
either. Verified on an API 37 emulator:

```bash
adb shell dpm remove-active-admin --user 0 <receiver>   # SecurityException: non-test admin
adb uninstall <applicationId>                           # DELETE_FAILED_DEVICE_POLICY_MANAGER
```

`pm clear` does not release it. Only a factory reset clears it: device settings,
`adb shell recovery --wipe_data`, or relaunching the emulator with `-wipe-data`.

Plan for this up front, because it is inconvenient to discover afterwards:

```bash
adb -s emulator-5554 emu avd snapshot save kiosk-provisioned   # preserve the state
# ... verification ...
adb -s emulator-5554 emu avd snapshot load kiosk-provisioned   # restore it
```

### A provisioned app is protected

While the application is Device Owner, Android treats its package as protected and blocks operations
a normal development workflow needs:

| Operation | Result |
|---|---|
| `am force-stop` | Ignored: `Ignoring request to force stop protected package` |
| `am kill` while the app is foreground | No effect |
| `adb install -r` | Works; installs over the running instance |

Android Studio fails to deploy with `Couldn't terminate previous instance of app`, because it must
terminate the previous instance and cannot. Use `./gradlew :app:installDebug` instead. If Studio is
required, send the app to the background first; `am kill` then succeeds.

## Troubleshooting

| Symptom | Cause |
|---|---|
| `Not allowed to set the device owner because there are already several users` | Account or secondary user present on the device; wipe it |
| `Trying to set the device owner, but device owner is already set` | Another DPC is provisioned, or a previous attempt partially succeeded; factory reset |
| `Cannot set the device owner to a non-test application` | The APK is not debuggable / not marked as a test build; use the debug variant |
| `java.lang.IllegalStateException: Trying to set the device owner, but the application is already a device owner` | Already provisioned; `dumpsys device_policy` confirms the current state |
| Command succeeds but the app reports `NotProvisioned` | The receiver component name is wrong; verify it against the merged manifest, not the source |

## Limitations

- This flow is ADB-only and manual. There is no QR or zero-touch enrollment; that is out of scope for
  `add-custom-dpc` and belongs to a later change.
- The flow requires a resettable device, so it cannot be used for a staged deployment.
- Provisioning state is queried at runtime and is never cached, so a re-provision or a wipe is picked
  up on the next query without restarting the application.
