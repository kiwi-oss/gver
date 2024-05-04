package org.dmfs.gver.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dmfs.semver.Release;
import org.dmfs.semver.VersionSequence;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Updates maven project with current version.
 */
@Mojo(name = "set-release-version")
@Execute(goal = "set-release-version")
public final class GVerSetReleaseVersionMojo extends AbstractMojo
{
    @Parameter(name = "config", defaultValue = "{}", readonly = true, required = true)
    private Object config;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException
    {
        executeMojo(
            plugin(
                groupId("org.codehaus.mojo"),
                artifactId("versions-maven-plugin"),
                version("2.16.2")
            ),
            goal("set"),
            configuration(
                element(name("newVersion"), new VersionSequence(new Release(new Version(mavenProject, config).value())).toString()),
                element(name("generateBackupPoms"), "false")
            ),
            executionEnvironment(
                mavenProject,
                mavenSession,
                pluginManager
            )
        );

    }
}
