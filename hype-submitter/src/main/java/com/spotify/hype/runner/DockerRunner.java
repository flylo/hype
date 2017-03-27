/*-
 * -\-\-
 * Spotify Styx Scheduler Service
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.hype.runner;

import static com.spotify.hype.ContainerEngineCluster.containerEngineCluster;
import static com.spotify.hype.StagedContinuation.stagedContinuation;
import static com.spotify.hype.runner.RunSpec.Secret.secret;
import static com.spotify.hype.runner.RunSpec.runSpec;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.container.Container;
import com.google.api.services.container.ContainerScopes;
import com.google.api.services.container.model.Cluster;
import com.google.common.base.Throwables;
import com.spotify.hype.ContainerEngineCluster;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an interface to the Docker execution environment
 */
public interface DockerRunner {

  Logger LOG = LoggerFactory.getLogger(DockerRunner.class);

  /**
   * Runs a hype execution. Blocks until complete.
   *
   * @param runSpec     Specification of what to run
   * @return Optionally a uri pointing to the gcs location of the return value
   */
  Optional<URI> run(RunSpec runSpec);

  /**
   * A local runner
   *
   * @return A locally operating docker runner
   */
  static DockerRunner local() {
    return new LocalDockerRunner();
  }

  static DockerRunner kubernetes(KubernetesClient kubernetesClient) {
    return new KubernetesDockerRunner(kubernetesClient);
  }

  static void main(String[] args) throws IOException {
    ContainerEngineCluster cluster = containerEngineCluster("datawhere-test", "us-east1-d", "hype-test");
    final KubernetesClient kubernetesClient = createKubernetesClient(cluster);
    final DockerRunner kubernetes = DockerRunner.kubernetes(kubernetesClient);

    final RunSpec runSpec = runSpec(
        "us.gcr.io/datawhere-test/hype-runner:4",
        stagedContinuation(
            URI.create("gs://rouz-test/spotify-hype-staging"),
            "continuation-4857074019514830262.bin"),
        secret("gcp-key", "/etc/gcloud"));

    kubernetes.run(runSpec);
  }

  static KubernetesClient createKubernetesClient(ContainerEngineCluster gkeCluster) {
    try {
      final GoogleCredential credential = GoogleCredential.getApplicationDefault()
          .createScoped(ContainerScopes.all());
      final Container gke = new Container.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
          .setApplicationName("hype")
          .build();

      final Cluster cluster = gke.projects().zones().clusters()
          .get(gkeCluster.project(), gkeCluster.zone(), gkeCluster.cluster())
          .execute();

      final io.fabric8.kubernetes.client.Config kubeConfig = new ConfigBuilder()
          .withMasterUrl("https://" + cluster.getEndpoint())
          .withCaCertData(cluster.getMasterAuth().getClusterCaCertificate())
          .withClientCertData(cluster.getMasterAuth().getClientCertificate())
          .withClientKeyData(cluster.getMasterAuth().getClientKey())
          .build();

      return new DefaultKubernetesClient(kubeConfig);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
