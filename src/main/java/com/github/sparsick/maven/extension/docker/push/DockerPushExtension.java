package com.github.sparsick.maven.extension.docker.push;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Named
public class DockerPushExtension extends AbstractEventSpy {

    private final Logger LOGGER = LoggerFactory.getLogger(DockerPushExtension.class);

    @Override
    public void init(Context context) throws Exception {
        super.init(context);
        LOGGER.info("maven-docker-push-extension is active");
    }

    @Override
    public void onEvent(Object event) throws Exception {
        if (event instanceof ExecutionEvent) {
            handleExecutionEvent((ExecutionEvent) event);
        }
    }

    private void handleExecutionEvent(ExecutionEvent event) {
        ExecutionEvent.Type eventType = event.getType();
        switch (eventType) {
            case ProjectDiscoveryStarted:
                break;
            case SessionStarted:
                sessionStarted(event);
                break;
            case SessionEnded:
//                    if ( this.failure )
//                    {
//                        LOGGER.warn( "The Maven Deployer Extension will not be called based on previous errors." );
//                    }
//                    else
//                    {
//                        sessionEnded( executionEvent );
//                    }
                break;
            case ForkFailed:
            case ForkedProjectFailed:
            case MojoFailed:
            case ProjectFailed:
                // TODO: Can we find out more about the cause of failure?
//                    LOGGER.debug( "Some failure has occured." );
//                    this.failure = true;
                break;

            case ForkStarted:
            case ForkSucceeded:
            case ForkedProjectStarted:
            case ForkedProjectSucceeded:
            case MojoStarted:
            case MojoSucceeded:
            case MojoSkipped:
            case ProjectStarted:
            case ProjectSucceeded:
            case ProjectSkipped:
                break;

            default:
                LOGGER.error("handleExecutionEvent: {}", eventType);
                break;
        }
    }

    private void sessionStarted(ExecutionEvent event) {
        if(containsLifeCyclePluginGoals(event, "io.fabric8", "docker-maven-plugin", "push")) {
            LOGGER.info("Find docker-maven-plugin");
            System.out.println("Find docker-maven-plugin");
        }

    }

    private boolean containsLifeCyclePluginGoals(ExecutionEvent executionEvent, String groupId, String artifactId,
                                                 String goal) {

        boolean result = false;
        List<MavenProject> sortedProjects = executionEvent.getSession().getProjectDependencyGraph().getSortedProjects();
        for (MavenProject mavenProject : sortedProjects) {
            List<Plugin> buildPlugins = mavenProject.getBuildPlugins();
            for (Plugin plugin : buildPlugins) {
                if (groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId())) {
                    List<PluginExecution> executions = plugin.getExecutions();
                    for (PluginExecution pluginExecution : executions) {
                        if (pluginExecution.getGoals().contains(goal)) {
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

}
