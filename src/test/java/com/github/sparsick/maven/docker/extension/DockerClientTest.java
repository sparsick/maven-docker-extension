package com.github.sparsick.maven.docker.extension;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import kong.unirest.Unirest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.util.Collections;

@Testcontainers
class DockerClientTest {

    @Container //TODO remove fix port
    private FixedHostPortGenericContainer registry = new FixedHostPortGenericContainer("registry:2")
            .withFixedExposedPort(6000, 5000);
    private com.github.dockerjava.api.DockerClient dockerClient;


    @BeforeEach
    void setup() throws InterruptedException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        dockerClient = DockerClientBuilder.getInstance(config).build();

        dockerClient
                .buildImageCmd(new File("src/test/resources/Dockerfile"))
                .withTags(Collections.singleton("user/demo2"))
                .start()
                .awaitCompletion();
    }

    @AfterEach
    void tearDown(){
        dockerClient.removeImageCmd("user/demo2");
    }

    @Test
    void pushDockerImage() {
        DockerClient clientUnderTest = new DockerClient("localhost:6000");
        clientUnderTest.pushDockerImage("user/demo2");

        Object repositories = Unirest.get("http://localhost:6000/v2/_catalog")
                .asJson()
                .mapBody(node -> node.getObject().get("repositories"));
        Assertions.assertThat(repositories).asString().contains("user/demo2");

    }

}