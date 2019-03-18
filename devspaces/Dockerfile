FROM ubuntu

LABEL description="This file generates a docker image containing everything needed to build LibGDX project"

# Install JDK 8
RUN apt-get update \
 && apt-get install -y openjdk-8-jdk wget unzip git sudo \
 && apt-get clean

# Install Android SDK
RUN wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz \
 && tar xf android-sdk*-linux.tgz \
 && wget https://dl.google.com/android/repository/android-ndk-r12b-linux-x86_64.zip \
 && unzip android-ndk*.zip \
 && ( while [ 1 ]; do sleep 5; echo y; done ) | /android-sdk-linux/tools/android update sdk --no-ui \
 && ( while [ 1 ]; do sleep 5; echo y; done ) | /android-sdk-linux/tools/android update sdk --all --no-ui --filter $(seq -s, 27) \
 && dpkg --add-architecture i386 \
 && apt-get update \
 && apt-get install -y libc6:i386 libstdc++6:i386 zlib1g:i386 \
 && apt-get clean \
 && rm -rf android-sdk_r24.4.1-linux.tgz \
 && rm -rf android-ndk-r12b-linux-x86_64 \
 && rm -rf /var/lib/apt/lists /var/cache/apt

ENV ANDROID_HOME=$HOME/android-sdk-linux \
    JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 \
    PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$HOME/android-sdk-linux/tools:$HOME/android-sdk-linux/platform-tools:$PATH \
    ANDROID_DAILY_OVERRIDE=f7213cf7652733a92329555ff913d1c7a1b74f89

WORKDIR /data

CMD ["tail", "-f", "/dev/null"] 