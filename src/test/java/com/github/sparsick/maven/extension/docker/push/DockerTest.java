package com.github.sparsick.maven.extension.docker.push;

import kong.unirest.Unirest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DockerTest {

    @Test
    void test(){
        Object repositories = Unirest.get("http://localhost:5001/v2/_catalog")
                                        .asJson()
                                        .mapBody(node -> node.getObject().get("repositories"));
        assertThat(repositories).asString().contains("user/demo");
    }
}
