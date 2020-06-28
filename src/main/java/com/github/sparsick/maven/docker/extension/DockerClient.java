package com.github.sparsick.maven.docker.extension;

import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;

import java.io.Closeable;
import java.io.IOException;


public class DockerClient implements Closeable {


    private final String registryAddress;
    private com.github.dockerjava.api.DockerClient dockerClient;

    public DockerClient(String registryAddress) {
        this.registryAddress = registryAddress;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        // when using docker-java directly
//        dockerClient = DockerClientBuilder.getInstance(config).build();

        // workaround because elder docker-java lib is shaded in testcontainers
        dockerClient = DockerClientImpl.getInstance(config).withDockerCmdExecFactory(new OkHttpDockerCmdExecFactory());
    }

    public void pushDockerImage(String imageName) {
        try {
            String remoteImageName = registryAddress + "/" + imageName;
            //TODO tagname latest replace by better logic
            dockerClient.tagImageCmd(imageName,remoteImageName, "latest" )
                    .exec();
            dockerClient.pushImageCmd(remoteImageName)
                    .withAuthConfig(new AuthConfig().withRegistryAddress(registryAddress))
                    .start()
                    .awaitCompletion();
        } catch (InterruptedException e) {
              e.printStackTrace();
        }


    }

    @Override
    public void close() throws IOException {
        dockerClient.close();
    }
}
