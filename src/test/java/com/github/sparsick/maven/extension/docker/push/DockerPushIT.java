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
                systemProperties = {"docker.push.registry=localhost:5001", "maven.deploy.skip"})
    void no_extension_is_set(MavenExecutionResult result){
        assertThat(result).isSuccessful();

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo");
    }

    @MavenTest
    void extension_is_set(MavenExecutionResult result){
        assertThat(result).isSuccessful();
    }
}
