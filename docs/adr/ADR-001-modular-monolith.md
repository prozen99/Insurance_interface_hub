# ADR-001: Modular Monolith 선택

## 상태

Accepted

## 배경

Insurance Interface Hub는 REST, SOAP, MQ, SFTP, FTP, Batch처럼 성격이 다른 여러 인터페이스 프로토콜을 다룹니다. 장기적으로는 프로토콜별 서비스나 worker로 분리할 수 있지만, 포트폴리오 초기 단계에서는 과도한 분산 구조보다 하나의 애플리케이션 안에서 업무 경계와 실행 흐름을 명확히 보여주는 것이 더 중요했습니다.

## 결정

현재 구조는 single Spring Boot application을 유지하고, package boundary로 modular monolith를 구성합니다.

주요 module boundary:

- `admin`
- `interfacehub`
- `protocol.rest`
- `protocol.soap`
- `protocol.mq`
- `protocol.sftp`
- `protocol.ftp`
- `protocol.filetransfer`
- `protocol.batch`
- `monitoring`
- `audit`
- `common`

공통 실행 엔진은 `interfacehub`에 두고, 각 프로토콜은 `InterfaceExecutor` contract를 구현합니다.

## 이유

- local MySQL과 IntelliJ만으로 실행 가능한 데모를 만들 수 있다.
- 면접에서 전체 구조와 실행 흐름을 한 repository 안에서 설명하기 쉽다.
- transaction, schema, security, UI, monitoring을 하나의 맥락으로 보여줄 수 있다.
- protocol module이 독립적인 package로 나뉘어 있어 나중에 service 분리 가능성을 남긴다.

## 결과

장점:

- 실행과 디버깅이 단순하다.
- Flyway schema 관리가 한 곳에 모인다.
- Thymeleaf admin UI와 backend flow를 함께 이해하기 쉽다.
- 포트폴리오 평가자가 빠르게 기능을 실행해 볼 수 있다.

단점:

- 배포 단위가 하나로 묶인다.
- 운영 규모가 커지면 protocol별 scaling이 어렵다.
- Batch, MQ, file transfer처럼 runtime 성격이 다른 기능이 같은 process에 있다.

## 향후 전환 기준

다음 조건이 생기면 service 분리를 검토합니다.

- 특정 protocol workload가 독립 scaling을 필요로 한다.
- Batch나 MQ worker를 admin web process와 분리해야 한다.
- 외부 broker, scheduler, file transfer server가 운영 인프라로 고도화된다.
- 팀 경계가 protocol별로 분리되고 배포 주기가 달라진다.
