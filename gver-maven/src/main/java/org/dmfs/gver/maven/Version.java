package org.dmfs.gver.maven;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.DelegatingScript;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.dmfs.gver.dsl.GitVersionConfig;
import org.dmfs.gver.git.GitVersion;
import org.dmfs.gver.git.changetypefacories.FirstOf;
import org.dmfs.jems2.Fragile;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * The version of the current Git hosted Maven project.
 */
@Named
@Singleton
public final class Version implements Fragile<org.dmfs.semver.Version, MojoExecutionException>
{
    @Inject
    private MavenProject mProject;

    @Override
    public org.dmfs.semver.Version value() throws MojoExecutionException
    {
        Object pluginConfig = mProject.getBuildPlugins().stream().filter(plugin -> "gver-maven".equals(plugin.getArtifactId()))
            .findFirst().map(Plugin::getConfiguration).map(c -> extractNestedStrings("config", (Xpp3Dom) c)).get();
        try
        {
            Repository repo = new FileRepositoryBuilder().setWorkTree(mProject.getBasedir()).build();

            CompilerConfiguration cc = new CompilerConfiguration();
            cc.setScriptBaseClass(DelegatingScript.class.getName());
            GroovyShell shell = new GroovyShell(DelegatingScript.class.getClassLoader(), new Binding(), cc);
            DelegatingScript configClosure = (DelegatingScript) shell.parse(pluginConfig.toString());

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


    /**
     * Extracts nested values from the given config object into a List.
     *
     * @param childname the name of the first subelement that contains the list
     * @param config    the actual config object
     */
    private List extractNestedStrings(String childname, Xpp3Dom config)
    {

        final Xpp3Dom subelement = config.getChild(childname);
        if (subelement != null)
        {
            List result = new LinkedList();
            final Xpp3Dom[] children = subelement.getChildren();
            for (int i = 0; i < children.length; i++)
            {
                final Xpp3Dom child = children[i];
                result.add(child.getValue());
            }
            return result;
        }

        return null;
    }

}
