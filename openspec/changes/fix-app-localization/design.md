# Design: Fix Application Localization

## 1. Context

The application declares Russian and Uzbek support in `README.md` and `AGENTS.md`. The resource
layout does not deliver it. This change corrects the resource layout and supplies the missing Home
translations. It changes no Kotlin code and no behavior beyond which text is rendered.

## 2. The defect, precisely

Android selects a resource directory by matching qualifiers against the current configuration.
Three facts drive the design:

- A plain language qualifier (`values-ru`) has been supported since API 1 and matches any locale
  whose language is Russian, on every API level.
- The BCP 47 qualifier form (`values-b+ru+RU`, directory name starts with `b+`) was introduced for
  per-app language support in **API 33**. On API 24 through 32 the directory is not recognized as a
  language resource at all, so the strings in it are dead weight.
- `android:localeConfig` in the manifest is likewise API 33+. It tells the system which languages
  the app supports so it can offer them in the per-app language settings. It is not required for the
  app to follow the device language.

So the current configuration localizes the application label only on Android 13+, and localizes
nothing at all in `feature:home` on any version.

## 3. Goals

- Russian and Uzbek Home text on every supported API level.
- Resource layout matching the directories `AGENTS.md` prescribes.
- No lint errors attributable to the localization configuration.
- A guard so a future feature cannot ship an untranslated string unnoticed.

## 4. Non-goals

- Per-app language selection on API levels below 33.
- Any change to `HomeScreen`, `HomeState`, `HomeViewModel` or the Home data chain.
- Translating strings that do not exist yet.
- Changing the brand name.
- Raising `minSdk`.

## 5. Resource layout after this change

```text
app/src/main/res/
├── values/
│   ├── strings.xml            # app_name (brand, untranslated)
│   └── themes.xml
├── values-ru/strings.xml      # kept only if app_name ever needs a translation
├── values-uz/strings.xml
└── xml/locale_config.xml      # unchanged

feature/home/src/main/res/
├── values/strings.xml         # 4 keys, unchanged
├── values-ru/strings.xml      # new: 4 translated keys
└── values-uz/strings.xml      # new: 4 translated keys

core/devicepolicy/src/main/res/   # already uses values-ru / values-uz
```

`app/src/main/res/values-ru/strings.xml` and `values-uz/strings.xml` become empty of keys and are
deleted in this change, because `app_name` is a brand name that must not be translated. They are
kept in the layout diagram only to document that the convention applies if that ever changes.

## 6. Decisions

### 6.1 `values-ru` / `values-uz`, not `values-b+ru+RU`

Chosen because it is the only form that works on the full supported range and because it is what
`AGENTS.md` prescribes. The cost is that on API 24 through 32 the app follows the *device* language
rather than a per-app language. That is the correct and expected behavior for an application with no
language picker.

`localeConfig` is retained rather than removed. Removing it would silence lint but would drop the
per-app language setting on Android 13+, which is a real capability loss for a device meant to be
kiosk-managed.

### 6.2 Suppress `UnusedAttribute` rather than drop the attribute

Lint reports `localeConfig` as unused below API 33. That is correct and intended: the attribute is
forward-looking. The finding is suppressed locally with `tools:ignore="UnusedAttribute"` and a
comment stating why, so the suppression is visible to reviewers and cannot outlive its reason.

### 6.3 Brand name is not translated

`app_name` is identical across locales today. Shipping three identical copies invites the
assumption that the label is localized when it is not. The per-locale files for `app` are deleted.

### 6.4 Translations proposed in the change

The Home screen wording is proposed, not asserted as approved. `tasks.md` carries a review gate for a
native speaker. The strings are short, non-technical and already free of device state.

## 7. Guarding against regression

The defect survived because missing translations produce no build error. Android Lint has
`MissingTranslation`: the project currently does not enable it, and enabling it globally would
immediately fail on the `core:devicepolicy` and `feature:home` modules until translations exist, and
would flag legitimate cases such as brand names.

The guard introduced by this change is therefore a test rather than a lint configuration change:
`common:testing` gains a resource-consistency check that, for a given module resource directory,
asserts that the `values/` keys and the `values-ru` / `values-uz` keys match exactly in both
directions. It runs on the JVM against the resource files, needs no Android runtime, and fails the
build when a key is added without a translation.

This is deliberately narrower than `MissingTranslation`: it does not attempt to prove a translation
is correct, only that none is missing.

## 8. Verification

- Unit test: key sets of `values/`, `values-ru/` and `values-uz/` are equal per module.
- Build: `./gradlew :app:assembleDebug`.
- APK inspection: the merged resources contain the Russian and Uzbek `feature_home_title`.
- Lint: `./gradlew lint` reports no `UnusedAttribute` for `localeConfig`.
- Manual: run the app on a device set to Russian and to Uzbek and confirm the start screen renders
  the localized text. This is the only check that proves end-to-end behavior.

## 9. Risks

- Wrong translations ship if the wording is not reviewed by a native speaker. Mitigated by the gate
  in `tasks.md`; the strings are trivial to correct later without touching code.
- Removing `values-b+*` means an API 33+ user who previously selected a per-app language now follows
  the device language. Since the app never shipped a language picker and the per-app setting was
  only reachable through system settings for an app declaring `localeConfig`, the practical impact is
  limited to a developer preview build.
