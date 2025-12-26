# spring-kafka-rolling-upgrade

## how to build and run

- `docker-compose.yml` 를 이용해 local kafka controller, broker 실행 후 테스트 가능
    - controller-1, controller-2, controller-3 모두 container 내부 9093 포트로 통신
    - broker-1, broker-2, broker-3 모두 container 내부 9092 포트로 통신
    - host 기준으로는 broker-1:29093, broker-2:39093, broker-3:49093
    - broker 들의 메모리 할당이 크게 되어 있으므로, docker machine 의 메모리 설정을 충분히 늘려줄 것 (8GB 권장)
