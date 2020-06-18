package com.github.sparsick.maven.extension.docker.push;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
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
            .withFixedExposedPort(5001, 5000);
//            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*listening on [::]:5000.*"))


    @MavenTest(goals = "deploy",
                systemProperties = {"docker.push.registry=localhost:5001", "maven.deploy.skip"}) //TODO fix port is bad
    void no_extension_is_set(MavenExecutionResult result){
        assertThat(result).isSuccessful();

        Assertions.assertThat(assertThat(result).log().info())
            .doesNotContain("maven-docker-push-extension is active");

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo");
    }

    @MavenTest(goals = "deploy",
        systemProperties = {"docker.push.registry=localhost:5001", "maven.deploy.skip"}) //TODO fix port is bad
    void extension_is_set(MavenExecutionResult result){
        assertThat(result).isSuccessful();

        // io.fabric8:docker-maven-plugin:push has been deactivated.
        Assertions.assertThat(assertThat(result).log().info())
            .contains("maven-docker-push-extension is active",
                    "Found io.fabric8:docker-maven-plugin:push",
                    "Starting pushing docker images...")
            .doesNotContain("docker-maven-plugin:0.33.0:push (default) @ docker-push-no-extension");

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
            .asJson()
            .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");
    }

    @MavenTest(goals = "deploy",
            systemProperties = {"docker.push.registry=localhost:5001", "maven.deploy.skip"}) //TODO fix port is bad
    void docker_plugin_configuration_outside_execution(MavenExecutionResult result){
        assertThat(result).isSuccessful();

        //  io.fabric8:docker-maven-plugin:push has been deactivated.
        Assertions.assertThat(assertThat(result).log().info())
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push",
                        "Starting pushing docker images...")
                .doesNotContain("docker-maven-plugin:0.33.0:push (default) @ docker-push-no-extension");

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");
    }

    @MavenTest(goals = "deploy",
            systemProperties = {"maven.deploy.skip"}) //TODO fix port is bad
    void extension_is_set_but_no_docker_plugin(MavenExecutionResult result){
        assertThat(result).isSuccessful();

        Assertions.assertThat(assertThat(result).log().info())
                .contains("maven-docker-push-extension is active")
                .doesNotContain("Found io.fabric8:docker-maven-plugin:push",
                                "Starting pushing docker images...");

        // io.fabric8:docker-maven-plugin:push has been deactivated. doesnotcontain

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().doesNotContain("user/demo2");
    }

    @MavenTest(goals = "install") //TODO fix port is bad
    void extension_is_set_docker_plugin_exists_but_no_deploy_goal(MavenExecutionResult result){
        assertThat(result).isSuccessful();

//        " io.fabric8:docker-maven-plugin:push has been deactivated."
        Assertions.assertThat(assertThat(result).log().info())
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push")
                .doesNotContain("Starting pushing docker images...");

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().doesNotContain("user/demo2");
    }

    @MavenTest(goals = "deploy",
            systemProperties = {"docker.push.registry=localhost:5001", "maven.deploy.skip"}) //TODO fix port is bad
    void extension_is_set_but_build_failed(MavenExecutionResult result){
        assertThat(result).isFailure();

        Assertions.assertThat(assertThat(result).log().info())
                .contains("maven-docker-push-extension is active",
                        "Found io.fabric8:docker-maven-plugin:push")
//                        "The Maven Docker Push Extension will not be called based on previous errors.")
                .doesNotContain("Starting pushing docker images...");

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().doesNotContain("user/demo2");
    }



    // todo failure during pushing in extension
    // todo push is binded to other goal
}
