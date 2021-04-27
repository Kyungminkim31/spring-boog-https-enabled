# 스프링부트와 HTTPS 를 사용한 Self-Signed 인증서

## 원문

[바엘덩 원문 보기](https://www.baeldung.com/spring-boot-https-self-signed-certificate)

## 1. 개요

이 예제는 스프링부트에서 HTTPS를 사용하는 방법에 대하여 설명합니다. 예제를 위하여 Self-signed Certificate를 생성하고 간단한 어플리케이션을 통해 설정해보도록 하겠습니다. 

아래의 링크를 통해 여러가지의 스프링부트의 자세한 자료들을 참고 하실수 있습니다.

[링크](https://www.baeldung.com/spring-boot)

## 2. Self-Singed Certificate 생성하기

시작하기전에 먼저 Self-Signed Certificate 를 생성해야 합니다. 아래의 두가지 방법중에 선택하여 생성할수 있습니다.

* PKCS12: Public Key Cryptographic Standards(공개키를 이용한 암호화 표준방식) 다수의 인증서와 키를 담을 수 있는 포맷으로 보호된 패스워드 입니다. 산업계에서 현재 널리 사용되고 있습니다.
* JKS: Java KeyStore는 PKCS12; 와 비슷합니다. 자바환경 내에서 제한적이면서 가장 어울리는 형태의 포맷입니다.

커맨드라인 인터페이스 상에서 OpenSSL 또는 Keytool을 사용하여 인증서들을 생성할수 있습니다. [KeyTool](https://docs.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html) 은 Java Runtime Environment 와 함께 제공되며 OpenSSL은 [이곳](https://www.openssl.org/)을 통해 다운로드 받을 수 있습니다.

준비가 되었다면 한번 시작해보겠습니다!

### 2.1 Keystore 생성하기

암호된 키들의 조합들을 생성하고 키 저장소(Keystore) 저장을 해보록 합니다.

아래의 명령어를 사용하여 PKCS12 키스토어 포맷을 생성하도록 합니다.

```shell
keytool -genkeypair -alias gaval -keyalg RSA -keysize 2048 -storetyp
e PKCS12 -keystore gaval.p12 -validity 3650
```

동일한 키스토어 내 유일한 앨리어스로 구분되는 키쌍들을 저장할수 있습니다. 위의 명령어를 입력하게 된다면 패스워드를 비롯하여 인증서 내에 저장될 정보들을 입력을 요구 받게 됩니다. 필요한 정보들을 입력하고 마지막에 확인으로 yes를 입력하게 되면 생성된 키가 키저장소에 저장되게 됩니다.

 ```shell
Enter keystore password
What is your first and last name?
  [Kyungmin Kim]:  Kyungmin Kim
What is the name of your organizational unit?
  [Gaval]:  Gaval
What is the name of your organization?
  [Gaval]:  Gaval
What is the name of your City or Locality?
  [Seoul]:  Hanam
What is the name of your State or Province?
  [Kyunggi]:  Kyunggi
What is the two-letter country code for this unit?
  [kk]:  KK
Is CN=Kyungmin Kim, OU=Gaval, O=Gaval, L=Hanam, ST=Kyunggi, C=KK correct?
  [no]:  yes
 ```

위와 같이 예제로 입력할 수 있습니다.

만약 JKS 포맷으로 입력하고 싶다면 아래와 같이 입력합니다.

```shell
keytool -genkeypair -alias gaval -keyalg RSA -keysize 2048 -keystroe gaval.jks -validity 3650
```

위와 같이 JKS 포맷을 생성할수 있지만, 산업표준 포맷인 PKCS12를 사용할 것을 추천합니다. 만약 이미 JKS 키저장소를 가지고 있다면 아래의 명령어를 통해 PKCS12 형식으로 변환할수 있습니다. 

```shell
keytool -importkeystore -srckeystore gaval.jks - destkeystore gaval.p12 -deststoretype pkcs12
```

추후 생성된 키스토어의 패스워드와 새로 생성한 패스워드등은 나중에 사용해야 하니 앨리어스와 키저장소의 패스워드는 추후 필요하므로 꼭 기억해 놓도록 합니다.

## 3. 스프링 부트 내에서 HTTPS 활성화 하기

스프링 부트 내에서 다음의 [속성값](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-configure-ssl) `server.ssl.* properties`  를 통해  SSL 에대한 속성들을 선언할수 있습니다.  아래의 샘플 어플리케이션들을 통해 HTTPS 활성화를 설정해보도록 합니다.

먼저, Spring Security 를 의존성으로 가지고 있는 스프링 부트 어플리케이션에서 부터 시작 합니다. 첫번째 엔드포인트는 `/welcome` 입니다.

그리고, 위에서 생성한 `gaval.p12` 인증서 파일을 `src/main/resources/keystore/` 디렉토리내 복사해 놓습니다.

### 3.1 SSL 프로퍼티 설정하기

이제 SSL 관련 속성값들을 설정합니다.

```properties
# 키스토어에서 사용한 포맷 형식, JKS로 설정하였다면 JKS 로 지정
server.ssl.key-store-type=PKCS12
# 인증서를 담고 있는 디렉토리 위치
server.ssl.key-store=classpath:keystore/gaval.p12
# 인증서를 생성할때 만든 패스워드
server.ssl.key-store-password=<위에서-설정한-패스워드>
# 인증서와 매핑되어 있는 앨리어스
server.ssl.key-alias=gaval
```

아래의 속성값 지정을 통해 HTTPS 요청만 처리하도록 아래와 같이 지정합니다.

```properties
server.ssl.enabled=true
```

## 4. HTTPS URL 을 호출하기

위와 작업이 성공적으로 마무리 되었다면 HTTPS 통신을 위한 어플리케이션 설정은 마무리 되었습니다. 지금부터 클라이언트 측면에서 생각해서 HTTPS 엔드포인트로 self-signed certificate 인증서를 가지고 호출을 해보는 법을 살펴보겠습니다.

첫번째로 `trust store` 를 생성합니다. PKCS12 파일을 생성한것처럼 같은 방법으로 trust store 를 생성할수 있습니다. 아래와 같은 속성값들을 지정을 해보도록 합니다.

```properties
# trust store 위치
trust.store=classpath:keystore/gaval.p12
# trust store 패스워드
trust.store.password=<위에서-설정한-패스워드>
```

이제 SSLContext 를 trust store와 함께 준비하고 맞춤 RestTemplate를 생성합니다.

```java
RestTemplate restTemplate() throws Exception {
    SSLContext sslContext = new SSLContextBuilder()
      .loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
      .build();
    SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
    HttpClient httpClient = HttpClients.custom()
      .setSSLSocketFactory(socketFactory)
      .build();
    HttpComponentsClientHttpRequestFactory factory = 
      new HttpComponentsClientHttpRequestFactory(httpClient);
    return new RestTemplate(factory);
}
```

자 이제 만든 앱의 운명을 테스트 해볼 시간입니다. 스프링 시큐리티 설정 내에 들어오는 요청에 대해서 통과되도록 설정합니다.

```java
protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers("/**")
      .permitAll();
}
```

마지막으로 HTTPS 엔드포인트를 호출해 보도록 하겠습니다.

```java
@Test
public void whenGETanHTTPSResource_thenCorrectResponse() throws Exception {
    ResponseEntity<String> response = 
      restTemplate().getForEntity(WELCOME_URL, String.class, Collections.emptyMap());

    assertEquals("<h1>Welcome to Secured Site</h1>", response.getBody());
    assertEquals(HttpStatus.OK, response.getStatusCode());
}
```

## 5.결론

지금까지 self-signed 인증서를 생성하고 HTTPS 를 스프링 부트 어플리케이션에서 활성화 하는 것을 살펴보있습니다. 그리고 마지막으로 HTTPS 활성화된 엔드포인트를 호출하는 것 역시 알아보았습니다.

항상 그러하듯 상기의 코드는  [깃헙저장소](https://github.com/eugenp/tutorials/tree/master/spring-security-modules/spring-security-web-boot-2)에서 찾아 볼수 있습니다.

마지막으로 아래와 같이 시작 클래스 속성값을 pom.xml 값에서 주석해제 해준다면 샘플코드를 실행해볼수 있습니다.

끝-

## 역자 주

상기의 소스에서 하단부의 호출하기의 경우 JUnit 테스트를 통해 테스트를 하는 방법이며, JUnit 테스트 외의 외부에서의 테스트는 하단부의 호출하기를 생략할수 있습니다.

외부에서의 호출은 Httpie 등을 이용하여 아래와 같이 테스트가 가능합니다.

```shell
➜  Project https --verify=no localhost:8080/welcome
HTTP/1.1 200
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 18
Content-Type: text/plain;charset=UTF-8
Date: Tue, 27 Apr 2021 14:22:51 GMT
Expires: 0
Keep-Alive: timeout=60
Pragma: no-cache
Strict-Transport-Security: max-age=31536000 ; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

Hello, HTTPs WORLD
```

역자가 사용한 소스는 아래의 링크에서 확인 가능합니다.

[깃헛저장소 링크가기] ()

> *주의: application.properties 내의 인증서 패스워드와 앨리어스 등은 각자 맞게 커스텀 해야 합니다.*

-진짜 끝.
