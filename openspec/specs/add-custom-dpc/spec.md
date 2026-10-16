# add-custom-dpc Specification

## Purpose

Defines the Device Policy Controller foundation of the application: the administrative component
that Android recognizes, and a way to ask whether the installation is actually provisioned as Device
Owner.

This capability exists because every later kiosk capability depends on knowing that the application
holds device-owner authority. It covers registration and status detection only; applying policies,
Lock Task Mode, persistent HOME, startup and remote management belong to the changes that follow it.

Note for implementers: a provisioned installation is a *protected* package on Android. It cannot be
force-stopped or uninstalled from the shell, so provisioning state is not reversible without a factory
reset.

## Requirements

### Requirement: Device administrator component registration

The application MUST declare a custom `DeviceAdminReceiver` in its Android manifest.

The receiver MUST be protected by the required Android system permission and MUST reference valid device-administrator metadata.

The receiver MUST NOT perform kiosk policy configuration, remote communication, database operations, or long-running tasks as part of its registration lifecycle.

#### Scenario: Android discovers the administrative component

- GIVEN the application is installed on a compatible Android device
- WHEN Android inspects the application's administrative component
- THEN the receiver MUST be declared with the required permission and metadata
- AND the receiver MUST be available for the supported device-management provisioning workflow

#### Scenario: Administrative component receives a system callback

- GIVEN the receiver is registered
- WHEN Android delivers a supported administrative lifecycle callback
- THEN the receiver MUST handle the callback without performing unrelated business operations
- AND any diagnostic logging MUST NOT expose credentials or sensitive device information

### Requirement: Device Owner status detection

The application MUST provide a dedicated abstraction for determining whether its package is provisioned as Device Owner.

The implementation MUST use the Android-supported `DevicePolicyManager.isDeviceOwnerApp()` API to determine Device Owner status.

The result MUST distinguish `NotProvisioned`, `DeviceOwner`, and `Error`.

The implementation MUST NOT equate receiver registration, legacy device-administrator activation, or ordinary application installation with Device Owner provisioning.

#### Scenario: Application is not provisioned

- GIVEN the application is installed as an ordinary application
- WHEN the device-management status is queried
- THEN the result MUST be `NotProvisioned`
- AND the application MUST NOT claim to have Device Owner privileges

#### Scenario: Application is provisioned as Device Owner

- GIVEN the application has successfully been assigned Device Owner on a compatible device
- WHEN the device-management status is queried
- THEN the result MUST be `DeviceOwner`

#### Scenario: Status detection fails

- GIVEN the device-management status cannot be determined because an operation fails
- WHEN the status abstraction handles the failure
- THEN the result MUST be `Error`
- AND the failure MUST NOT be interpreted as `DeviceOwner`

### Requirement: Provisioning-state exposure

The application MUST expose the current provisioning state through a testable device-management abstraction.

The provisioning-state implementation MUST be independent of the existing Home Screen's business logic.

The initial implementation MUST NOT require a backend connection, persistent database, or kiosk-specific UI.

#### Scenario: Existing Home Screen runs without provisioning

- GIVEN the application has not been provisioned as Device Owner
- WHEN the Home Screen is opened
- THEN the Home Screen MUST remain usable according to its existing behavior
- AND the provisioning state MUST be available to the application for diagnostics or future integration

#### Scenario: Provisioning state is queried repeatedly

- GIVEN the application has a device-management status abstraction
- WHEN the status is queried more than once
- THEN each result MUST reflect the outcome of the relevant status check
- AND the implementation MUST NOT permanently cache a successful Device Owner state without a defined refresh strategy

### Requirement: Provisioning state is surfaced to the operator

The application MUST display the current provisioning state on its start screen, covering a
provisioned device, an unprovisioned device, and a status that could not be determined.

When the state could not be determined, the UI MUST explain that the state is unavailable and MUST NOT
show the failure detail, exception type or message; the raw failure MUST NOT cross the layer boundary
into presentation state.
#### Scenario: Provisioned device is reported

- GIVEN the application is provisioned as Device Owner
- WHEN the start screen is shown
- THEN the card MUST report that the device is managed as Device Owner

#### Scenario: Unprovisioned device is reported

- GIVEN the application is not provisioned as Device Owner
- WHEN the start screen is shown
- THEN the card MUST report that the device is not managed yet

#### Scenario: Undeterminable state is explained without details

- GIVEN the provisioning state cannot be determined because a check fails
- WHEN the start screen is shown
- THEN the card MUST state that the device-management status is unavailable
- AND it MUST NOT display the failure message, exception type or any other failure detail

#### Scenario: Refresh picks up a new provisioning state

- GIVEN the start screen was shown while the application was not provisioned
- AND the application is provisioned while the start screen remains open
- WHEN the operator refreshes the screen
- THEN the card MUST report the new provisioned state

### Requirement: Development provisioning procedure

The project MUST document how to provision the application as Device Owner on a compatible development device using ADB.

The procedure MUST state the prerequisites and MUST NOT imply that Device Owner can be assigned to any already-configured device.

#### Scenario: Compatible test device is provisioned

- GIVEN the application is installed
- AND the test device satisfies Android's provisioning prerequisites
- WHEN the documented ADB provisioning procedure succeeds
- THEN the application MUST be detectable as Device Owner
- AND the result MUST be verifiable through the application status abstraction and Android device-policy diagnostics

#### Scenario: Provisioning cannot be completed

- GIVEN the device does not satisfy the required provisioning conditions
- WHEN the provisioning procedure fails
- THEN the failure MUST be documented
- AND the application MUST NOT report successful Device Owner provisioning without verification

### Requirement: Testability of the device-policy layer

The Device Owner status interpretation MUST be unit testable without requiring a physical Android device.

Android framework interactions MUST be isolated behind an appropriate interface or adapter.

The implementation MUST provide tests for successful provisioning, missing provisioning, and status-check failure.

#### Scenario: Unit tests evaluate status mapping

- GIVEN a test implementation of the device-management status provider
- WHEN the provider returns each supported status outcome
- THEN the application MUST interpret each outcome correctly
- AND failure cases MUST NOT result in a false successful-provisioning state

### Requirement: No kiosk policy side effects

This change MUST introduce only the administrative foundation and provisioning-state detection.

It MUST NOT configure Lock Task Mode, modify persistent HOME activity resolution, apply kiosk-specific user restrictions, or change the system UI.

#### Scenario: The application is installed but kiosk policies are not implemented

- GIVEN the application is installed and may be provisioned as Device Owner
- WHEN the device-policy layer initializes
- THEN it MUST NOT automatically activate Lock Task Mode
- AND it MUST NOT apply kiosk-specific system restrictions
- AND the existing Home Screen behavior MUST remain unchanged
