# EUDI SD-JWT

:heavy_exclamation_mark: **Important!** Before you proceed, please read
the [EUDI Wallet Reference Implementation project description](https://github.com/eu-digital-identity-wallet/.github/blob/main/profile/reference-implementation.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)


## Table of contents

* [Overview](#overview)
* [Installation](#installation)
* [Use cases supported](#use-cases-supported)
* [How to contribute](#how-to-contribute)
* [License](#license) 

## Overview

Statium is a Kotlin multiplatform library supporting JVM and Android platforms. 
It implements the Token Status List Specification [draft 10](https://www.ietf.org/archive/id/draft-ietf-oauth-status-list-10.html), 
and allows callers to check the status of a "Referenced Token" as defined in the specification, 
effectively enabling applications to verify if tokens are valid, revoked, or in other states.

## Installation

To include the Statium library in your project, add the following dependency:

### Kotlin Multiplatform Project

```kotlin
// build.gradle.kts
dependencies {
    implementation("eu.europa.ec.eudi:eudi-lib-kmp-statium:$statium_ver")
}
```

### Java Project

```kotlin
// build.gradle.kts
dependencies {
    implementation("eu.europa.ec.eudi:eudi-lib-kmp-statium-jvm:$statium_ver")
}
```

### Android Project

```kotlin
// build.gradle.kts
dependencies {
    implementation("eu.europa.ec.eudi:eudi-lib-kmp-statium-android:$statium_ver")
}
```



## Use cases supported


## How to contribute

We welcome contributions to this project. To ensure that the process is smooth for everyone
involved, follow the guidelines found in [CONTRIBUTING.md](CONTRIBUTING.md).

## License

### License details

Copyright (c) 2023 European Commission

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
