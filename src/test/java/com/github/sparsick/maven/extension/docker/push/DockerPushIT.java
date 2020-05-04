package com.github.sparsick.maven.extension.docker.push;

import com.soebes.itf.jupiter.extension.MavenIT;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import static org.assertj.core.api.Assertions.assertThat;

@MavenIT(goals = "deploy")
public class DockerPushIT {

    @MavenTest
    void no_extension_is_set(MavenExecutionResult result){

        assertThat(true).isTrue();
    }

    @MavenTest
    void extension_is_set(MavenExecutionResult result){
        assertThat(true).isTrue();
    }
}
