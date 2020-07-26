# completedJPA

이 Repository는 앞으로 jpa에 대한 나름대로의 정리를 위한 공간이 될것이다.

처음 하이버네이트를 접한 것이 대충 2012년도인가였다.

기존에 첫 직장의 솔루션은 지금은 기억도 잘 나지 않는 preparedStatement를 이용해서 개발을 하던 상황이였는데 자체 프레임워크를 만드려는 시도를 회사에서 했었다.

그때 채택했던 것이 하이버네이트였다. 하지만 그 프로젝트는 실패로 돌아갔었는데 이유는 ORM에 대한 이해가 부족했던 것도 있었고 그에 대한 경험들이 많지 않았기 때문이다.

뭔가 심박했지만 당시의 나로써는 전혀 이해를 하지 못했기 때문에 엄청 어렵다고만 느껴졌었고 거의 전무한 상태에서 헤딩하다 결국에는 무산되었던 그 때 경력 선배였던 형동생했던 한 친구는 여기에 매료되었다.

그리고 그 친구의 권유로 구입한 책이 최범균님의 책이었다. 기억으로는 이 책인 바로 ORM의 ~~전설은 아니고~~ 레전드의 시작을 알리지 않았나 싶다.

하지만 회사는 iBatis를 채택하고 이것이 discontiued (그냥 단종)되고 나온 myBatis를 사용했다.

이것도 당시에는 참 신선했었다. DAO가 사라지고 interface만으로 쿼리가 정의된 xml과 매핑해서 실제 비지니스 로직에서 쿼리는 사라지고 개발 속도 역시 이전보다는 훨씬 좋아졌으니깐~

하지만 여전히 데이터를 가져오는 방식은 sql중심으로 생각해야 한다는 것이다.

기억으로는 2015년인거 같은데 그때 김영한님의 [자바 ORM 표준 JPA 프로그래밍]를 보게 되면서 엄청 큰 감명을 받았다.

그렇게 그 다음해에 회사의 협력파트너사의 어떤 프로젝트에서 jpa를 쓰자고 강력하게 나의 의지를 보여주면서 처음 jpa로 프로젝트를 시작했다.

물론 미숙했던 당시의 실력으로는 엄청나게 고통을 받았던 시기였지만 그만큼 성장했다고 생각했는데 그때 같이 했던 선배와 동료, 후배들은 넌더리를 쳤고 결국에는 myBatis..... 

~~미안했지만 지금은 jpa쓰지요???~~

하지만 눈에서 멀어지면 마음에서도 멀어진다고 어느 덧 다시 JPA와 안녕을 고하고 그렇게 몇 년을 myBatis로 개발을 하니 간단한 토이 프로젝트나 간단한 서버 하나 만들려고 해도 이게 여간 귀찮은게 아니다.

그리고 현재 회사에서도 마음 같아서는 knex나 sequelize로 대체하고 싶지만 누군가를 설득한다는 것이 이게 또 만만찮다. 

타입스크립트로의 전환도 고려하는 이 마당에...

ORM도 괜찮은 선택이긴 한데 거대한 레거시라는 공룡앞에서 무릎...~~을 꿇는 것은 추진력을 얻기 위함이다~~

이래선 안되겠다 싶어 몇 달전부터 혼자서 나름대로 하나의 모듈을 조금씩 떼어다가 JPA로 전환하는 작업을 하는데 이게 마치 다시 시작하는 느낌이 들었다.

그래서 결국에는 먼지 묻은 책을 다시 꺼내고 삽질기에 접어들었고 이제는 어느 정도 속도가 붙었다.

이 Repository는 결국 그 삽잘기를 하면서 다시 공부했던 것을 리마인드하기 위한 그리고 나름대로의 정리 차원에서 이어나갈 생각이다.

최종 목표는 JPA, queryDSL를 활용하는 것이다.

커밍~ 쑨~

아참 이것은 김영한님의 책 [자바 ORM 표준 JPA 프로그래밍](http://acornpub.co.kr/book/jpa-programmig)를 토대로 진행된다. 

홍보같지만 개인적인 친분도 없을 뿐더러 만나 뵌 적도 없다. 김영한님은 나를 모를것이다. 

따라서 이 책을 보고 공부하신 분들에게는 크게 도움이 되지 않을 수 있다.

JPA를 처음 접하시는 분들에게는 그래도 어느정도 도움이 되길 희망하면서...

# 진행 상황
[개발전에 환경 잡고 가자](https://github.com/basquiat78/completedJPA/tree/1.enviroment)    

[영속성 컨텍스트](https://github.com/basquiat78/completedJPA/tree/2.persistenceContext)      

[영속성 컨텍스트 두 번째 이야기... 아이참 브랜치 넘버링 잘못했넹..](https://github.com/basquiat78/completedJPA/tree/3-1.persistencContext)    

[엔티티 매핑](https://github.com/basquiat78/completedJPA/tree/4.entityMapping)    

[엔티티 매핑 part2](https://github.com/basquiat78/completedJPA/tree/4-1.entitiyMapping2)    

[기본키 매핑 전략](https://github.com/basquiat78/completedJPA/tree/5.PrimaryKeyMapping)    

[단방향 매핑](https://github.com/basquiat78/completedJPA/tree/6.unary-relation-mapping)     

[양방향 매핑](https://github.com/basquiat78/completedJPA/tree/7.bidirectional-relation-mapping)    

[상속 매핑 및 단순 상속](https://github.com/basquiat78/completedJPA/tree/8.inheritance-mapping)    

[Proxy, Fetch, Cascade](https://github.com/basquiat78/completedJPA/tree/9.proxy-lazy-cascade)    

