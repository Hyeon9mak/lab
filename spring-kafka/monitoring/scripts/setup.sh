#!/bin/bash
set -e

JMX_EXPORTER_VERSION="1.0.1"
JMX_EXPORTER_JAR="jmx_prometheus_javaagent-${JMX_EXPORTER_VERSION}.jar"
JMX_EXPORTER_URL="https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${JMX_EXPORTER_VERSION}/${JMX_EXPORTER_JAR}"
TARGET_DIR="monitoring/jmx-exporter"

echo "Setting up Prometheus JMX Exporter..."

# 디렉토리 생성
mkdir -p "${TARGET_DIR}"

# JAR 다운로드
if [ ! -f "${TARGET_DIR}/${JMX_EXPORTER_JAR}" ]; then
    echo "Downloading JMX Exporter JAR..."
    curl -L "${JMX_EXPORTER_URL}" -o "${TARGET_DIR}/${JMX_EXPORTER_JAR}"
    echo "Downloaded: ${TARGET_DIR}/${JMX_EXPORTER_JAR}"
else
    echo "JMX Exporter JAR already exists: ${TARGET_DIR}/${JMX_EXPORTER_JAR}"
fi

echo "Setup complete!"
