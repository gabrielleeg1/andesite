/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */


kotlin {
  val hostOs = System.getProperty("os.name")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    hostOs.startsWith("Windows") -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }
  nativeTarget.apply {}

  js(BOTH) {
    browser()
    nodejs()
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("io.ktor:ktor-network:1.6.4")
        implementation("com.squareup.okio:okio:3.0.0")
      }
    }
  }
}
