package org.example

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.jboss.logging.Logger
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class QuarkusRepo {

    val gitRepo: Git
    val targetDir: File
    val logger = Logger.getLogger("QuarkusRepo")

    init {
        targetDir = File("quarkus-repo")

        gitRepo = if (targetDir.exists()) {
            logger.info("Reusing existing quarkus repository")
            Git.open(targetDir)
        } else {
            logger.info("Cloning quarkus repository anew")
            Git.cloneRepository()
                    .setDirectory(targetDir)
                    .setURI("https://github.com/quarkusio/quarkus.git")
                    .call()
        }
        val result = gitRepo.pull()
                .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
                .call()
        if (!result.isSuccessful) {
            throw IllegalStateException("Pull not successful")
        }
        logger.info("Git repo ready in ${targetDir.absolutePath}")
    }

    fun getFile(path: String): Path {
        return Paths.get(targetDir.toPath().toAbsolutePath().toString(), path)
    }

}