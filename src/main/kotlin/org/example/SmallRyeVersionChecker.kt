package org.example

import io.quarkus.runtime.annotations.QuarkusMain
import org.jboss.logging.Logger
import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptScopesStrategy

@QuarkusMain
class SmallRyeVersionChecker : io.quarkus.runtime.QuarkusApplication {

    val logger = Logger.getLogger("SmallRyeVersionChecker")

    override fun run(vararg args: String?): Int {
        try {
            val quarkusRepo = QuarkusRepo()
            val runtimeBom = QuarkusMavenProject(quarkusRepo.getFile("bom/runtime/pom.xml").toFile())

            getCheckedComponents().forEach {
                logger.info(" ")
                logger.info("***********************************************")
                logger.info(" ")
                logger.info("Checking component: ${it.name}")

                val componentVersion = runtimeBom.resolveVersion(it.smallRyeGroupId, it.smallRyeArtifactId)
                logger.info("${it.smallRyeGroupId}:${it.smallRyeArtifactId} version: $componentVersion")

                val tckPom = QuarkusMavenProject(quarkusRepo.getFile(it.tckPomPath).toFile())
                val specVersion = tckPom.resolveVersion(it.tckGroupId, it.tckArtifactId)
                logger.info("${it.tckGroupId}:${it.tckArtifactId} version: $specVersion")

                val candidates = Maven.configureResolver()
                        .resolve("${it.smallRyeGroupId}:${it.smallRyeArtifactId}:jar:${componentVersion}")
                        .using(AcceptScopesStrategy(ScopeType.PROVIDED, ScopeType.TEST, ScopeType.COMPILE))
                        .asResolvedArtifact()
                val specification = candidates.firstOrNull { artifact ->
                    artifact.coordinate.artifactId == it.specArtifactId &&
                            artifact.coordinate.groupId == it.specGroupId
                }
                if(specification != null) {
                    logger.info("SmallRye project uses spec version ${specification.resolvedVersion}")
                } else {
                    // FIXME: does not work for Reactive Messaging, because smallrye-reactive-messaging does not depend on
                    // the official API at all. How do I find out what MP-RM version it is supposed to implement?
                    // FIXME: does not work for Metrics either, but I don't understand why
                    logger.error("Could not find version of ${it.specGroupId}:${it.specArtifactId} " +
                            "dependency within ${it.smallRyeGroupId}:${it.smallRyeArtifactId}:jar:${componentVersion}")
                }
            }
            return 0
        } catch (t: Throwable) {
            // never let the app fail by throwing an exception
            // avoid "java.lang.IllegalStateException: Cannot reset a started application" in devmode
            // instead log the exception and return a non-zero value
            logger.error("Version checker failed", t)
            return 1
        }
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