# Localization Support

## ADDED Requirements

### Requirement: Locale resources resolve on every supported API level

The application MUST resolve locale-specific resources on every supported Android version, including
versions below API 33.

Locale resource directories MUST use plain language qualifiers (`values-ru`, `values-uz`) rather
than BCP 47 qualifiers (`values-b+ru+RU`, `values-b+uz+Latn+UZ`), because the qualifier form is only
recognized from API 33 onward.

#### Scenario: Application label resolves on a pre-Android-13 device

- GIVEN the application is installed on a device running Android 7 or later and below Android 13
- AND the device language is Russian
- WHEN the application label is resolved
- THEN the resource system MUST select the Russian locale resources
- AND it MUST NOT fall back to the default locale resources for a language that has translations

#### Scenario: Application label resolves on an Android 13 or later device

- GIVEN the application is installed on a device running Android 13 or later
- AND the device or per-app language is Uzbek
- WHEN the application label is resolved
- THEN the resource system MUST select the Uzbek locale resources

#### Scenario: Unsupported language falls back to the default

- GIVEN the device language has no locale resource directory
- WHEN any user-visible string is resolved
- THEN the default locale resources MUST be used

### Requirement: Every user-visible string is translated

Each module that declares user-visible strings MUST provide a translation for every supported locale.
A key present in the default resources MUST also be present in each locale resource file, and a key
present in a locale file MUST also exist in the default resources.

The build MUST fail when the key sets diverge, so an untranslated string cannot be introduced
silently.

#### Scenario: A string is added without a translation

- GIVEN a module declares a user-visible string in its default resources
- AND the module has locale resource directories for the supported languages
- WHEN the key sets are compared
- THEN the check MUST report the missing key for each locale
- AND the build MUST fail

#### Scenario: A locale declares a key that does not exist

- GIVEN a locale resource file declares a key
- AND the default resource files do not declare that key
- WHEN the key sets are compared
- THEN the check MUST report the surplus key
- AND the build MUST fail

#### Scenario: Home screen strings are translated

- GIVEN the `feature:home` module declares its start-screen strings
- WHEN the Russian and Uzbek resource files are inspected
- THEN every key declared in the default resource file MUST be present in both
- AND each locale file MUST declare a translated value for every key

### Requirement: Brand names are not translated

Application identity strings that are brand names MUST NOT be duplicated into locale resource files.
A brand name MUST remain a single value in the default resources so that an untranslated brand is not
mistaken for a localized one.

#### Scenario: Application label is unchanged across locales

- GIVEN the application declares an application label
- AND the label is a brand name identical in every language
- WHEN the application is installed under any device language
- THEN the resolved label MUST be that single brand value

### Requirement: Forward-looking locale configuration is retained

The application MUST keep declaring its supported languages through the platform locale configuration
attribute so that per-app language selection remains available on the platform versions that support
it.

A lint finding that reports this attribute as unused below API 33 MUST be suppressed locally with a
recorded reason, and MUST NOT be resolved by removing the attribute.

#### Scenario: Per-app language setting is available where the platform supports it

- GIVEN the application is installed on a device running Android 13 or later
- WHEN the user opens the system per-app language settings
- THEN the application MUST be listed with its supported languages

#### Scenario: Lint does not report the attribute as unused

- GIVEN the locale configuration attribute is declared
- WHEN Android Lint analyses the module
- THEN it MUST NOT report an `UnusedAttribute` finding for that attribute

### Requirement: Localization configuration is not silently broken

Resource layout changes that would break locale resolution MUST be detectable by the build. This
covers the class of defect where a resource directory exists but is never selected at runtime.

#### Scenario: A non-functional locale directory is introduced

- GIVEN a module adds a locale resource directory whose qualifier form is not supported by the
  project's minimum API level
- WHEN the localization consistency check runs
- THEN it MUST fail, or the prohibited qualifier form MUST be rejected by the build

## Implementation constraints

- Minimum Android API level: 24 (project convention).
- Supported locales: default (English), Russian (`ru-RU`), Uzbek (`uz-Latn-UZ`).
- Resource directories MUST follow the convention set out in `AGENTS.md`.
- No Kotlin source file may change as a result of this change.
- The consistency check MUST run on the JVM and MUST NOT require an emulator or a device.
- The check MUST NOT attempt to judge translation quality; review of the wording is a human gate.
