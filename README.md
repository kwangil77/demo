# Demo
Demo 는 HTTP 요청에 대한 응답으로 Access Key Pair 를 생성한지 N 시간을 초과하는 IAM User 의 User ID 와 Access Key ID 를 반환합니다.

## Docker Registry 가 없는 로컬 환경에 배포할 경우
Demo 는 `Java 17` 과 `Spring Boot 3.1.4` 을 사용해 개발되었고, 로컬 환경에서의 빌드 및 배포는 다음 과정으로 진행합니다.
- 로컬에 [Colima](https://github.com/abiosoft/colima) 를 이용해 docker + k3s 환경을 만들고 Docker Registry 없이 빌드된 이미지를 사용해 배포합니다.
- 제공되는 [manifest](manifest) 는 위 환경을 기준으로 작성되었지만 일부 과정을 추가하면 일반 K8s 환경에 배포 가능합니다.

### 사전 준비
로컬에 [Colima](https://github.com/abiosoft/colima) 와 [Docker]() 를 설치해 빌드 및 배포 환경을 만듭니다.
```bash
$ brew install colima docker
$ sudo ln -sf $HOME/.colima/default/docker.sock /var/run/docker.sock
$ colima start --runtime docker --kubernetes
```

### AWS Credentials 설정
AWS Credentials 설정은 다양한 방법이 있지만 Access Key Pair 가 제공되는 것을 전제로 아래에 적절히 설정합니다.
<br />
[manifest/secret.yaml](manifest/secret.yaml)
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: demo
  labels:
    app: demo
type: Opaque
stringData:
  AWS_ACCESS_KEY_ID: ''
  AWS_SECRET_ACCESS_KEY: ''
```

### Container 이미지 빌드 및 배포
- Git 저장소에서 로컬 환경으로 소스를 클론합니다.
- 설정된 Dockerfile 을 이용해 Container 이미지를 빌드합니다.
- 로컬 K8s 환경에 제공된 manifest 를 배포합니다.
```bash
$ git clone -b main https://github.com/kwangil_ha/demo.git
$ cd demo
$ docker build -t demo .
$ cd manifest
$ kubectl apply -f deployment.yaml -f secret.yaml -f service.yaml -f serviceaccount.yaml
```

## Docker Registry 가 있고 일반 K8s 환경에 배포할 경우
아래 `image` 설정을 Docker Registry 에 맞게 적절히 변경하고 `imagePullPolicy` 설정도 적절히 설정합니다. 필요에 따라 `Ingress` 등을 [manifest](manifest) 에 추가 후 배포할 수 있습니다.
<br />
[manifest/deployment.yaml](manifest/deployment.yaml)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo
  labels:
    app: demo
spec:
  selector:
    matchLabels:
      app: demo
  template:
    metadata:
      labels:
        app: demo
    spec:
      serviceAccountName: demo
      containers:
        - name: app
          image: demo
          imagePullPolicy: Never
```
### Container 이미지 빌드 후 
- Container 를 빌드 후 Docker Registry 에 Push 합니다.
- 일반 K8s 환경에 변경된 manifest 를 배포합니다.

## HTTP 요청해 보기
`elapsedHoursOfAccessKey` 쿼리 인수가 있을 경우 해당 값을, 없을 경우 기본 N 값을 기준으로 IAM User 목록을 반환한다.
- `kubectl` 을 이용해 로컬에서 접근 가능하도록 한다.
- `curl` 을 이용해 HTTP 요청에 대한 응답을 확인한다.
```bash
$ kubectl port-forward services/demo 8080:8080
$ curl -s -H 'Accept: application/json' 'http://localhost:8080/iam/users?elapsedHoursOfAccessKey=19900'
$ curl -s -H 'Accept: application/json' 'http://localhost:8080/iam/users'
```
### 설정 파일 내 N 값 변경이 필요할 경우
기본 N 값은 아래 설정에서 확인 가능하며 적절히 변경할 수 있습니다.
<br />
[src/main/resources/application.yml](src/main/resources/application.yml)
```yaml
aws:
  iam:
    user:
      accessKey:
        elapsedHours: 10000
```
### 환경 변수로 N 값 설정이 필요할 경우
아래와 같이 환경 변수를 설정할 경우 설정 파일 내 기본 N 값을 대체합니다.
<br />
[manifest/deployment.yaml](manifest/deployment.yaml)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo
  labels:
    app: demo
spec:
  selector:
    matchLabels:
      app: demo
  template:
    metadata:
      labels:
        app: demo
    spec:
      serviceAccountName: demo
      containers:
        - name: app
          image: demo
          env:
            - name: AWS_IAM_USER_ACCESSKEY_ELAPSEDHOURS
              value: '15000'
```