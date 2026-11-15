pluginManagement {
    repositories {
        // PICO Spatial SDK 只在火山引擎仓库
        maven {
            url = uri("https://artifact.bytedance.com/repository/Volcengine")
            name = "Volcengine"
        }
        // 国内镜像代理 Google / Maven Central / Gradle Plugin Portal
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "AliyunGoogle"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/central")
            name = "AliyunCentral"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
            name = "AliyunGradlePlugin"
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = uri("https://artifact.bytedance.com/repository/Volcengine")
            name = "Volcengine"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/google")
            name = "AliyunGoogle"
        }
        maven {
            url = uri("https://maven.aliyun.com/repository/central")
            name = "AliyunCentral"
        }
    }
}

rootProject.name = "demo"

include(":app")
