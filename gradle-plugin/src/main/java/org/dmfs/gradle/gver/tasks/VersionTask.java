package org.dmfs.gradle.gver.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;


/**
 * A {@link Task} that logs the current version.
 */
public class VersionTask extends DefaultTask
{
    public VersionTask()
    {
        setGroup("gver");
        setDescription("Shows the current project version.");
    }

    @TaskAction
    public void perform()
    {
        System.out.println(getProject().getVersion());
    }
}
