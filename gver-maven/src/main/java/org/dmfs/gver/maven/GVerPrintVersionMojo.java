package org.dmfs.gver.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dmfs.semver.VersionSequence;

import javax.inject.Inject;

/**
 * Prints the current project version to the output screen.
 */
@Mojo(name = "print-version")
@Execute(goal = "print-version")
public final class GVerPrintVersionMojo extends AbstractMojo
{
    @Inject
    private Version version;

    @Parameter(name = "config", defaultValue = "{}", readonly = true, required = true)
    private Object config;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;


    @Override
    public void execute() throws MojoExecutionException
    {
        System.out.println(new VersionSequence(version.value()));
    }
}
