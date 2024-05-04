package org.dmfs.gver.maven;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.git.GitVersion;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.jems2.Fragile;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.util.Optional;

/**
 * The version of the current Git hosted Maven project.
 */
public final class Version implements Fragile<org.dmfs.semver.Version, MojoExecutionException>
{
    private final MavenProject mProject;
    private final Object mConfig;

    public Version(MavenProject project, Object config)
    {
        mProject = project;
        mConfig = config;
    }

    @Override
    public org.dmfs.semver.Version value() throws MojoExecutionException
    {
        try
        {
            Repository repo = new FileRepositoryBuilder().setWorkTree(mProject.getBasedir()).build();

            CompilerConfiguration cc = new CompilerConfiguration();
            cc.setScriptBaseClass(DelegatingScript.class.getName());
            GroovyShell shell = new GroovyShell(DelegatingScript.class.getClassLoader(), new Binding(), cc);
            DelegatingScript configClosure = (DelegatingScript) shell.parse(mConfig.toString());

            GitVersionConfig config = new GitVersionConfig();
            configClosure.setDelegate(config);
            configClosure.run();

            return new GitVersion(
                new FirstOf(config.mChangeTypeStrategy.mChangeTypeStrategies),
                config.mSuffixes,
                branch -> config.mPreReleaseStrategies.mBranchConfigs.stream()
                    .map(it -> it.apply(branch))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElse("alpha"))
                .value(repo);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e);
        }
    }
}
