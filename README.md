个人可复用Redisson-Lock组件Demo
# 分布式锁框架

一个功能强大、灵活易用的分布式锁框架，支持多种锁类型和加锁方式，提供高度可定制的锁超时处理策略。

## 功能特性

1. **多种锁类型支持**
   - 可重入锁 (ReentrantLock)
   - 公平锁 (FairLock)
   - 读写锁 (ReadWriteLock)

2. **灵活的加锁方式**
   - 注解式加锁：通过注解方式实现方法级别的加锁，支持自定义参数
   - 方法式加锁：通过API方式手动控制加锁和释放锁

3. **注解式加锁特性**
   - 支持指定锁类型
   - 自定义业务名称
   - 灵活的业务key生成方式
   - 可配置加锁等待时间
   - 可自定义锁超时处理策略

4. **锁超时处理策略**
   - 提供默认的拒绝策略
   - 支持自定义超时处理策略

5. **设计模式**
   - 基于工厂模式实现锁的创建
   - 基于策略模式实现锁超时处理

## 快速开始

### 依赖配置

```xml
<!-- Maven依赖 -->
<dependency>
    <groupId>com.example</groupId>
    <artifactId>distributed-lock</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基础使用

#### 1. 方法式加锁

```java
LockManager lockManager = LockFactory.createLockManager(LockType.REENTRANT);

// 加锁
Lock lock = lockManager.lock("businessKey", 5000);
try {
    // 业务逻辑
} finally {
    // 释放锁
    lockManager.unlock(lock);
}
```

#### 2. 注解式加锁

```java
@Lockable(
    lockType = LockType.REENTRANT,
    businessName = "orderService",
    keyGenerator = "orderKeyGenerator",
    waitTime = 3000,
    timeoutStrategy = TimeoutStrategy.REJECT
)
public void createOrder(Order order) {
    // 业务逻辑
}
```

### 高级配置

#### 自定义锁超时处理策略

```java
// 实现LockTimeoutStrategy接口
public class CustomTimeoutStrategy implements LockTimeoutStrategy {
    @Override
    public void handleTimeout(String businessKey, String lockKey) {
        // 自定义超时处理逻辑
    }
}

// 注册自定义策略
LockManager lockManager = LockFactory.createLockManager(LockType.REENTRANT);
lockManager.registerTimeoutStrategy("custom", new CustomTimeoutStrategy());
```

## API文档

### 锁类型

| 锁类型 | 描述 | 适用场景 |
|--------|------|----------|
| REENTRANT | 可重入锁 | 同一线程可多次获取同一把锁 | 
| FAIR | 公平锁 | 按请求顺序获取锁，避免线程饥饿 |
| READ_WRITE | 读写锁 | 读多写少的场景，提高并发性能 |

### 注解参数

| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| lockType | LockType | REENTRANT | 锁类型 |
| businessName | String | "" | 业务名称 |
| keyGenerator | String | "" | 自定义key生成器 |
| waitTime | long | 0 | 加锁等待时间(毫秒) |
| timeoutStrategy | TimeoutStrategy | REJECT | 锁超时处理策略 |

### 锁超时处理策略

| 策略名称 | 描述 |
|----------|------|
| REJECT | 拒绝执行，抛出异常 |
| CONTINUE | 继续执行业务逻辑 |
| RETRY | 重试执行业务逻辑 |
| CUSTOM | 自定义处理逻辑 |

## 最佳实践

1. **锁粒度选择**：根据业务场景选择合适的锁粒度，避免过大的锁粒度影响并发性能。

2. **锁超时设置**：合理设置锁超时时间，避免长时间持有锁导致系统性能下降。

3. **异常处理**：确保在finally块中释放锁，避免死锁。

4. **业务key设计**：设计合理的业务key，确保唯一性，避免锁冲突。

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个项目。

## 许可证

本项目采用MIT许可证。详见 [LICENSE](LICENSE) 文件。




