# Fix Application Localization

## Why

The project claims Russian and Uzbek support. The code does not deliver it.

Three concrete defects:

1. `feature:home` ships four user-visible strings in `values/` only. There is no `values-ru` and no
   `values-uz`, so a Russian or Uzbek user sees the English start screen.
2. The application module uses the BCP 47 qualifiers `values-b+ru+RU` and `values-b+uz+Latn+UZ`.
   Android only honours `b+` qualifiers from API 33 (Android 13). With `minSdk = 24`, the
   application label is never localized on Android 7 through 12.
3. `android:localeConfig` is an API 33 attribute, which Android Lint already reports as
   `UnusedAttribute`.

The result is that localization appears to be implemented and silently does nothing on the majority
of supported devices. The defect is invisible in code review because the resource files exist.

## Goals

- Deliver Russian and Uzbek start-screen text on every supported API level.
- Use resource qualifier directories that `AGENTS.md` prescribes and that work from API 24.
- Remove the lint finding caused by the API 33 manifest attribute.
- Record one localization convention so later features do not repeat the defect.

## Proposed approach

### Legacy-compatible locale resources

Replace the `values-b+<lang>+<REGION>` directories with plain language qualifiers:

```text
app/src/main/res/values-ru/strings.xml
app/src/main/res/values-uz/strings.xml
```

`values-ru` and `values-uz` are honoured from API 24, so the application label resolves on every
supported device. `localeConfig` stays: on API 33+ it additionally exposes the per-app language
setting in system settings, and it is harmless on older versions once the lint finding is addressed.

### Application label

`app_name` is a brand name and is identical in every locale today. Rather than shipping three copies
of the same string, keep it in `values/` only and delete the per-locale duplicates. The label stays
"Flex Kiosk App" in every language.

### Home feature translations

Add `values-ru/strings.xml` and `values-uz/strings.xml` to `feature:home` with translations of the
four existing keys. No key is added, renamed or removed, so `HomeScreen` does not change.

### Lint

`localeConfig` is required for per-app language settings on API 33+ and is the only supported way to
get them. Suppress the `UnusedAttribute` finding locally with `tools:ignore` and a comment recording
why, rather than dropping the attribute and losing the API 33+ capability.

## Scope

### Included

- Migrate the application module from `values-b+*` to `values-ru` / `values-uz`.
- Add Russian and Uzbek translations for the `feature:home` strings.
- Remove the redundant localized copies of `app_name`.
- Suppress and document the `localeConfig` lint finding.
- Document the localization convention for new features.

### Excluded

- A per-app language picker in the UI. On API levels below 33 it requires AppCompat locale APIs,
  which the project does not depend on. This belongs in a separate change if it is ever needed.
- In-app language switching without a device restart.
- Translating any feature other than `feature:home`; only Home has user-visible strings today.
- Changing the brand name or localizing `app_name`.
- Changing the start route, the navigation graph, the Home data chain or any business behavior.
- Raising `minSdk`.
- Any kiosk, device-policy, network or persistence work.

## Acceptance criteria

- `feature:home` has `values-ru/strings.xml` and `values-uz/strings.xml` containing every key that
  `values/strings.xml` declares.
- No key that exists in `values/` is missing from a locale file, and no locale file declares a key
  that `values/` does not.
- The application module no longer contains `values-b+ru+RU` or `values-b+uz+Latn+UZ`.
- `./gradlew :app:assembleDebug` succeeds and the built APK resolves `feature_home_title` under the
  `ru` and `uz` locales.
- `./gradlew lint` reports no `UnusedAttribute` finding for `localeConfig`.
- The rendering behavior of `HomeScreen` is unchanged; no Kotlin source is modified.
- A lint check fails when a locale file drifts out of sync with the default file, so the defect
  cannot silently return.

## Rollout

1. Verify the exact set of string keys per module.
2. Add the lint guard that detects missing or extra translations.
3. Migrate the application module locale directories.
4. Add the Home translations.
5. Verify the built APK resolves the localized strings.
6. Document the convention.
7. Review and archive.

## Open questions

- The Russian and Uzbek wording for the Home screen is proposed here and should be confirmed by a
  native speaker before release. Translations are tracked as requirements in `tasks.md`.
