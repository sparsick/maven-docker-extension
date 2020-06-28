package com.github.sparsick.maven.docker.extension;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.xml.Xpp3Dom;
import org.apache.maven.shared.utils.xml.Xpp3DomBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Named
public class DockerPushExtension extends AbstractEventSpy {

    private final Logger LOGGER = LoggerFactory.getLogger(DockerPushExtension.class);
    private Set<String> dockerImageNames = new HashSet<>();
    private boolean projectFailed = false;
    private DockerClient dockerClient;

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
                if (this.projectFailed) {
                        LOGGER.warn( "The Maven Docker Push Extension will not be called based on previous errors." );
                } else {
                    sessionEnded( event );

                }
                break;
            case ForkFailed:
            case ForkedProjectFailed:
            case MojoFailed:
            case ProjectFailed:
                this.projectFailed = true;
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

    private void sessionEnded(ExecutionEvent event) {
        if (goalsContain(event, "deploy") && !dockerImageNames.isEmpty()) {
            LOGGER.info("Starting pushing docker images...");
            dockerClient = new DockerClient(System.getProperty("docker.push.registry"));
            dockerImageNames.forEach( image -> dockerClient.pushDockerImage(image));
        }
    }

    private boolean goalsContain( ExecutionEvent event, String goal ) {
        return event.getSession().getGoals().contains( goal );
    }

    private void sessionStarted(ExecutionEvent event) {
        if (containsLifeCyclePluginGoals(event, "io.fabric8", "docker-maven-plugin", "push")) {
            removePluginFromLifeCycle(event, "io.fabric8", "docker-maven-plugin", "push");
        }

    }


    private boolean containsLifeCyclePluginGoals(ExecutionEvent executionEvent, String groupId, String artifactId,
                                                 String goal) {
        List<MavenProject> sortedProjects = executionEvent.getSession().getProjectDependencyGraph().getSortedProjects();

        boolean foundgivenPluginGoal = sortedProjects.stream()
                .flatMap(mavenProject -> mavenProject.getBuildPlugins().stream())
                .filter(plugin -> groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId()))
                .flatMap(plugin -> plugin.getExecutions().stream())
                .anyMatch(pluginExecution -> pluginExecution.getGoals().contains(goal));

        if(foundgivenPluginGoal) {
            LOGGER.info("Found {}:{}:{}", groupId, artifactId, goal);
        }
        return foundgivenPluginGoal;
    }

    private void removePluginFromLifeCycle(ExecutionEvent executionEvent, String groupId, String artifactId,
                                           String goal) {

        List<MavenProject> sortedProjects = executionEvent.getSession().getProjectDependencyGraph().getSortedProjects();

        List<PluginExecution> foundPluginExecution = sortedProjects.stream()
                .flatMap(mavenProject -> mavenProject.getBuildPlugins().stream())
                .filter(plugin -> {
                    LOGGER.debug("Plugin: " + plugin.getId());
                    plugin.getExecutions().forEach(pluginExecution -> LOGGER.debug("  -> " + pluginExecution.getGoals()));
                    return groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId());
                })
                .flatMap(plugin -> plugin.getExecutions().stream())
                .collect(Collectors.toList());

        if(!foundPluginExecution.isEmpty()){
            LOGGER.warn(groupId + ":" + artifactId + ":" + goal + " has been deactivated.");
            foundPluginExecution.forEach(pluginExecution -> {
                parseDockerImageName(pluginExecution.getConfiguration());
                pluginExecution.removeGoal(goal);
            });
        }
    }

    private void parseDockerImageName(Object configuration) {
        LOGGER.debug("Configuration: {}", configuration);

        // this is necessary because cast throw an exception "incompatible type"
        Xpp3Dom dom = Xpp3DomBuilder.build(new StringReader(configuration.toString()));
        dockerImageNames.addAll(Arrays.stream(dom.getChild("images").getChildren("image")).map(image -> image.getChild("name").getValue()).collect(Collectors.toSet()));
        LOGGER.info("Found docker image names: {}", dockerImageNames);
    }


}
