# 问题修改记录

## 推送离线消息只获取10条
#### 问题原因

- 离线消息是分页获取的，每次获取10条。
- 在响应完一次同步数据（PushSyncFinAckRequest）后，没有检查SyncKey，是否已经把全部离线消息都拉取了下来，所有导致此问题。

#### 修改方案

在PushSyncFinHandler响应结束后检查当前的SyncKey，如果还有离线消息，则继续发起同步请求（PushSyncRequest）

## 获取正在运行的进程
#### 问题原因

- android系统5.1及以后的版本getRunningAppProcesses()只能获取到自己进程的信息，无法获取到其他app的进程信息信息
- 推送库中多处使用到getRunningAppProcesses()，其功能主要是对于宿主的选择，宿主切换。

#### 修改方案

- 我们自己的系统会对含有eebbk包名的app权限放开，添加<uses-permission android:name="android.permission.REAL_GET_TASKS"/>权限即可。

## 兼容4.0.5-bugfix宿主切换
#### 问题原因

- 缓存数据无法清除
    - PushApplication.java中callOnInitSuccessListener()的 mInitCalled没有及时置为false。
    导致再次联网后跑到login后发送不了PushSyncTriggerRequest 的请求，尝试过调用turnoff和trunon，但是没有效果。
    所以不能实现检查当前宿主绑定的app里面如果没有才杀，必须每次连接后都杀。但是只针对这个版本的app（保存在PandaAppManager.java中）才做此操作。
    - HostServiceElection.java中getHostServicePackageName()获取当前宿主由于有缓存，宿主已经切换了也不知道，导致无法及时切换宿主。
    
#### 解决方案

- 升级推送库。
- 在不能升级推送库的前提下，只能每次连接后，通过ConnectSwitchService.java的turnON来杀进程，重启4.0.5-bugfix版本的app.

## 宿主切换的“当前登录设备ID”问题

#### 问题原因

- 老版本的因为“当前登录设备ID”通过aidl直接获取了新的注册信息，而且没有走“设置别名和标签请求”更新服务器列表，导致没有关联上“当前登录设备ID”。

#### 解决方案

- 对于4.0.5-bugfix版本遇到此问题：
    - 第一种,找服务器手动更新“当前登录设备ID”的关联。
    - 第二种，清除家长管控的数据。由于家长管控在设置中是不显示的，所以只能恢复原厂设置。
    
- 新版本中修改此问题的方案：
    - 在宿主切换的时候，尝试充老宿主中获取此“当前登录设备ID”（即registerId）。当老宿主没有启动时，会获取不到。
    - 在registerId更变时，清除通过StoreUtil.java清除AliasAndTag的数据，达到触发“设置别名和标签请求”（SetAliasAndTagRequest）。
    因为有获取不到registerId，或者宿主app数据被清除的问题，有可能出现宿主去获取新的registerId的情况。
    - 在ConnectSwitchService.java中预留清除数据的接口。避免出现清除不了数据的尴尬现象。