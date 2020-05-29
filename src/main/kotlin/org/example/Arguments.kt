package org.example

import picocli.CommandLine

class Arguments {

    @CommandLine.Option(names = ["--git-url"], defaultValue = "https://github.com/quarkusio/quarkus.git")
    var url: String = ""

    @CommandLine.Option(names = ["--ref"], defaultValue = "master")
    var ref: String = ""

}