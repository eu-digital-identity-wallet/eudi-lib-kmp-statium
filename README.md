# EUDI Statium

:heavy_exclamation_mark: **Important!** Before you proceed, please read
the [EUDI Wallet Reference Implementation project description](https://github.com/eu-digital-identity-wallet/.github/blob/main/profile/reference-implementation.md)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)


## Table of contents

* [Overview](#overview)
* [Disclaimer](#disclaimer)
* [Installation](#installation)
* [Use cases supported](#use-cases-supported)
* [How to contribute](#how-to-contribute)
* [License](#license) 

## Overview

Statium is a Kotlin multiplatform library supporting JVM and Android platforms. 
It implements the Token Status List Specification [draft 10](https://www.ietf.org/archive/id/draft-ietf-oauth-status-list-10.html), 
and allows callers to check the status of a "Referenced Token" as defined in the specification, 
effectively enabling applications to verify if tokens are valid, revoked, or in other states.

## Disclaimer

The released software is an initial development release version:
-  The initial development release is an early endeavor reflecting the efforts of a short time-boxed period, and by no means can be considered as the final product.
-  The initial development release may be changed substantially over time, might introduce new features but also may change or remove existing ones, potentially breaking compatibility with your existing code.
-  The initial development release is limited in functional scope.
-  The initial development release may contain errors or design flaws and other problems that could cause system or other failures and data loss.
-  The initial development release has reduced security, privacy, availability, and reliability standards relative to future releases. This could make the software slower, less reliable, or more vulnerable to attacks than mature software.
-  The initial development release is not yet comprehensively documented.
-  Users of the software must perform sufficient engineering and additional testing to properly evaluate their application and determine whether any of the open-sourced components is suitable for use in that application.
-  We strongly recommend not putting this version of the software into production use.
-  Only the latest version of the software will be supported

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

### Ktor

Statium uses [Ktor](https://ktor.io/) to perform HTTP Requests. Statium doesn't use a specific Client Engine. 
To use Statium in your project, configure your preferred Client Engine implementation as well.

A list of available Client Engines can be found [here](https://ktor.io/docs/client-engines.html)

For instance, to use OkHttp add the following dependency:

```kotlin
dependencies {
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
}
```

## Use cases supported

- [Get Status List Token](#get-status-list-token)
- [Read a Status List](#read-a-status-list)
- [Get Status](#get-status) 

### Get Status List Token

As a `Relying Party` fetch a `Status List Token`.

Library provides for this use case the interface [GetStatusListToken](lib/src/commonMain/kotlin/eu/europa/ec/eudi/statium/GetStatusListToken.kt)

```kotlin
// Create an instance of GetStatusListToken using the usingJwt factory method
val getStatusListToken: GetStatusListToken = GetStatusListToken.usingJwt(
    clock = Clock.System,
    httpClientFactory = {
        HttpClient {
            // Configure your HTTP client here
        }
    },
    verifyStatusListTokenSignature = VerifyStatusListTokenSignature.Ignore // Not for production
)

// Use the GetStatusListToken instance to fetch a status list token
val uri = "https://example.com/status-list"
val result = getStatusListToken(uri, null) // null means "now"

// Handle the result
val claims : StatusListTokenClaims = result.getOrThrow()
println("Status list token claims: $claims")
```

### Read a Status List

As a `Relying Party` be able to read a `Status List` at a specific index.

It is assumed that the caller has already [fetched](#get-status-list-token) 
the `Status List` (via a `Status List Token`)

Library provides for this use case the interface [ReadStatus](lib/src/commonMain/kotlin/eu/europa/ec/eudi/statium/ReadStatus.kt)

```kotlin
// Assuming you have already obtained a StatusListTokenClaims
val claims: StatusListTokenClaims = obtainStatusListTokenClaims() // This function is not shown here
val readStatus: ReadStatus = ReadStatus.fromStatusList(claims.statusList).getOrThrow()
val status = readStatus(StatusIndex(5)).getOrThrow() // check index 5
// Pattern match on status
when (status) {
    Status.Valid -> println("Token is valid")
    Status.Invalid -> println("Token is invalid")
    Status.Suspended -> println("Token is suspended")
    is Status.ApplicationSpecific -> println("Application-specific status: ${status.value}")
    is Status.Reserved -> println("Reserved status: ${status.value}")
}
```
### Get Status

As a `Relying Party` [fetch](#get-status-list-token) the corresponding `Status List Token` 
to validate the status of that `Referenced Token`

It is assumed that the caller has extracted from the `Referenced Token` 
a reference to a `status_list`.

Library provides for this use case the interface [GetStatus](lib/src/commonMain/kotlin/eu/europa/ec/eudi/statium/GetStatus.kt)

```kotlin
// Create an instance of GetStatusListToken (as shown in the Get Status List Token section)
val getStatusListToken: GetStatusListToken = TODO("Check above")

// Create an instance of GetStatus using the GetStatusListToken
val getStatus: GetStatus = GetStatus(getStatusListToken)

// Assuming you have a StatusReference from a Referenced Token
val statusReference = StatusReference(
    index = StatusIndex(42),
    uri = "https://example.com/status-list"
)

// Use the GetStatus instance to check the status of the Referenced Token
val status = runBlocking {
    with(getStatus) {
        statusReference.status(at = null).getOrThrow() // null means "now"
    }
}

// Handle the result
when (status) {
    Status.Valid -> println("Token is valid")
    Status.Invalid -> println("Token is invalid")
    Status.Suspended -> println("Token is suspended")
    is Status.ApplicationSpecific -> println("Application-specific status: ${status.value}")
    is Status.Reserved -> println("Reserved status: ${status.value}")
}
```

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

