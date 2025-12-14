# spring-kafka

## how to build and run

- `docker-compose.yml` 를 이용해 local kafka controller, broker 실행 후 테스트 가능
    - controller-1, controller-2, controller-3 모두 container 내부 9093 포트로 통신
    - broker-1, broker-2, broker-3 모두 container 내부 9092 포트로 통신
    - host 기준으로는 broker-1:29093, broker-2:39093, broker-3:49093
    - broker 들의 메모리 할당이 크게 되어 있으므로, docker machine 의 메모리 설정을 충분히 늘려줄 것 (8GB 권장)

- `src/test/kotlin/com/hyeon9mak/springkafka/SpringKafkaApplicationTests.kt` 파일 참고

<br>

## articles

### 01. kafka producer 는 재발송시 어떤 partition 으로 메세지를 보내는가?

#### 상황 가정
- kafka producer 가 발송한 message 를 kafka broker 가 받았으나, 네트워크 이슈로 ACK 응답 유실.
- producer 는 일정 시간 내에 ACK 응답을 받지 못했으므로, 동일한 message 를 재발송.
  - 이 때, producer 는 동일한 partition(broker) 으로 message 를 재발송 하는가?
  - 혹은 다른 parition(broker) 로 message 를 재발송 할 가능성이 있는가? (Cluster 단위로 메모리를 활용하는가?)

#### 결론
- 동일한 partition 으로 재발송 한다.
- 기본적으로 kafka producer 는 어떤 partition 으로 message 를 보낼지 결정하는 partitioner 를 사용한다.
- kafka 4.1 버전 기준 producer partitioner 는 message key 를 hash 하여 partition 을 결정한다.
  - 재발송 시에도 동일한 key 로 hash 를 수행하므로, 동일한 partition 으로 message 를 재발송 한다.

**partition 3개 설정**

<img width="728" height="242" alt="Image" src="https://github.com/user-attachments/assets/d7099fc8-bd44-49da-9329-bb5a0ddb8d3e" />

**producer partitioner 구현체**

<img width="1222" height="454" alt="Image" src="https://github.com/user-attachments/assets/ff4eb550-89ef-4235-b8f7-df14fb9add0d" />

**동일한 message key 로 계속해서 발송을 진행하는 경우**

<img width="1276" height="556" alt="Image" src="https://github.com/user-attachments/assets/ed082dfe-e756-4e15-8225-5d29447466a2" />

**message key 를 계속 바꾸면서 발송을 진행하는 경우**

<img width="1290" height="548" alt="Image" src="https://github.com/user-attachments/assets/2d386876-c0ee-4124-9314-643e8e5f910c" />
