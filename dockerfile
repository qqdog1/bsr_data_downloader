FROM anapsix/alpine-java:8u202b08_jdk
RUN apk add maven
RUN mkdir -p /workspace
WORKDIR /workspace
COPY pom.xml /workspace
COPY src /workspace/src
COPY bsr /workspace/bsr
COPY config /workspace/config
COPY settings.xml /workspace
RUN mvn -f pom.xml clean package -s /workspace/settings.xml -Dgithub.user=user -Dgithub.password=token