helm repo add wavefront https://wavefronthq.github.io/helm/
helm repo update
kubectl create namespace wavefront
helm install wavefront wavefront/wavefront \
    --set wavefront.url=https://longboard.wavefront.com \
    --set wavefront.token=$WAVEFRONT_TOKEN \
    --set clusterName=streaming-example \
    --namespace wavefront
