<!-- # Catalogs and Orders Microservice

<br><br>

## Branch name
step04/catalogs-and-orders-microservice

<br><br>

# INDEX -->




# 사용자 조회 구현

## API Gateway 설정

1. application.yml 파일 수정
 
> user-service application 설정 및 로드밸런싱 설정

```yml
...
spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/**
...
```

<br><br>

## User Service, DTO, VO 수정
```java
// UserDto.java

import com.msoogle.userservice.vo.ResponseOrder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserDto {
    private String email;
    private String name;
    private String pwd;
    private String userId;
    private Date createdAt;

    private String encryptedPwd;

    private List<ResponseOrder> orders;
}
```

```java
// ResponseOrder.java

import lombok.Data;

import java.util.Date;

@Data
public class ResponseOrder {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
    private Date createdAt;

    private String orderId;
}
```

```java
// ResponseUser.java

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUser {
    private String email;
    private String name;
    private String userId;

    private List<ResponseOrder> orders;
}
```

`@JsonInclude(JsonInclude.Include.NON_NULL)`

> Json 타입으로 요청을 받을 경우 Null로 데이터를 받을 때는 해당 변수는 제외하고 받겠다는 의미를 갖고 있는 어노테이션 입니다.

<br><br>

## 사용자 단일 조회 및 전체 조회 구현

```java
// UserRepository.java

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    UserEntity findByUserId(String userId);
}
```

> JPA를 사용했기 때문에 findByUserId를 다음과 같이 SQL 쿼리문으로 변경해서 수행<br>
> find : SELECT * (UserEntity 컬럼들) FROM UserEntity<br>
> By : WHERE<br>
> UserId : user_id = UserId (String userId 값으로 대체)<br>

```java
// UserService.java

import com.msoogle.userservice.dto.UserDto;
import com.msoogle.userservice.jpa.UserEntity;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserByUserId(String userId);
    Iterable<UserEntity> getUserByAll();
}
```

> createUser 메서드는 이전 시간에 생성해 두었으므로, <br>
> 사용자 단일 조회, 사용자 전체 조회 하는 메서드를 interface에 작성 <br>

```java
// UserServiceImpl.java

import com.msoogle.userservice.dto.UserDto;
import com.msoogle.userservice.jpa.UserEntity;
import com.msoogle.userservice.jpa.UserRepository;
import com.msoogle.userservice.vo.ResponseOrder;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
    UserRepository userRepository;
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // createUser Method...

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        if(userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        List<ResponseOrder> orders = new ArrayList<>();
        userDto.setOrders(orders);

        return userDto;
    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }
}
```

> 비즈니스 로직 구현

```java
// UserController.java

import com.msoogle.userservice.dto.UserDto;
import com.msoogle.userservice.jpa.UserEntity;
import com.msoogle.userservice.service.UserService;
import com.msoogle.userservice.vo.Greeting;
import com.msoogle.userservice.vo.RequestUser;
import com.msoogle.userservice.vo.ResponseUser;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user-service/")
public class UserController {

    private Environment env;
    private UserService userService;

    @Autowired
    private Greeting greeting;

    @Autowired
    public UserController(Environment env, UserService userService) {
        this.env = env;
        this.userService = userService;
    }

    // createUser Method...

    @GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();

        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable String userId) {
        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser resultValue = new ModelMapper().map(userDto, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(resultValue);
    }
}
```

<br><br>

## 실행

1. http://localhost:8000/user-service/users url로 POST 요청
2. Body > raw > JSON 형식으로 데이터 전송
   ```json
   {
    "email": "test@gmail.com",
    "name": "test",
    "pwd": "test1234"
   }
   ```
   ![step04-01.png](./img/step04-01.png)
3. 이외 다른 email로 2개 정도 요청을 보냄
4. http://localhost:8000/user-service/users url로 GET 요청
   ![step04-02.png](./img/step04-02.png)
5. 사용자 단일 요청을 하기 위해 검색된 내용 중 아무 userId를 복사
6. http://localhost:8000/user-service/users/{userId} url로 GET 요청
   ![step04-03.png](./img/step04-03.png)


<br><br><br>

# Catalog MicroServie 생성 및 기능 구현

## 새 프로젝트 생성 및 Dependency 추가

catalog-service 이름으로 프로젝트 생성

- Eureka Discovery Client <br>
- Lombok <br>
- Spring Boot DevTools <br>
- Spring Web <br>
- Spring Data JPA <br>
- H2 Database <br>

![step04-04.png](./img/step04-04.png)

> H2 Database version은 1.3.x 버전으로 변경 <br>
> ModelMapper Dependency 추가 <br>

<br>

## API Gateway 설정

1. application.yml 파일 수정
 
> catalog-service application 설정 및 로드밸런싱 설정

```yml
...
spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      routes:
        - id: catalog-service
          uri: lb://CATALOG-SERVICE
          predicates:
            - Path=/catalog-service/**
...
```

## Catalog Service application.yml 설정

```yml
server:
  port: 0

spring:
  application:
    name: catalog-service
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
      path: /h2-console
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    generate-ddl: true

eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka

logging:
  level:
    com.msoogle.catalogservice: DEBUG
```

<br>

## data.sql 파일 생성

> 본 프로젝트 실행과 동시에 데이터가 입력되어있는 상태로 시작하기 위해 작성 <br>
> `resources/data.sql` 파일 생성

```sql
insert into catalog(product_id, product_name, stock, unit_price) values('CATALOG-001', 'Berlin', 100, 1500);
insert into catalog(product_id, product_name, stock, unit_price) values('CATALOG-002', 'Tokyo', 110, 1000);
insert into catalog(product_id, product_name, stock, unit_price) values('CATALOG-003', 'Stockholm', 120, 2000);
```

<br>

## DTO, VO 파일 생성 및 구현

```java
// CatalogDto.java

import lombok.Data;

import java.io.Serializable;

@Data
public class CatalogDto implements Serializable {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;
}
```

```java
// ResponseCatalog.java

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseCatalog {
    private String productId;
    private String productName;
    private Integer unitPrice;
    private Integer stock;

    private Date createdAt;
}
```

<br>

## JPA Entity, Repository 생성 및 구현

```java
// CatalogEntity.java

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "catalog")
public class CatalogEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private Integer stock;
    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private Date createdAt;
}
```

```java
// CatalogRepository.java

import org.springframework.data.repository.CrudRepository;

public interface CatalogRepository extends CrudRepository<CatalogEntity, Long> {
    CatalogEntity findByProductId(String productId);
}
```

<br>

## Service 생성 및 구현

```java
// CatalogService.java

import com.msoogle.catalogservice.jpa.CatalogEntity;

public interface CatalogService {
    Iterable<CatalogEntity> getAllCatalogs();
}
```

```java
// CatalogServiceImpl.java

import com.msoogle.catalogservice.jpa.CatalogEntity;
import com.msoogle.catalogservice.jpa.CatalogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service
public class CatalogServiceImpl implements CatalogService{

    CatalogRepository catalogRepository;

    @Autowired
    public CatalogServiceImpl(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    @Override
    public Iterable<CatalogEntity> getAllCatalogs() {
        return catalogRepository.findAll();
    }
}
```

<br>

## Controller 생성 및 구현

```java
// CatalogController.java

import com.msoogle.catalogservice.jpa.CatalogEntity;
import com.msoogle.catalogservice.service.CatalogService;
import com.msoogle.catalogservice.vo.ResponseCatalog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catalog-service")
public class CatalogController {
    Environment env;
    CatalogService catalogService;

    @Autowired
    public CatalogController(Environment env, CatalogService catalogService) {
        this.env = env;
        this.catalogService = catalogService;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Catalog Service on PORT %s", env.getProperty("local.server.port"));
    }

    @GetMapping("/catalogs")
    public ResponseEntity<List<ResponseCatalog>> getCatalogs() {
        Iterable<CatalogEntity> catalogList = catalogService.getAllCatalogs();

        List<ResponseCatalog> result = new ArrayList<>();

        catalogList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseCatalog.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
```

<br><br>

## 실행

http://localhost:8000/catalog-service/catalogs url로 GET 요청
![step04-05.png](./img/step04-05.png)