FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# JAR 파일 메인 디렉토리에 복사
COPY build/libs/app.jar app.jar
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone

# python 설치
RUN apt-get update \
 && apt-get install -y python3 python3-pip \
 && rm -rf /var/lib/apt/lists/*

# scripts + requirements 복사
COPY scripts scripts
COPY requirements.txt requirements.txt

# python 의존성 설치
RUN pip3 install --no-cache-dir -r requirements.txt

ENV SPRING_PROFILES_ACTIVE=prod

# 시스템 진입점 정의
ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]