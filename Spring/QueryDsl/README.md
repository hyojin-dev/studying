# 인프런 김영한님의 '실전! Querydsl' 을 학습한 프로젝트

# 기본 문법
## 기본 Q-Type
### Q 클래스 인스턴스를 사용하는 2가지 방법

    QMember qMember = new QMember("m"); //별칭 직접 지정
    QMember qMember = QMember.member; //기본 인스턴스 사용

> Qmember.member 를 static import 하여 사용 권장

## 검색 조건 쿼리
## JPQL 이 제공하는 모든 검색 조건


    member.username.eq("member1") // username = 'member1'
    member.username.ne("member1") //username != 'member1'
    member.username.eq("member1").not() // username != 'member1'
    member.username.isNotNull() //이름이 is not null
    member.age.in(10, 20) // age in (10,20)
    member.age.notIn(10, 20) // age not in (10, 20)
    member.age.between(10,30) //between 10, 30
    member.age.goe(30) // age >= 30
    member.age.gt(30) // age > 30
    member.age.loe(30) // age <= 30
    member.age.lt(30) // age < 30
    member.username.like("member%") //like 검색
    member.username.contains("member") // like ‘%member%’ 검색
    member.username.startsWith("member") //like ‘member%’ 검색
    ...

- where() 에 파라미터로 검색조건을 추가하면 AND 조건이 추가됨
  이 경우 null 값은 무시
> 검색 조건은 .and()와 .or()를 메서드 체인으로 연결할 수 있다.

## 결과 조회
- fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
- fetchOne() : 단 건 조회
    1. 결과가 없으면 : null
    2. 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
- fetchFirst() : limit(1).fetchOne()
- fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
- fetchCount() : count 쿼리로 변경해서 count 수 조회

        
        //List
        List<Member> fetch = queryFactory
        .selectFrom(member)
        .fetch();
    
        //단 건
        Member findMember1 = queryFactory
        .selectFrom(member)
        .fetchOne();
    
        //처음 한 건 조회
        Member findMember2 = queryFactory
        .selectFrom(member)
        .fetchFirst();
    
        //페이징에서 사용
        QueryResults<Member> results = queryFactory
        .selectFrom(member)
        .fetchResults();
    
        //count 쿼리로 변경
        long count = queryFactory
        .selectFrom(member)
        .fetchCount();

## 정렬

    List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.age.eq(100))
    .orderBy(member.age.desc(), member.username.asc().nullsLast())
    .fetch();

- desc() , asc() : 일반 정렬
- nullsLast() , nullsFirst() : null 데이터 순서 부여

## 페이징

    QueryResults<Member> queryResults = queryFactory
    .selectFrom(member)
    .orderBy(member.username.desc())
    .offset(1)
    .limit(2)
    .fetchResults();
    
    queryResults.getTotal());//전체 데이터 수
    queryResults.getLimit());//몇개로 제한할 지
    queryResults.getOffset());//offset: 몇번 째 부터
    queryResults.getResults().size());//페이징 결과의 데이터 수


- fetchResults() 를 사용할 경우 count 쿼리가 먼저 나간다.

> 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두
조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면,
count 전용 쿼리를 별도로 작성해야 한다

## 집합
### 집합 함수
    
    List<Tuple> result = queryFactory
    .select(member.count(),
    member.age.sum(),
    member.age.avg(),
    member.age.max(),
    member.age.min())
    .from(member)
    .fetch();

### groupBy(), having() 예시

     …
    .groupBy(item.price)
    .having(item.price.gt(1000))
    …    

## 조인 - 기본 조인
- 조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고, 두 번째 파라미터에 별칭(alias)으로 사용할
  Q 타입을 지정하면 된다


    join(조인 대상, 별칭으로 사용할 Q타입)

### 기본 조인

     List<Member> result = queryFactory
    .selectFrom(member)
    .join(member.team, team)
    .where(team.name.eq("teamA"))
    .fetch();

- join() , innerJoin() : 내부 조인(inner join)
- leftJoin() : left 외부 조인(left outer join)
- rightJoin() : rigth 외부 조인(rigth outer join)

## 세타 조인
- 연관관계가 없는 필드로 조인


      List<Member> result = queryFactory
      .select(member)
      .from(member, team)
      .where(member.username.eq(team.name))
      .fetch();

- from 절에 여러 엔티티를 선택해서 세타 조인
- 외부 조인 불가능 다음에 설명할 조인 on을 사용하면 외부 조인 가능

## 조인 - 페치조인

    Member findMember = queryFactory
    .selectFrom(member)
    .join(member.team, team).fetchJoin()
    .where(member.username.eq("member1"))
    .fetchOne();

### 사용방법
- join(), leftJoin() 등 조인 기능 뒤에 fetchJoin() 이라고 추가하면 된다.

## 서브 쿼리
### 나이가 평균 나이 이상인 회원 조회

    List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.age.goe(
    JPAExpressions
    .select(memberSub.age.avg())
    .from(memberSub)
    ))
    .fetch();

### select 절에 subquery

    List<Tuple> fetch = queryFactory
    .select(member.username,
    JPAExpressions
    .select(memberSub.age.avg())
    .from(memberSub)
    ).from(member)
    .fetch();

- 서브쿼리를 사용할때는 JpaExpressions 를 사용하자 

> from 절의 서브쿼리 한계<br>
JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도
하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.

### from 절의 서브쿼리 해결방안
1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
3. nativeSQL을 사용한다

## Case 문
### 단순한 조건

    List<String> result = queryFactory
    .select(member.age
    .when(10).then("열살")
    .when(20).then("스무살")
    .otherwise("기타"))
    .from(member)
    .fetch();

### 복잡한 조건

    List<String> result = queryFactory
    .select(new CaseBuilder()
    .when(member.age.between(0, 20)).then("0~20살")
    .when(member.age.between(21, 30)).then("21~30살")
    .otherwise("기타"))
    .from(member)
    .fetch();

- new CaseBuilder() 를 사용해 복잡한 Case 문을 작성한다.

## 상수, 문자 더하기
- 상수가 필요하면 Expressions.constant(xxx) 사용

### 상수

    Tuple result = queryFactory
    .select(member.username, Expressions.constant("A"))
    .from(member)
    .fetchFirst();

### 문자 더하기

    String result = queryFactory
    .select(member.username.concat("_").concat(member.age.stringValue()))
    .from(member)
    .where(member.username.eq("member1"))
    .fetchOne();

> member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로
문자로 변환할 수 있다. 이 방법은 ENUM을 처리할 때도 자주 사용한다.

# 중급 문법
## 프로젝션과 결과 반환 - 기본
- 프로젝션: select 대상 지정
### 프로젝션 대상이 하나

    List<String> result = queryFactory
    .select(member.username)
    .from(member)
    .fetch();

- 프로젝션 대상이 하나면 타입을 명화하게 지정할 수 있음
- 프로젝션 대상이 둘 이상이면 튜플이나 DTO 로 조회

### 튜플 조회
- 프로젝션 대상이 둘 이상일 때 사용


    List<Tuple> result = queryFactory
    .select(member.username, member.age)
    .from(member)
    .fetch();

    for (Tuple tuple : result) {
      String username = tuple.get(member.username);
      Integer age = tuple.get(member.age);
      System.out.println("username=" + username);
      System.out.println("age=" + age);
    }

### 프로젝션과 결과 반환 - DTO 조회
#### 프로퍼티 접근 - Setter

    List<MemberDto> result = queryFactory
    .select(Projections.bean(MemberDto.class,
    member.username,
    member.age))
    .from(member)
    .fetch();
  
#### 필드 직접 접근

    List<MemberDto> result = queryFactory
    .select(Projections.fields(MemberDto.class,
    member.username,
    member.age))
    .from(member)
    .fetch();

#### 생성자 사용

    List<MemberDto> result = queryFactory
    .select(Projections.constructor(MemberDto.class,
    member.username,
    member.age))
    .from(member)
    .fetch();
    }

#### 별칭이 다를 때

    List<UserDto> fetch = queryFactory
    .select(Projections.fields(UserDto.class,
    member.username.as("name"),
    ExpressionUtils.as(
    JPAExpressions
    .select(memberSub.age.max())
    .from(memberSub), "age")
    )
    ).from(member)
    .fetch();

- ExpressionUtils.as(source,alias) : 필드나, 서브 쿼리에 별칭 적용

## 프로젝션과 결과 반환 - @QueryProjection

    @Data
    public class MemberDto {
      private String username;
      
      private int age;
      
      public MemberDto() {
      }
      
      @QueryProjection
      public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
      }
    }

### @QueryProjection 활용

    List<MemberDto> result = queryFactory
    .select(new QMemberDto(member.username, member.age))
    .from(member)
    .fetch();

> 이 방법은 컴파일러로 타입을 체크할 수 있으므로 가장 안전한 방법이다. 다만 DTO 에 QueryDSL
어노테이션을 유지해야 하는 점과 DTO 까지 Q 파일을 생성해야 하는 단점이 있다.

## 동적 쿼리 - BooleanBuilder 사용

    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {
      String usernameParam = "member1";
      Integer ageParam = 10;
      List<Member> result = searchMember1(usernameParam, ageParam);
      Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
      BooleanBuilder builder = new BooleanBuilder();
      
      if (usernameCond != null) {
       builder.and(member.username.eq(usernameCond));
      }

      if (ageCond != null) {
        builder.and(member.age.eq(ageCond));
      }

      return queryFactory
      .selectFrom(member)
      .where(builder)
      .fetch();
    }

- BooleanBuilder 사용 시 and, or 를 사용하여 체인 방식으로 사용할 수 있다.

## 동적 쿼리 - Where 다중 파라미터 사용

    @Test
    public void 동적쿼리_WhereParam() throws Exception {
      String usernameParam = "member1";
      Integer ageParam = 10;
      List<Member> result = searchMember2(usernameParam, ageParam);
      Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
      return queryFactory
      .selectFrom(member)
      .where(usernameEq(usernameCond), ageEq(ageCond))
      .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
      return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
     return ageCond != null ? member.age.eq(ageCond) : null;
    }

- where 조건에 null 값은 무시된다.
- 메서드를 다른 쿼리에서도 재활용 할 수 있다.
- 쿼리 자체의 가독성이 높아진다.

## 수정, 삭제 벌크 연산
### 수정 예시

    long count = queryFactory
    .update(member)
    .set(member.username, "비회원")
    .where(member.age.lt(28))
    .execute();
    
    em.flush();
    em.clear();

> 주의: JPQL 배치와 마찬가지로, 영속성 컨텍스트에 있는 엔티티를 무시하고 실행되기 때문에 배치 쿼리를
실행하고 나면 영속성 컨텍스트를 초기화 하는 것이 안전하다.

## 햇갈리는 클래스
> JPAExpressions: 서브 쿼리<br>
Expressions: 상수, SQL function 사용<br>
Projections: 프로젝션을 DTO 반환할 때 사용<br>
ExpressionUtils: 서브쿼리에 별칭을 줄 떄 사용<br>
BooleanBuilder: 동적 쿼리의 파라미터 사용<br>
BooleanExpression: Where 다중 파라미터 시 메서드의 반환 값<br>

## SQL function 호출하기
### member >> M으로 변경하는 replace 함수 사용

    String result = queryFactory
    .select(Expressions.stringTemplate("function('replace', {0}, {1},
    {2})", member.username, "member", "M"))
    .from(member)
    .fetchFirst();

> SQL function 은 JPA 와 같이 Dialect 에 등록된 내용만 호출할 수 있다.

# 실무 활용
## Querydsl 리포지토리
### 사용자 정의 리포지토리 구성
![repository](https://user-images.githubusercontent.com/66157892/149620800-8be33b57-c4a7-47f6-b451-2f54600896c1.PNG)

###사용자 정의 리포지토리 사용법
1. 사용자 정의 인터페이스 작성
2. 사용자 정의 인터페이스 구현
3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속

## 스프링 데이터 페이징과 Querydsl 페이징 연동


    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition,
    Pageable pageable) {

        QueryResults<MemberTeamDto> results = queryFactory
        .select(new QMemberTeamDto(
            member.id,
            member.username,
            member.age,
            team.id,
            team.name))
        .from(member)
        .leftJoin(member.team, team)
        .where(usernameEq(condition.getUsername()),
            teamNameEq(condition.getTeamName()),
            ageGoe(condition.getAgeGoe()),
            ageLoe(condition.getAgeLoe()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

- fetchResults() 를 사용할 경우 카운트쿼리 까지 나간다.
- Pageable 객체를 파라미터로 받은 후 where 절 이후
  offset(pageable.getOffset()), limit(pageable.getPageSize()) 메서드를 사용해 페이징 처리
  

## count 쿼리 최적화

    JPAQuery<Member> countQuery = queryFactory
    .select(member)
    .from(member)
    .leftJoin(member.team, team)
    .where(usernameEq(condition.getUsername()),
        teamNameEq(condition.getTeamName()),
       ageGoe(condition.getAgeGoe()),
        ageLoe(condition.getAgeLoe()));
   
    return PageableExecutionUtils.getPage(content, pageable,
    countQuery::fetchCount);

- count 쿼리가 생략 가능한 경우 생략해서 처리
- 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
- 마지막 페이지 일 때 (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함)
- PageableExecutionUtils.getPage(content, pageable,
  countQuery::fetchCount) 를 사용하여 카운트 쿼리를 날릴지 말지 계산.
