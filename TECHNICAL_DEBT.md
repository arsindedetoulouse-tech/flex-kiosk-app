# Technical debt and open questions

Last updated: 2026-10-16, after archiving `add-custom-dpc`.

Tracked deliberately: `docs/` is ignored by `.gitignore`, so this file is the durable record of what
is known to be unfinished, what was verified only partially, and what still needs a decision.

## 1. Device Owner makes the app uninstallable and un-stoppable

**Status:** known platform behaviour, working around it, no code change needed.

Once the application is provisioned as Device Owner, Android treats its package as *protected* and
blocks the operations a normal development workflow depends on. Verified on the Pixel_9a emulator
(API 37):

| Operation | Result |
|---|---|
| `am force-stop` | Ignored: `Ignoring request to force stop protected package` |
| `am kill` while foreground | No effect |
| `adb uninstall` | `DELETE_FAILED_DEVICE_POLICY_MANAGER` |
| `dpm remove-active-admin` | `SecurityException: Attempt to remove non-test admin` |

Consequences:

- Android Studio's deploy step fails with `Couldn't terminate previous instance of app`, because it
  must terminate the previous instance and cannot.
- The provisioned state cannot be undone from the shell. Only a data wipe removes it.

### Resolution in use

1. Deploy with `./gradlew :app:installDebug`, which installs over a running instance and was verified
   working every time.
2. If Android Studio must be used, send the app to the background first (`HOME` or `Back`); `am kill`
   then succeeds and Studio completes its procedure.
3. Keep provisioning work on a dedicated AVD and wipe it afterwards
   (`emulator -wipe-data <avd>`). A provisioned snapshot can be preserved and restored with
   `adb emu avd snapshot save <name>` / `adb emu avd snapshot load <name>`.
4. Record in `openspec/changes/add-custom-dpc/provisioning.md` that neither uninstall nor
   `remove-active-admin` is a way back, so nobody loses time rediscovering it.

The application itself needs no change: this is platform policy, and the app already treats an
unprovisioned installation as a supported state.

## 2. Verification gaps

| Gap | Why | Where covered instead |
|---|---|---|
| Card in the not-provisioned state was never seen on a device | The emulator could not be returned to that state without a wipe (see section 1) | `ProvisioningStatusMappingTest`, and the unprovisioned state was observed on the physical device before the card existed |
| The undeterminable state cannot be produced on a healthy device | It requires `DevicePolicyManager` to fail, which a working system does not do | `ProvisioningStatusMappingTest`, `HomeProvisioningReadTest` |
| Device Owner behaviour on Android 16 / vendor hardware is unverified | The physical device (A015, API 36) carries two accounts, which Android rejects for provisioning. Wiping it was out of scope for this change | Provisioning was verified on the emulator only |

## 3. Build and test debt

**Incremental Kotlin compilation in `feature:home` can reuse stale outputs after a failed
compilation.** This produced two rounds of plausible but wrong test results before it was identified;
the symptom was failure messages referencing source lines that did not exist. Run
`./gradlew :feature:home:clean` before trusting a surprising result. Not yet investigated beyond that.

**Orbit's container exposes a conflated `StateFlow`.** How many intermediate states survive is
scheduling-dependent, so asserting an exact emission sequence in `testWithInternalState` is flaky.
Tests here assert the settled state and read counts instead. Worth documenting as a convention before
more ViewModel tests are written.

## 4. Localization

`feature:home` now has `values-ru` and `values-uz`, added together with the status card because
`AGENTS.md` requires translations for new user-visible text. Two consequences:

- Those translations, including the four pre-existing Home strings, have **not** been reviewed by a
  native speaker. The gate is open in `fix-app-localization`.
- The application module still uses `values-b+ru+RU` / `values-b+uz+Latn+UZ`, which Android only
  honours from API 33, and `android:localeConfig` still produces an `UnusedAttribute` lint finding.
  Both remain open in `fix-app-localization`.

## 5. Smaller known gaps

- No `@Preview` exists anywhere in the project and `compose-ui-tooling` is not in the version
  catalog, so the new status card is stateless but not previewable. Adding tooling is a project-wide
  decision, not a feature-local one.
- The project has no integration test source set, so nothing runs against a real
  `DevicePolicyManager`. The device checks performed by hand are the only coverage of that seam.
- `HomeScreen` is documented as having no "empty" state and the data layer is still a shipped stub.
  This is intentional for now, not a defect.

## 6. Open questions needing a decision

1. **Test-device policy.** Should provisioning tests use a second dedicated AVD, or the single
   developer AVD plus a wipe after each session? The second option costs a few minutes per session and
   makes section 1 a non-issue; the first needs a documented AVD and a script.
2. **Does the team need physical-hardware verification before the kiosk changes start?** Nothing so
   far has run on vendor Android 16 hardware as a device owner. If yes, a wipeable test handset has to
   be provisioned and a reset procedure agreed.
3. **Should `feature:home` keep English-first wording?** The status card text is currently written for
   an operator audience in English first and then translated. Confirm this is the intended register
   before the wording is translated again as part of `fix-app-localization`.
4. **Is a per-app language picker wanted?** Excluded from `fix-app-localization`. On API levels below 33
   it needs AppCompat locale APIs the project does not depend on. Only answer this if the kiosk
   devices will need a language other than the device's own.