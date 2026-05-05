android-startup-template/
│
├── apps/
│   ├── app-main/                 # First production app
│   │   ├── src/
│   │   └── build.gradle.kts
│   │
│   ├── app-sample/               # OPTIONAL: clean reference template (no business logic)
│   │   ├── src/
│   │   └── build.gradle.kts
│
├── core/
│   ├── core-ui/
│   ├── core-data/
│   ├── core-domain/
│   └── core-common/
│
├── features/
│   ├── feature-auth/
│   ├── feature-onboarding/
│   
│
├── brand-kit/
│
├── build-logic/                 # convention plugins (must-have)
│
├── gradle/
├── libs.versions.toml
├── settings.gradle.kts
├── build.gradle.kts
└── .github/workflows/