package org.example

import org.jboss.shrinkwrap.resolver.api.maven.Maven
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage
import java.io.File

class QuarkusMavenProject(parentPom: File) {

    val project: PomEquippedResolveStage = Maven.configureResolver()
            .loadPomFromFile(parentPom)

    fun resolveVersion(groupId: String, artifactId: String): String {
        val candidates = project
                .importCompileAndRuntimeDependencies()
                .resolve("$groupId:$artifactId")
                .withoutTransitivity()
                .asResolvedArtifact()
                .filter { artifact ->
                    artifact.coordinate.groupId.equals(groupId) &&
                            artifact.coordinate.artifactId.equals(artifactId)
                }
        return candidates.first().resolvedVersion
    }

}