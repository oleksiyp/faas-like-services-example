# Spring Cloud Stream example

## Article



## How to run?

Install following tools from corresponding websites:

 * docker
 * helm
 * kubectl
 * helmfile
 * k3s with docker support

Clone it:

```
git clone git@github.com:oleksiyp/spring-cloud-stream-example.git
```

Make sure you have docker on your system installed via one of manuals alike https://docs.docker.com/engine/install/ubuntu/
Install k3s with docker support enabled:

```
curl -sfL https://get.k3s.io | sh -s - --docker
```

Change the current working directory to cloned directory and build the project:
```
./gradlew jibDockerBuild
```
Deploy helm charts:

```
helmfile sync
```

Once all parts are deployed you can check installation via `kubectl get pods`:
```
NAME                                                           READY   STATUS    RESTARTS   AGE
wavefront-collector-8lshh            1                          1/1     Running   0          3h10m
wavefront-proxy-c4fd5f48d-5kxtj                                1/1     Running   0          3h10m
keda-operator-metrics-apiserver-78f4f687dd-qq6s2               1/1     Running   0          3h10m
keda-operator-5c569bb794-vnwbf                                 1/1     Running   0          3h10m
rabbitmq-operator-rabbitmq-cluster-operator-59cb6cf6b8-qhrjn   1/1     Running   0          172m
rabbitmq-cluster-server-0                                      1/1     Running   0          171m
streaming-processor2-streaming-service-b9cfd6774-dj5b7         1/1     Running   0          139m
streaming-processor1-streaming-service-5f9c766d7c-ptv2n        1/1     Running   0          139m
streaming-consumer-streaming-service-b756989b-g8wpr            1/1     Running   0          139m
streaming-producer-streaming-service-555c8dd499-c8l9z          1/1     Running   0          139m
streaming-processor1-streaming-service-5f9c766d7c-q729h        1/1     Running   0          101m
streaming-processor2-streaming-service-b9cfd6774-78sqk         1/1     Running   0          101m
streaming-processor2-streaming-service-b9cfd6774-xkdkp         1/1     Running   0          100m
```

*Note: the amount of processor pods might be different, also wavefront is installed depennding if WAVEFRONT_TOKEN exists*

Helmfile installs following things:

 - KEDA operator
 - RabbitMQ cluster operator
 - Wavefront controller (if WAVEFRONT_TOKEN is available)
 - RabbitMQ cluster
 - streaming-producer microservice
 - streaming-consumer microservice
 - streaming-processor1 microservice
 - streaming-processor2 microservice

### Watching changes

To run watch loop to deploy locally:
```
skaffold dev
```

### Wavefront monitoring

Register on `https://tanzu.vmware.com/observability`.

Get token to install Wavefront to Kubernetes cluster
and put it to `WAVEFRONT_TOKEN` environment variable.

Run sync again:

```
helmfile sync
```

