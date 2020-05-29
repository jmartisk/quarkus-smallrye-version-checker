package org.example

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.ResetCommand
import org.jboss.logging.Logger
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class QuarkusRepo(url: String, ref: String) {

    val gitRepo: Git
    val targetDir: File
    val logger = Logger.getLogger("QuarkusRepo")

    init {
        SmallRyeVersionChecker.throwExceptionIfStopWasRequested()
        targetDir = File("quarkus-repo")

        gitRepo = if (targetDir.exists()) {
            logger.info("Reusing existing quarkus repository at ${targetDir.absolutePath}")
            Git.open(targetDir)
        } else {
            logger.info("Cloning quarkus repository anew")
            Git.cloneRepository()
                    .setDirectory(targetDir)
                    .setURI(url)
                    .call()
        }
        gitRepo.reset().setMode(ResetCommand.ResetType.HARD).call()
        gitRepo.fetch().call()
        gitRepo.checkout().setName(ref).call()
        // FIXME: guessing whether we are currently at a branch. Is there a better way than this?
        // If we are at a tag, then gitRepo.repository.branch returns a commit hash instead of branch name,
        // so if the branch name length is 40 we assume it is a tag
        val isBranch = gitRepo.repository.branch.length != 40
        if(isBranch) {
            // if we're running from a branch, update it
            val result = gitRepo.pull()
                    .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
                    .call()
            if (!result.isSuccessful) {
                throw IllegalStateException("Fast forward not successful")
            }
        }
        logger.info("Git repo ready in ${targetDir.absolutePath}")
        logger.info("Using ref: ${gitRepo.repository.branch}")
    }

    fun getFile(path: String): Path {
        return Paths.get(targetDir.toPath().toAbsolutePath().toString(), path)
    }

}