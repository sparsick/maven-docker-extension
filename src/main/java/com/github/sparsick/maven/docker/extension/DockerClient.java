package com.github.sparsick.maven.docker.extension;

import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;


public class DockerClient implements Closeable {


    private final String registryAddress;
    private com.github.dockerjava.api.DockerClient dockerClient;

    public DockerClient(String registryAddress) {
        this.registryAddress = registryAddress;
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        dockerClient = DockerClientImpl.getInstance(config, httpClient);
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
