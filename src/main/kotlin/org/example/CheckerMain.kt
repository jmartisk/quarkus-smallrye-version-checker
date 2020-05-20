package org.example

import io.quarkus.runtime.Quarkus

// for running the script within an IDE
fun main() {
    Quarkus.run(SmallRyeVersionChecker::class.java)
}