# X Throttling Test

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg" alt="Platform">
  <img src="https://img.shields.io/badge/Language-Kotlin-blue.svg" alt="Language">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg" alt="UI">
  <img src="https://img.shields.io/badge/Material-3-purple.svg" alt="Material Design">
  <img src="https://img.shields.io/badge/Min%20SDK-21-red.svg" alt="Min SDK">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

A comprehensive Android application for testing CPU throttling behavior under sustained workload. Built with **Jetpack Compose** and **Material Design 3**, this app provides real-time performance monitoring, detailed stability analysis, and professional reporting capabilities.

---

## 🎯 Features

### Core Functionality
- **Real-time CPU Performance Testing** - Measures GIPS (Giga Instructions Per Second) with 150M iterations per sample
- **Thermal Throttling Detection** - Identifies performance degradation due to thermal limits
- **Stability Analysis** - Current-based stability calculation with peak degradation tracking
- **Live Performance Graph** - Interactive chart with multi-color performance indicators
- **CPU Monitoring** - Real-time per-core frequency tracking and overall CPU usage

### Advanced Features
- **Smart Benchmarking Engine** - Calibrated workload with optimized iteration counts
- **Progressive UI States** - Empty state, Running state, and Completed state with smooth transitions
- **Multi-format Export** - Share results as PNG Image, PDF Report, or JSON data
- **Professional Reporting** - Comprehensive performance metrics and visual graphs

### UI/UX Highlights
- **Material Design 3** - Modern, adaptive theming with dynamic colors
- **State-driven Architecture** - Clean separation of UI states for better UX
- **Responsive Design** - Optimized for various screen sizes
- **Dark Theme Support** - Full light/dark mode compatibility
- **Smooth Animations** - Polished transitions and interactive elements

---

## 📊 Performance Metrics

### Stability Calculation
The app uses a **current-based stability metric** that reflects real-time performance:

**Status Levels:**
- **95-100%** - Excellent (Perfect, no throttling)
- **90-95%** - Very Good (Minimal throttling)
- **85-90%** - Good (Light throttling)
- **80-85%** - Fair (Moderate throttling)
- **75-80%** - Warning (Notable throttling)
- **70-75%** - Throttling (Significant throttling)
- **65-70%** - Heavy (Heavy throttling)
- **<65%** - Critical (Severe throttling)

### Peak Degradation
Tracks the worst performance drop from peak:

This provides historical context while the stability metric gives real-time feedback.

---

## 🏗️ Architecture

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **State Management:** StateFlow
- **Design System:** Material Design 3
- **Concurrency:** Coroutines + Flow


---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Narwhale Feature Drop or later
- **JDK** 11 or higher
- **Android SDK** API 21+ (Android 5.0+)
- **Gradle** 8.0+


### Why This Approach?

- **CPU-intensive** - Pure computational workload
- **Memory-light** - Minimal RAM usage
- **Thermal-sensitive** - Generates sustained heat
- **Reproducible** - Consistent results across runs
- **Platform-agnostic** - No native code dependencies


---

## 🧪 Testing Recommendations

### Device Preparation
1. **Charge to 80%+** - Avoid thermal throttling from charging
2. **Close background apps** - Ensure isolated CPU testing
3. **Disable battery saver** - Prevent frequency capping
4. **Remove case** - Allow proper heat dissipation
5. **Room temperature** - Test in consistent ambient conditions

### Test Duration
- **Short (1-3 min)** - Quick performance check
- **Medium (5-10 min)** - Recommended for thermal analysis
- **Long (15-30 min)** - Extreme stress testing

### Interpreting Results

#### Excellent Thermal Management (95%+ stability)
- Minimal throttling throughout the test
- Consistent peak performance
- Good cooling system or efficient CPU

#### Moderate Throttling (80-95% stability)
- Expected behavior for most devices
- Thermal limits reached but manageable
- Acceptable for normal use cases

#### Severe Throttling (<80% stability)
- Significant performance degradation
- Thermal limits reached quickly
- Consider external cooling solutions

---

## 📧 Contact

**Developer**: Gustyx-Power 
**Email**: gustiadityamuzaky08@gmail.com  
**Telegram**: [@GustyxPower](t.me/GustyxPower)  
**Xtra Manager Software Community**: [Join Here](https://t.me/XtraManagerSoftware)

---

## 🌟 Support
If you find this project helpful, please give it a ⭐️!
Feel free to share feedback or report issues via GitHub Discussions or Issues.
---
[![Made with Kotlin](https://img.shields.io/badge/Made%20with-Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)

