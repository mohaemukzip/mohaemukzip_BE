# mohaemukzip_BE
mohaemukzip_BE_Repo


# 🚀 뭐해먹집?



## 📱 소개

> 반복되는 '뭐해 먹지'를 집밥 루틴으로 바꾸다, 뭐해먹집? 

배포 주소 : https://api.mohaemukzip.site/

---

<br>

## 👥 멤버
| 팀원 1 | 팀원 2 | 팀원 3 | 팀원 4 |
|:------:|:------:|:------:|:------:|

| [조은정](https://github.com/E-hyeon) | [강신욱](https://github.com/shinwokkang) | [김수빈](https://github.com/binsu1222) | [신민정](https://github.com/littby) | 

<br>

## 📆 프로젝트 기간
- 전체 기간: `2025.12.20 - 2025.02.20`
- 개발 기간: `2025.12.20 - 2025.02.20`

<br>

## 🤔 요구사항
For building and running the application you need:

- Language: Java 17
- Framework: Spring Boot 3.4.x
- Database: MySQL 8.0, Redis
- Infrastructure: AWS (EC2, RDS, S3), Docker, Nginx
<br>

## ⚒️ 개발 환경
* BackEnd : Spring Boot
* 버전 및 이슈 관리 : Github, Github Issues
* 협업 툴 : Discord, Notion

<br>

## 🔎 기술 스택
### Envrionment
<div align="left">
<img src="https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white" />
<img src="https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white" />
</div>

### Development
<div align="left"> <img src="https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white" /> <img src="https://img.shields.io/badge/Spring_Boot-3.4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" /> <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" /> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" /> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" /> <img src="https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white" /> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" /> <img src="https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white" /> </div>


### ☁️ Infrastructure & AI
<div align="left"> <img src="https://img.shields.io/badge/AWS_EC2-232F3E?style=for-the-badge&logo=amazonec2&logoColor=white" /> <img src="https://img.shields.io/badge/AWS_RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white" /> <img src="https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white" /> <img src="https://img.shields.io/badge/Google_Gemini-4285F4?style=for-the-badge&logo=googlegemini&logoColor=white" /> <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" /> </div>

<br>

## 📁 프로젝트 구조 
```
mohaemukzip_be
├── src/main/java/com/mohaemukzip/mohaemukzip_be
│   ├── 📦 domain          // 비즈니스 핵심 도메인 (기능별 응집)
│   │   ├── chatbot       // 레시피 상담 챗봇
│   │   ├── home          // 메인 화면 대시보드 및 집계
│   │   ├── ingredient    // 냉장고 재료 및 즐겨찾기 관리
│   │   ├── member        // 회원 정보 및 활동 관리
│   │   ├── mission       // 일일 미션 및 도전 과제 시스템
│   │   ├── recipe        // 레시피 및 조리 기록
│   │   └── search        // 통합 검색 엔진
│   │
│   └── 📦 global          // 전역 공통 모듈 (인프라 및 유틸리티)
│       ├── config        // AWS(S3), Redis, Security, Swagger 설정
│       ├── entity        // 공통 엔티티 (BaseEntity 등)
│       ├── exception     // 전역 예외 처리 (GlobalExceptionHandler)
│       ├── jwt           // JWT 보안 인증 및 토큰 블랙리스트 관리
│       ├── response      // 공통 응답 포맷 (ApiResponse, ErrorStatus)
│       ├── s3            // 파일 업로드 서비스 및 컨트롤러
│       ├── security      // CustomUserDetails 등 보안 인증 로직
│       ├── service       // LevelService, PythonExecutor 등 공통 서비스
│       └── util          // 공통 유틸리티 (TimeFormatter 등)
│
├── 📂 resources
│   ├── db.mygration      // DB 마이그레이션 및 초기화 스크립트
│   ├── 📄 application.yaml             // 프로젝트 공통 설정 (포트, JPA 기본 설정 등)
│   ├── 📄 application-dev.yaml         // 로컬/개발 환경 전용 설정 (로컬 DB)
│   ├── 📄 application-dev.template.yaml// 개발 설정 공유를 위한 템플릿 파일
│   └── 📄 application-prod.yaml        // 운영 환경 전용 설정 (AWS RDS, S3, 배포용 환경변수)
│
└── 🛠️ Root Files
    ├── 📄 compose.yaml             // 기본 도커 컴포즈 설정
    ├── 📄 compose.override.yaml    // 로컬 개발 환경 덮어쓰기 설정
    └── 📄 compose.prod.yaml        // 운영 서버 배포용 최적화 설정
    ├── build.gradle      // 의존성 및 빌드 관리
    └── requirements.txt  // Python 스크립트 실행 환경 정보
```


## 🌀 코딩 컨벤션
* 패키지명은 lowercase를 사용한다
* 클래스명은 PascalCase, 변수/메서드명은 camelCase를 사용한다
* 상수는 UPPER_SNAKE_CASE를 사용한다
* 인터페이스의 구현체는 이름 뒤에 Impl을 붙인다
* API 응답은 공통 포맷인 ApiResponse<T>로 감싸서 반환한다

<br>

## 🔖 브랜치 컨벤션
이슈를 먼저 판 후 이슈 번호로 브랜치를 만든다 
- main - 제품 출시 브랜치 (Production)
- dev - 통합 개발 브랜치
- chore - build 
- feat/#[이슈번호]-[기능명] - 새로운 기능 개발
- refactor/#[이슈번호]-[기능명] - 코드 리팩토링
- fix/#[이슈번호]-[버그명] - 버그 수정
- docs/#[이슈번호]-[설명] - 문서 수정
<br>

## 📁 PR 컨벤션
- pr 제목은 이슈 제목과 동일하게 올린다
- 예시는 다음과 같다 
  - feat : [기능 구현]
  - refactor : [변경 및 추가 기능 구현]
  - fix : [버그 수정]
  - docs : [문서 수정]
  - chore : [개발 후 서버에 최신화]
 
    <br> 

#### 🌟 태그 종류 (pr 및 커밋 컨벤션과 동일)
| 태그        | 설명                                                   |
|-------------|--------------------------------------------------------|
| [Feat]      | 새로운 기능 추가                                       |
| [Fix]       | 버그 수정                                              |
| [Refactor]  | 코드 리팩토링 (기능 변경 없이 구조 개선)              |
| [Style]     | 코드 포맷팅, 들여쓰기 수정 등                         |
| [Docs]      | 문서 관련 수정                                         |
| [Test]      | 테스트 코드 추가 또는 수정                            |
| [Chore]     | 빌드/설정 관련 작업                                    |
| [Design]    | UI 디자인 수정                                         |
| [Hotfix]    | 운영 중 긴급 수정                                      |
| [CI/CD]     | 배포 및 워크플로우 관련 작업                          |

