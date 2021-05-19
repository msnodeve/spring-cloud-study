# Service Discovery 프로젝트 생성

<br><br>

## Branch name
step01/service-discovery

<br><br>

## 1. application.yml 파일 설정
```yml
server:
  port: 8761

spring:
  application:
    name: discoveryservice

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

<br><br>

## 2. EurekaServer 등록하기
> src/main/java/[package]/application.java
> 파일에 Eureka Server 를 등록해줍니다.

```java
package com.msoogle.discoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryserviceApplication.class, args);
    }

}
```

`@EurekaServer` 이 코드에 의해서 프로그램이 자동으로 Eureka 서버의 역할로 스프링 부트로써 사용이 됩니다.

<br><br>

## 실행 방법

![step01-01](./img/step01-01.png)

빨간색 표시된 부분처럼 실행을 할 수 있습니다.

실행을 하면 `Console` 탭이 열리면서 다음과 같이 `application.yml` 파일에 설정해 두었던 포트 번호로 프로젝트가 실행 됩니다.

```
...
2021-05-19 14:39:55.835  INFO 8526 --- [       Thread-9] c.n.e.r.PeerAwareInstanceRegistryImpl    : Changing status to UP
2021-05-19 14:39:55.851  INFO 8526 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8761 (http) with context path ''
2021-05-19 14:39:55.852  INFO 8526 --- [           main] .s.c.n.e.s.EurekaAutoServiceRegistration : Updating port to 8761
2021-05-19 14:39:55.870  INFO 8526 --- [           main] c.m.d.DiscoveryserviceApplication        : Started DiscoveryserviceApplication in 2.739 seconds (JVM running for 3.302)
2021-05-19 14:39:55.870  INFO 8526 --- [       Thread-9] e.s.EurekaServerInitializerConfiguration : Started Eureka Server
2021-05-19 14:40:55.838  INFO 8526 --- [a-EvictionTimer] c.n.e.registry.AbstractInstanceRegistry  : Running the evict task with compensationTime 0ms
```

그리고 웹 브라우저에서 http://localhost:8761 또는 http://127.0.0.1:8761 로 접속하시게 되면 Eureka 서버가 실행된 모습을 확인할 수 있습니다.

![step01-02](./img/step01-02.png)