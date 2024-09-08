## ChzzkLib
치지직(네이버) 연동을 할 수 있는 Java API입니다.<br>
해당 라이브러리는 [**taromati**](https://github.com/taromati/done-connector)님의 **done-connector**를 개선한 API입니다.<br><br>
**개선된 사항**
- 일반 채팅 & 구독 채팅 연동 가능
- 사용하기 쉽게 수정
- 방송 정보 가져오기 (스트리머 아이디, 스트리머 닉네임, 스트리머 설명, 방송 제목, 방송 종류, 방송 태그, 팔로워 수, 방송 시청자 수, 인증 마크)

## 라이선스
해당 라이브러리의 라이선스는 [**taromati**](https://github.com/taromati/done-connector)님의 **done-connector**의 라이선스와 같습니다.

## 예시
```groovy
repositories {
    maven("https://repo.repsy.io/mvn/lone64/platform")
}

dependencies {
    implementation("me.taromati:chzzklib:1.0.4")
}
```

```java
/*---| 플랫폼 연동 및 소켓 연결 |---*/
final ChzzkAPI api = new ChzzkAPI.ChzzkBuilder().withData("스트리머 아이디").build()
        .addListeners(new ChzzkListener() {
            @Override
            public void onMessageChat(MessageChatEvent e) {
                /*---| 일반 채팅 불러오기 |---*/
            }

            @Override
            public void onDonationChat(DonationChatEvent e) {
                /*---| 후원 채팅 불러오기 |---*/
            }

            @Override
            public void onSubscriptionChat(SubscriptionChatEvent e) {
                /*---| 구독 채팅 불러오기 |---*/
            }
        }).connect();

/*---| 소켓 연결 해제 |---*/
api.disconnect();

/*---| 방송 정보 가져오기 |---*/
final ChzzkLiveInfo liveInfo = ChzzkAPI.getLiveInfo("스트리머 아이디");
```