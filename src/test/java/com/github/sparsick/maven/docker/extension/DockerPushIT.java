package com.github.sparsick.maven.docker.extension;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import kong.unirest.Unirest;
import org.assertj.core.api.Assertions;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;


@MavenJupiterExtension
@Testcontainers
public class DockerPushIT {

    @Container
    private FixedHostPortGenericContainer registry = new FixedHostPortGenericContainer("registry:2")
            .withFixedExposedPort(6000, 5000);
//            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*listening on [::]:6000.*"))

    private final String REGISTRY_URL = "http://" + registry.getHost() + ":6000/v2/_catalog";

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void no_extension_is_set(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        assertThat(result)
                .out()
                .info()
                .doesNotContain("maven-docker-push-extension is active");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo");
    }

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void extension_is_set(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push",
                        "Starting pushing docker images...")
                .doesNotContain("docker-maven-plugin:0.33.0:push (default) @ docker-push-no-extension");
        assertThat(result)
                .out()
                .warn()
                .contains("io.fabric8:docker-maven-plugin:push has been deactivated.");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");
    }

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void docker_plugin_configuration_outside_execution(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push",
                        "Starting pushing docker images...")
                .doesNotContain("docker-maven-plugin:0.33.0:push (default) @ docker-push-no-extension");
        assertThat(result)
                .out()
                .warn()
                .contains("io.fabric8:docker-maven-plugin:push has been deactivated.");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");
    }

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void configuration_using_properties(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push",
                        "Starting pushing docker images...")
                .doesNotContain("docker-maven-plugin:0.33.0:push (default) @ docker-push-no-extension");
        assertThat(result)
                .out()
                .warn()
                .contains("io.fabric8:docker-maven-plugin:push has been deactivated.");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");
    }

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void extension_is_set_but_no_docker_plugin(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active")
                .doesNotContain("Found io.fabric8:docker-maven-plugin:push",
                        "Starting pushing docker images...");
        assertThat(result)
                .out()
                .warn()
                .doesNotContain("io.fabric8:docker-maven-plugin:push has been deactivated.");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().doesNotContain("user/demo2");
    }

    @MavenTest
    @MavenGoal("install")
    void extension_is_set_docker_plugin_exists_but_no_deploy_goal(MavenExecutionResult result) {
        assertThat(result).isSuccessful();

        assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push")
                .doesNotContain("Starting pushing docker images...");
        assertThat(result)
                .out()
                .warn()
                .contains("io.fabric8:docker-maven-plugin:push has been deactivated.");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().doesNotContain("user/demo2");
    }

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void extension_is_set_but_build_failed(MavenExecutionResult result) {
        assertThat(result).isFailure();

        assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push")
                .doesNotContain("Starting pushing docker images...");
        assertThat(result)
                .out()
                .warn()
                .contains("io.fabric8:docker-maven-plugin:push has been deactivated.");

        Object repositories = Unirest.get(REGISTRY_URL)
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().doesNotContain("user/demo2");
    }

    @MavenTest
    @MavenGoal("deploy")
    @SystemProperty(value = "docker.push.registry", content = "localhost:6000")     //TODO fix port is bad
    @SystemProperty(value = "maven.deploy.skip")
    void extension_is_set_multi_module(MavenExecutionResult result) {
            assertThat(result).isSuccessful();

            assertThat(result)
                .out()
                .info()
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push",
                        "Starting pushing docker images...");
            assertThat(result)
                .out()
                .warn()
                .contains("io.fabric8:docker-maven-plugin:push has been deactivated.");

            Object repositories = Unirest.get(REGISTRY_URL)
                    .asJson()
                    .mapBody(node -> node.getObject().get("repositories"));
            Assertions.assertThat(repositories).asString().contains("user/demo1",  "user/demo2");
    }
    // todo failure during pushing in extension
    // todo push is binded to other goal
}
