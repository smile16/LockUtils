# LockUtils
如何使用本依赖
Step 1. Add it in your root build.gradle at the end of repositories:
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency


dependencies {
	         implementation 'com.github.XiaoWuLibs:FastSharepreference:v1.0.0'
}
	}
  
  　　gradle synchronize一下，在工程中调用库中的任意一个API，如果只提示导包，而不是报错，则表示添加依赖成功。
