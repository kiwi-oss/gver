package org.dmfs.gradle.gver.tasks;

import org.dmfs.gradle.gver.utils.Repository;
import org.dmfs.gradle.gver.utils.TestProject;
import org.dmfs.gver.dsl.Strategy;
import org.dmfs.gver.git.changetypefacories.condition.CommitMessage;
import org.dmfs.gver.git.predicates.Contains;
import org.dmfs.jems2.iterable.Mapped;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.saynotobugs.confidence.description.Text;
import org.saynotobugs.confidence.junit5.engine.Assertion;
import org.saynotobugs.confidence.junit5.engine.Confidence;
import org.saynotobugs.confidence.junit5.engine.resource.TempDir;

import static org.dmfs.gver.git.ChangeType.*;
import static org.dmfs.jems2.confidence.Jems2.procedureThatAffects;
import static org.eclipse.jgit.lib.Constants.R_TAGS;
import static org.junit.jupiter.api.Assertions.fail;
import static org.saynotobugs.confidence.junit5.engine.ConfidenceEngine.*;
import static org.saynotobugs.confidence.quality.Core.*;


@Confidence
class TagReleaseTaskTest
{
    Assertion gitTagRelease_adds_a_next_patch_tag =
        withResources("with temporary directory and git repository",
            new TempDir(),
            new Repository(getClass().getClassLoader().getResource("0.0.2-alpha.1.bundle"), "main"),

            (tempDir, repo) -> withResource(
                new TestProject(tempDir,
                    new Strategy(
                        MAJOR.when(new CommitMessage(new Contains("#major"))),
                        MINOR.when(new CommitMessage(new Contains("#minor"))),
                        PATCH.when(new CommitMessage(new Contains("#patch"))),
                        UNKNOWN.when(((repository1, commit, branches) -> true)))),
                project -> assertionThat(
                    repository -> ((TagReleaseTask) project.getTasks().getByName("gitTagRelease")).perform(),
                    is(procedureThatAffects(
                        new Text("alters repository"),
                        () -> repo,
                        soIt(has(repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                            iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.0.2"))))))));


    Assertion gitTagRelease_no_tag_on_feature_branch =
        withResources("with temporary directory and git repository",
            new TempDir(),
            new Repository(getClass().getClassLoader().getResource("0.2.0-alpha.1.feature.bundle"), "feature"),

            (tempDir, repo) -> withResource(new TestProject(tempDir,
                    new Strategy(
                        MAJOR.when(new CommitMessage(new Contains("#major"))),
                        MINOR.when(new CommitMessage(new Contains("#minor"))),
                        PATCH.when(new CommitMessage(new Contains("#patch"))),
                        UNKNOWN.when(((repository1, commit, branches) -> true)))),
                project -> assertionThat(
                    repository -> {
                        try
                        {
                            ((TagReleaseTask) project.getTasks().getByName("gitTagRelease")).perform();
                            fail("did not throw");
                        }
                        catch (Exception e)
                        {
                            // ignore the exception until we have a Confidence Quality to test for it
                            // for now we just ensure no tag has been added
                        }
                    },
                    is(procedureThatAffects(
                        new Text("alters repository"),
                        () -> repo,
                        soIt(has(repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                            iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.1.0"))))))));


    Assertion gitTagRelease_skips_existing_tag =
        withResources("with temporary directory and git repository",
            new TempDir(),
            new Repository(getClass().getClassLoader().getResource("0.2.0-trivial-change.bundle"), "main"),

            (tempDir, repo) -> withResource(new TestProject(tempDir,
                    new Strategy(
                        NONE.when(new CommitMessage(new Contains("#none"))),
                        MAJOR.when(new CommitMessage(new Contains("#major"))),
                        MINOR.when(new CommitMessage(new Contains("#minor"))),
                        PATCH.when(new CommitMessage(new Contains("#patch"))),
                        UNKNOWN.when(((repository1, commit, branches) -> true)))),
                project -> assertionThat(
                    repository -> ((TagReleaseTask) project.getTasks().getByName("gitTagRelease")).perform(),
                    is(procedureThatAffects(
                        new Text("alters repository"),
                        () -> repo,
                        soIt(has(repository -> new Mapped<>(Ref::getName, new Git(repository).tagList().call()),
                            iteratesInAnyOrder(R_TAGS + "0.0.1", R_TAGS + "0.1.0", R_TAGS + "0.2.0"))))))));
}