package com.github.sparsick.maven.extension.docker.push;

import kong.unirest.Unirest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DockerClientTest {

    @Container //TODO remove fix port
    private FixedHostPortGenericContainer registry = new FixedHostPortGenericContainer("registry:2")
            .withFixedExposedPort(5001, 5000);

    @Test
    void pushDockerImage() {
        DockerClient clientUnderTest = new DockerClient("localhost:5001");
        clientUnderTest.pushDockerImage("user/demo2");

        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");

    }

}