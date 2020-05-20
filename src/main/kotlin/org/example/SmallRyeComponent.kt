package org.example

import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.strategy.AcceptAllStrategy

data class SmallRyeComponent(val name: String,
                             val smallRyeGroupId: String,
                             val smallRyeArtifactId: String,
                             val tckGroupId: String,
                             val tckArtifactId: String,
                             val specGroupId: String,
                             val specArtifactId: String,
                             val extensionPomPath: String,
                             val tckPomPath: String) {

    /**
     * Retrieves the MP API dependency version used by a particular version
     * of this SmallRye project. Returns null if no such dependency is found.
     */
    // FIXME: does not work for Reactive Messaging, because smallrye-reactive-messaging does not depend on
    // the official API at all. How do I find out what MP-RM version it is supposed to implement?
    // FIXME: does not work for Metrics either, but I don't understand why
    fun getSpecDependencyVersion(smallRyeVersion: String): String? {
        SmallRyeVersionChecker.throwExceptionIfStopWasRequested()
        val candidates = Maven.configureResolver()
                .resolve("$smallRyeGroupId:$smallRyeArtifactId:jar:$smallRyeVersion")
                .using(AcceptAllStrategy.INSTANCE)
                .asResolvedArtifact()
        val specificationArtifact = candidates.firstOrNull { artifact ->
            artifact.coordinate.artifactId == specArtifactId &&
                    artifact.coordinate.groupId == specGroupId
        }
        return specificationArtifact?.resolvedVersion
    }

}