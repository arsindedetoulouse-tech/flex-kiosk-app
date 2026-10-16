# Design: Add Custom Device Policy Controller

## 1. Context

The application currently contains a Home Screen and does not yet have a device-management component.

This change introduces the minimum foundation for a project-owned Android Device Policy Controller (DPC). It must allow the application to be provisioned as Device Owner on a compatible, fully managed test device and expose the current provisioning state to the application.

This change does **not** implement Kiosk Mode. It does not configure Lock Task Mode, change the preferred HOME activity, apply kiosk restrictions, start the application after boot, or add remote management.

## 2. Goals

- Register a custom `DeviceAdminReceiver`.
- Add device-admin XML metadata and manifest registration.
- Isolate Device Owner status detection behind a small, testable abstraction.
- Represent provisioning outcomes explicitly.
- Keep the existing Home Screen behavior unchanged.
- Document local ADB provisioning and verification.
- Add unit tests for status mapping and error handling.

## 3. Non-goals

- Applying `DevicePolicyManager` policies beyond checking Device Owner status.
- Calling `setLockTaskPackages()`, `setLockTaskFeatures()`, or `startLockTask()`.
- Configuring a persistent preferred HOME activity.
- Adding boot receivers, automatic startup, or process recovery.
- Adding Ktor clients, backend endpoints, remote commands, or SQLite persistence.
- Adding maintenance UI or decommissioning workflow.
- Implementing production QR provisioning.

## 4. Proposed structure

Keep the existing application namespace `com.arsinde.flexkioskapp`; do not introduce a new namespace for this feature.

The device-management code is Android platform infrastructure. It does not belong to `app` (application wiring and navigation), to a `feature` module, or to `common:data` (which holds remote/local data sources for application data). Introduce a dedicated `core:devicepolicy` module so that later changes (`add-kiosk-policy-management`, `add-lock-task-runtime`, `add-kiosk-home-launch`) can depend on the DPC foundation without depending on `app`.

```text
core/devicepolicy/
├── build.gradle.kts                      # android.library + android.koin + android.library.test
├── src/main/
│   ├── AndroidManifest.xml               # declares the admin receiver (merged into the app APK)
│   ├── kotlin/com/arsinde/flexkioskapp/core/devicepolicy/
│   │   ├── DeviceOwnerStatus.kt          # sealed status model, no Android types
│   │   ├── DeviceOwnerStatusProvider.kt  # interface, no Android types
│   │   ├── DeviceOwnerGateway.kt         # internal seam over DevicePolicyManager
│   │   ├── AndroidDeviceOwnerStatusProvider.kt  # status mapping + Context factory + Android gateway
│   │   ├── DevicePolicyModule.kt         # Koin module for this module
│   │   └── admin/
│   │       └── KioskDeviceAdminReceiver.kt
│   └── res/
│       ├── xml/device_admin.xml          # empty <uses-policies/>
│       └── values{,-ru,-uz}/strings.xml  # label + description of the admin component
└── src/test/kotlin/com/arsinde/flexkioskapp/core/devicepolicy/
    ├── AndroidDeviceOwnerStatusProviderTest.kt  # status mapping with a fake gateway
    └── DevicePolicyModuleTest.kt                # resolution through the real Koin graph

app/src/main/kotlin/com/arsinde/flexkioskapp/App.kt   # registers devicePolicyModule
settings.gradle.kts                                  # registers :core:devicepolicy
openspec/changes/add-custom-dpc/provisioning.md      # ADB provisioning procedure
```

Layering consequences:

- `DeviceOwnerStatus` and `DeviceOwnerStatusProvider` MUST NOT import Android types, so they are usable and testable from `domain`/`presentation` without framework dependencies.
- `AndroidDeviceOwnerGateway` is the only production class that touches `DevicePolicyManager`. It holds the application `Context`; no caller receives it.
- The receiver lives in `admin/` subpackage to keep framework lifecycle callbacks physically separate from status logic.
- The module MUST be a plain Android library using the existing `android.library` convention plugin so it stays off the classpath of anything that does not need device management.
- Manifest merging: declare the receiver in the library manifest fragment and let Gradle merge it into `app`. The app-level manifest needs no change, and no `<uses-permission>` entry is required because `BIND_DEVICE_ADMIN` is a signature-level permission held by the system.

Keep the receiver as a thin Android framework adapter and keep status interpretation testable independently of Android framework classes.

## 5. Components

### 5.1 `KioskDeviceAdminReceiver`

A minimal subclass of `android.app.admin.DeviceAdminReceiver`.

Responsibilities:
- Provide the receiver class referenced by the manifest.
- Handle only lifecycle callbacks needed by this feature.
- Optionally emit safe diagnostic logs for enable/disable callbacks.

Constraints:
- Do not apply kiosk policies from receiver callbacks.
- Do not perform network or database operations.
- Do not start long-running work.
- Do not log credentials or sensitive device information.

The receiver declaration must use `android.permission.BIND_DEVICE_ADMIN`, include the `android.app.device_admin` metadata entry, and reference a valid XML resource under `res/xml/`.

The XML metadata should use an empty `<uses-policies />` element because this change requests no legacy device-admin policies. Device Owner status is determined separately through `DevicePolicyManager`.

### 5.2 `DeviceOwnerStatus`

Represent the outcome of a status query explicitly, using Kotlin sealed types consistent with the existing codebase:

```kotlin
sealed interface DeviceOwnerStatus {
    data object NotProvisioned : DeviceOwnerStatus
    data object DeviceOwner : DeviceOwnerStatus
    data class Error(val cause: Throwable) : DeviceOwnerStatus
}
```

The project has no standard `Result`/error wrapper today, so a sealed type is the established equivalent. Per `AGENTS.md`, `Error` MUST NOT be handed to UI code as a raw exception; consumers map it to their own presentation error model. `HomeError` in `feature:home` is the existing example of such a mapping.

`NotProvisioned` means the application package is not the Device Owner. It does not necessarily mean legacy Device Admin is disabled.

`Error` means the status check could not be completed. It must never be treated as successful provisioning.

### 5.3 `DeviceOwnerStatusProvider`

Define a small interface for retrieving the current status. Prefer a synchronous API if it only wraps the local `DevicePolicyManager.isDeviceOwnerApp()` call; do not introduce coroutines for a simple immediate framework query.

```kotlin
interface DeviceOwnerStatusProvider {
    fun getStatus(): DeviceOwnerStatus
}
```

The interface contains no Android types, so it can be faked in tests and consumed from any layer without pulling in the framework. Per `AGENTS.md`, the `Context` used to reach `DevicePolicyManager` MUST NOT be a constructor parameter of this interface.

### 5.4 `AndroidDeviceOwnerStatusProvider`

The Android implementation should:

1. Query the platform through a minimal gateway rather than holding Android state itself.
2. Return `DeviceOwner` when the gateway reports `true`.
3. Return `NotProvisioned` when the gateway reports `false`.
4. Map an unexpected exception to `Error`, preserving the cause.

For testability the framework call is isolated behind an internal gateway:

```kotlin
internal interface DeviceOwnerGateway {
    fun isDeviceOwnerApp(applicationId: String): Boolean
}
```

`AndroidDeviceOwnerGateway` is the only production class that touches `DevicePolicyManager`. It takes
the application `Context` in its constructor and keeps it; nobody else receives it.

`AndroidDeviceOwnerStatusProvider` therefore has an `internal` constructor taking the gateway, and a
public factory that consumes a `Context` once:

```kotlin
class AndroidDeviceOwnerStatusProvider internal constructor(
    private val gateway: DeviceOwnerGateway,
    private val applicationId: String,
) : DeviceOwnerStatusProvider {
    companion object {
        fun create(context: Context): AndroidDeviceOwnerStatusProvider { /* ... */ }
    }
}
```

This keeps the testable mapping free of Android types while leaving the framework reachable from
production code, and it keeps the `Context` from travelling through the dependency graph.

The implementation must not cache a successful result indefinitely. A status query should be safe to
repeat when the application needs to refresh provisioning state. The query is synchronous with no
suspension point, so no coroutine scope is introduced and there is no cancellation to preserve.

### 5.5 Dependency injection

Register the Android implementation through existing Koin module conventions, following the same
pattern as `coreDataStoreModule` and `coreNetworkModule`:

```kotlin
val devicePolicyModule = module {
    single<DeviceOwnerStatusProvider> { AndroidDeviceOwnerStatusProvider.create(androidContext()) }
}
```

`androidContext()` resolves the application `Context` inside the DI graph and passes it only to the
platform adapter. No navigation callbacks, `NavController`, Compose state, or other UI-scoped objects
are registered, and no `parametersOf` usage is required.

Do not add a new DI framework, global service locator, persistence, or network abstractions solely for this feature.

`DeviceOwnerStatusProvider` is registered in Koin, and `feature:home` consumes it to render a status card (section 9).

### 5.6 Module boundary

`core:devicepolicy` MUST NOT depend on `app`, `feature:*`, `common:data`, or `common:presentation`. Its only project-free dependencies are the Android framework and Koin, so nothing can form a cycle. Verified with `./gradlew :core:devicepolicy:dependencies`.

### 5.7 Receiver and resources

`KioskDeviceAdminReceiver` lives in the `admin/` subpackage to keep framework lifecycle callbacks physically separate from status logic. It logs only the enable/disable event and applies no policy.

The receiver is declared in `core/devicepolicy/src/main/AndroidManifest.xml` and reaches the application APK through Gradle manifest merging, so the app-level manifest stays unchanged. `android:exported="true"` is required because the system binds the receiver from outside the application process.

Device-admin strings live in the module resources (`values/`, `values-ru/`, `values-uz/`) because Android renders the label and description in the device-administrator UI.

## 6. Manifest and resources

Declare the receiver in `core/devicepolicy/src/main/AndroidManifest.xml` so Gradle manifest merging brings it into the application APK. The app-level `AndroidManifest.xml` keeps only its existing `MainActivity` declaration and is otherwise unchanged.

The receiver should be declared with:
- The fully qualified receiver class.
- `android.permission.BIND_DEVICE_ADMIN`.
- `android:exported="true"`, required because the system binds the receiver from outside the app process.
- The `android.app.device_admin` metadata reference.
- The `android.app.action.DEVICE_ADMIN_ENABLED` intent action.

Add `res/xml/device_admin.xml` in the module containing a valid `<device-admin>` root and an empty `<uses-policies />`.

Do not add HOME/LAUNCHER intent filters or `BOOT_COMPLETED` handling in this change. Do not add `<uses-permission>` entries: `BIND_DEVICE_ADMIN` is a signature-level permission granted by the system, not a runtime permission the app requests.

## 7. Provisioning and development verification

Document a development-only procedure for a compatible test device:

1. Build and install the debug APK.
2. Ensure the device meets Android Device Owner provisioning prerequisites. Typical development workflows require a fresh/reset test device without incompatible existing provisioning or accounts.
3. Run the ADB `dpm set-device-owner` command using the actual application ID and fully qualified receiver component.
4. Inspect `adb shell dumpsys device_policy`.
5. Query the app's status provider and verify it returns `DeviceOwner`.
6. Verify an ordinary installation without provisioning returns `NotProvisioned`.

Do not hardcode an assumed package name in project documentation. Use the actual Gradle `applicationId` and receiver class. ADB provisioning is for development/testing; production enrollment is a separate future change.

## 8. Error handling and observability

- Status-check errors must remain distinguishable from `NotProvisioned`.
- Do not report successful Device Owner status unless Android confirms it.
- Logs must be minimal and must not contain secrets.
- The feature must not crash the existing Home Screen because provisioning is absent.
- Status queries must be side-effect free.

## 9. Testing strategy

### Unit tests

Test status interpretation in `core/devicepolicy/src/test`, using JUnit 5 and a fake `DeviceOwnerGateway` per `AGENTS.md`:
- Device Owner API result is `true` -> `DeviceOwner`.
- Device Owner API result is `false` -> `NotProvisioned`.
- Status lookup throws -> `Error`, carrying the original cause.
- Repeated queries re-evaluate the current status rather than returning a permanently cached result: flip the fake gateway result between calls and assert the second query reflects it.
- Koin module resolves `DeviceOwnerStatusProvider` (per `AGENTS.md`, every parameterized/registered resolution needs a test).

The existing test stack is JUnit 5 with `mockk` (`common:data`, `feature:home`, `core:navigation` tests), so the new module uses the `android.library.test` convention plugin and needs no new test dependencies.

The framework call is isolated behind `DeviceOwnerGateway` precisely so these tests run on the JVM without Robolectric or a device.

### Android/device verification

On a compatible test device:
- Verify that the merged manifest receiver and XML metadata are accepted.
- Verify provisioning through ADB.
- Verify `dumpsys device_policy` identifies the expected Device Owner.
- Verify the app reports the correct state before and after provisioning.
- Verify the existing Home Screen still works when the app is not provisioned.
- Verify that this change does not apply Lock Task or other kiosk policies.

## 10. Acceptance and follow-up

This change is complete when the receiver is registered, status detection is testable, ADB provisioning is documented and verified, and the Home Screen remains unchanged.

The next change, `add-kiosk-policy-management`, may build on the DPC foundation to apply actual device policies. It must not be implemented as part of this change.

## 9. Provisioning status card

`feature:home` renders the provisioning state so an operator can see at a glance whether the device
is managed, without a log or an ADB session.

### Boundary

`feature:home` presentation MUST NOT hold `DeviceOwnerStatus`. The mapping happens in the ViewModel,
which produces a Home-owned enum:

```kotlin
enum class ProvisioningStatus { DeviceOwner, NotProvisioned, Unavailable }
```

`Unavailable` deliberately carries no payload. The cause stays inside `core:devicepolicy`, which is
what keeps an exception message or type out of the UI. The enum matches the existing `HomeError`
convention rather than introducing a new sealed hierarchy.

The card is stateless and previewable; it renders one string chosen by the enum. All wording lives in
resources, including Russian and Uzbek, per `AGENTS.md`.

### Lifetime

The status is read in the ViewModel's initial intent and again on every refresh action. It is never
read during composition, and the provider result is never cached beyond that read, so a device
provisioned while the application is running is reflected on the next refresh.

The provider query is synchronous and cheap, so no coroutine or loading state is introduced for it.
A status read that fails is mapped to `Unavailable` and MUST NOT disturb the Home data flow.

### Dependency direction

`feature:home` depends on `core:devicepolicy`. The reverse dependency does not exist, and
`core:devicepolicy` continues to depend on no project module, so no cycle is formed.
