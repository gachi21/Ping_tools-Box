# Ping Tools Box

Android TV / Android Box network diagnostics tool.

Minimum Android version: **8.1 (API 27)**.

Features: continuous ping, packet loss, min/average/max latency, jitter, interval/timeout, presets, terminal-style output, copy/share report.

The release APK is debug-signed for direct testing. GitHub Actions builds, verifies the signature, uploads an artifact, and publishes a GitHub Release automatically.

Note: Android's InetAddress.isReachable() can behave differently from desktop raw ICMP ping depending on the device/network.
