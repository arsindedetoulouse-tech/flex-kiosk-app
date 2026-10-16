# Tasks: Fix Application Localization

## 1. Confirm the current state

- [x] Inventory the string resources of every module: `app`, `feature:home`, `core:devicepolicy`.
- [x] Confirm `feature:home` has no `values-ru` / `values-uz`.
- [x] Confirm the application module uses `values-b+ru+RU` / `values-b+uz+Latn+UZ`, which are API 33+.
- [x] Confirm `android:localeConfig` produces an `UnusedAttribute` lint finding.
- [x] Confirm `app_name` is identical in every locale file.
- [x] Confirm no Kotlin source needs to change.

## 2. Add the localization consistency check (test first)

- [ ] Add a resource-key consistency test helper to `common:testing` that, given a module resource
      directory, compares the keys of `values/`, `values-ru/` and `values-uz/`.
- [ ] Write the failing test for `feature:home`, proving the missing Russian and Uzbek keys are
      reported before the translations exist.
- [ ] Confirm the test fails for the expected reason.
- [ ] Confirm the test fails for the opposite reason too, by temporarily adding a surplus key to a
      locale file.
- [ ] Extend the check to reject the prohibited `values-b+*` qualifier form.

## 3. Migrate the application module locale resources

- [ ] Add `values-ru/strings.xml` and `values-uz/strings.xml` for `app`.
- [ ] Delete `values-b+ru+RU/` and `values-b+uz+Latn+UZ/`.
- [ ] Verify `app_name` remains declared in `app/src/main/res/values/strings.xml`.
- [ ] Verify no locale file re-declares `app_name`.

## 4. Add the Home translations

- [ ] Translate `feature_home_title` into Russian.
- [ ] Translate `feature_home_hint` into Russian.
- [ ] Translate `feature_home_error` into Russian.
- [ ] Translate `feature_home_refresh` into Russian.
- [ ] Translate all four keys into Uzbek.
- [ ] Escape apostrophes and other characters that `aapt2` rejects in string values.
- [ ] Confirm the key sets now match and the consistency test passes.

## 5. Silence the localeConfig lint finding without losing the capability

- [ ] Add `tools:ignore="UnusedAttribute"` to the `android:localeConfig` attribute in
      `app/src/main/AndroidManifest.xml`.
- [ ] Add a comment recording that the attribute targets API 33+ per-app language settings and must
      not be removed to satisfy lint.
- [ ] Add the namespace declaration required by `tools:` attributes if it is not already present.
- [ ] Confirm `./gradlew lint` reports no `UnusedAttribute` for `localeConfig`.

## 6. Document the convention

- [ ] Document the locale resource layout in `README.md`.
- [ ] State in `AGENTS.md` that a locale file must be added for every module that declares
      user-visible strings, and that the consistency test enforces it.

## 7. Translation review gate

- [ ] Have a Russian speaker confirm the Russian strings.
- [ ] Have an Uzbek (Latin) speaker confirm the Uzbek strings.
- [ ] Record the reviewer and the date in this change.

## 8. Verification

- [ ] Run `./gradlew test` and confirm the consistency test passes.
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Inspect the built APK and confirm the Russian and Uzbek `feature_home_title` are packaged.
- [ ] Run `./gradlew lint` and confirm no new findings.
- [ ] On a device set to Russian, confirm the start screen renders Russian text.
- [ ] On a device set to Uzbek, confirm the start screen renders Uzbek text.
- [ ] Confirm the rendering behavior of `HomeScreen` is unchanged and no Kotlin file was modified.

## 9. Final validation

- [ ] Review the diff for unrelated changes.
- [ ] Confirm no credentials, generated files or build outputs are included.
- [ ] Confirm the change stays inside the localization scope and touches no kiosk behavior.
- [ ] Mark the change ready for review only after all required checks pass.
