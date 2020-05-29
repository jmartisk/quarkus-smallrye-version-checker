package org.example

import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.annotations.QuarkusMain
import org.jboss.logging.Logger
import picocli.CommandLine
import java.util.concurrent.atomic.AtomicBoolean

@QuarkusMain
class SmallRyeVersionChecker : io.quarkus.runtime.QuarkusApplication {

    val logger = Logger.getLogger("SmallRyeVersionChecker")

    companion object {
        val stopRequested = AtomicBoolean(false)

        fun throwExceptionIfStopWasRequested() {
            if (stopRequested.get()) {
                throw StopRequested()
            }
        }

        // to run the script within an IDE, create a run configuration from this main method
        @JvmStatic fun main(args: Array<String>) {
            Quarkus.run(SmallRyeVersionChecker::class.java, *args)
        }
    }

    override fun run(vararg args: String?): Int {
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Requesting stop of the script, " +
                    "this might take a few seconds if there are active operations")
            stopRequested.set(true)
        })
        val arguments: Arguments = parseArguments(*args)
        try {
            val quarkusRepo = QuarkusRepo(url = arguments.url, ref = arguments.ref)
            val runtimeBom = QuarkusMavenProject(quarkusRepo.getFile("bom/runtime/pom.xml").toFile())

            getCheckedComponents().forEach {
                throwExceptionIfStopWasRequested()
                logger.info(" ")
                logger.info("***********************************************")
                logger.info(" ")
                logger.info("Checking component: ${it.name}")

                val componentVersion = runtimeBom.resolveVersion(it.smallRyeGroupId, it.smallRyeArtifactId)
                logger.info("${it.smallRyeGroupId}:${it.smallRyeArtifactId} version used by Quarkus runtime: $componentVersion")

                val tckPom = QuarkusMavenProject(quarkusRepo.getFile(it.tckPomPath).toFile())
                val usedTckVersion = tckPom.resolveVersion(it.tckGroupId, it.tckArtifactId)
                logger.info("${it.tckGroupId}:${it.tckArtifactId} version being tested in Quarkus: $usedTckVersion")

                val specVersionUsedByProject = it.getSpecDependencyVersion(componentVersion)
                if (specVersionUsedByProject != null) {
                    logger.info("SmallRye project depends on spec version $specVersionUsedByProject")
                    if (specVersionUsedByProject != usedTckVersion) {
                        logger.warn("Quarkus tests a different TCK version than what the SmallRye project depends on!")
                    }
                } else {
                    logger.error("Could not find version of ${it.specGroupId}:${it.specArtifactId} " +
                            "dependency within ${it.smallRyeGroupId}:${it.smallRyeArtifactId}:jar:${componentVersion}")
                }
            }
            logger.info(" ")
            logger.info("***********************************************")
            logger.info(" ")
            return 0
        } catch (t: Throwable) {
            // never let the app fail by throwing an exception
            // avoid "java.lang.IllegalStateException: Cannot reset a started application" in devmode
            // instead log the exception and return a non-zero value
            if (t !is StopRequested) {
                logger.error("Version checker failed", t)
            }
            return 1
        }
    }

    fun parseArguments(vararg args: String?): Arguments {
        val result = Arguments()
        CommandLine(result).parseArgs(*args)
        return result
    }

    fun getCheckedComponents(): Collection<SmallRyeComponent> {
        return listOf(
                SmallRyeComponent("SmallRye Metrics",
                        "io.smallrye", "smallrye-metrics",
                        "org.eclipse.microprofile.metrics", "microprofile-metrics-api-tck",
                        "org.eclipse.microprofile.metrics", "microprofile-metrics-api",
                        "extensions/smallrye-metrics/runtime/pom.xml",
                        "tcks/microprofile-metrics/api/pom.xml"),
                SmallRyeComponent("SmallRye Config",
                        "io.smallrye.config", "smallrye-config",
                        "org.eclipse.microprofile.config", "microprofile-config-tck",
                        "org.eclipse.microprofile.config", "microprofile-config-api",
                        "core/runtime/pom.xml",
                        "tcks/microprofile-config/pom.xml"),
                SmallRyeComponent("SmallRye Health",
                        "io.smallrye", "smallrye-health",
                        "org.eclipse.microprofile.health", "microprofile-health-tck",
                        "org.eclipse.microprofile.health", "microprofile-health-api",
                        "tcks/microprofile-health/pom.xml",
                        "tcks/microprofile-health/pom.xml"),
                SmallRyeComponent("SmallRye Fault Tolerance",
                        "io.smallrye", "smallrye-fault-tolerance",
                        "org.eclipse.microprofile.fault-tolerance", "microprofile-fault-tolerance-tck",
                        "org.eclipse.microprofile.fault-tolerance", "microprofile-fault-tolerance-api",
                        "extensions/smallrye-fault-tolerance/pom.xml",
                        "tcks/microprofile-fault-tolerance/pom.xml"),
                SmallRyeComponent("SmallRye Context Propagation",
                        "io.smallrye", "smallrye-context-propagation",
                        "org.eclipse.microprofile.context-propagation", "microprofile-context-propagation-tck",
                        "org.eclipse.microprofile.context-propagation", "microprofile-context-propagation-api",
                        "extensions/smallrye-context-propagation/pom.xml",
                        "tcks/microprofile-context-propagation/pom.xml"),
                SmallRyeComponent("SmallRye JWT",
                        "io.smallrye", "smallrye-jwt",
                        "org.eclipse.microprofile.jwt", "microprofile-jwt-auth-tck",
                        "org.eclipse.microprofile.jwt", "microprofile-jwt-auth-api",
                        "extensions/smallrye-jwt/pom.xml",
                        "tcks/microprofile-jwt/pom.xml"),
                SmallRyeComponent("SmallRye OpenAPI",
                        "io.smallrye", "smallrye-open-api",
                        "org.eclipse.microprofile.openapi", "microprofile-openapi-tck",
                        "org.eclipse.microprofile.openapi", "microprofile-openapi-api",
                        "extensions/smallrye-openapi/pom.xml",
                        "tcks/microprofile-openapi/pom.xml"),
                SmallRyeComponent("SmallRye OpenTracing",
                        "io.smallrye", "smallrye-opentracing",
                        "org.eclipse.microprofile.opentracing", "microprofile-opentracing-tck",
                        "org.eclipse.microprofile.opentracing", "microprofile-opentracing-api",
                        "extensions/smallrye-opentracing/pom.xml",
                        "tcks/microprofile-opentracing/base/pom.xml"),
                SmallRyeComponent("SmallRye Reactive Messaging",
                        "io.smallrye.reactive", "smallrye-reactive-messaging-provider",
                        "org.eclipse.microprofile.reactive.messaging", "microprofile-reactive-messaging-tck",
                        "org.eclipse.microprofile.reactive.messaging", "microprofile-reactive-messaging-api",
                        "extensions/smallrye-reactive-messaging/pom.xml",
                        "tcks/microprofile-reactive-messaging/pom.xml"),
                SmallRyeComponent("RESTEasy MicroProfile JAX-RS Client",
                        "org.jboss.resteasy", "resteasy-client-microprofile",
                        "org.eclipse.microprofile.rest.client", "microprofile-rest-client-tck",
                        "org.eclipse.microprofile.rest.client", "microprofile-rest-client-api",
                        "extensions/rest-client/pom.xml",
                        "tcks/microprofile-rest-client/pom.xml")
        )
    }

}