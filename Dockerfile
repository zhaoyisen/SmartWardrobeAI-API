# 1. 基础镜像：使用轻量级的 Alpine Linux，内置 JDK 21 (Eclipse Temurin 发行版，稳定且小巧)
FROM eclipse-temurin:21-jre-alpine

# 2. 维护者信息 (可选)
LABEL maintainer="SmartWardrobeAI"

# 3. 设置容器内的时区为上海 (解决日志时间差8小时的问题)
# 安装 tzdata 依赖 -> 复制上海时区文件 -> 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 4. 创建并设置工作目录
WORKDIR /app

# 5. 挂载一个临时目录 (Spring Boot Tomcat 有时需要)
VOLUME /tmp

# 6. 将 Maven 打包生成的 jar 包复制到镜像中
# 注意：pom.xml 里 artifactId 是 SmartWardrobeAI，version 是 0.0.1-SNAPSHOT
# maven 默认生成的包名是 SmartWardrobeAI-0.0.1-SNAPSHOT.jar
# 我们统一重命名为 app.jar，方便后续脚本通用
COPY target/*.jar app.jar

# 7. 暴露端口 (对应 application.yml 里的 server.port: 9090)
EXPOSE 9090

# 8. 启动命令
# 增加 -Djava.security.egd 是为了加快随机数生成，避免启动卡顿
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]