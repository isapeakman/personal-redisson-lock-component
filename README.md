# 分布式锁组件（ServiceLock）

基于 Redisson 实现的 Spring Boot 分布式锁组件，提供注解式和方法式两种使用方式，支持多种锁类型和自定义超时处理策略。

## 功能特性

### 1. 多种锁类型支持
- **可重入锁 (Reentrant)**: 允许同一线程多次获取同一把锁
- **公平锁 (Fair)**: 按照请求顺序获取锁，避免线程饥饿
- **读锁 (Read)**: 共享锁，适用于读多写少的场景
- **写锁 (Write)**: 排他锁，确保写操作的原子性

### 2. 灵活的加锁方式
- **注解式加锁**: 通过 `@ServiceLock` 注解实现方法级别的声明式加锁
- **方法式加锁**: 通过 `ServiceLockTool` 工具类手动控制加锁和释放锁

### 3. 注解式加锁特性
- 支持指定锁类型、业务名称、自定义业务key
- 可配置加锁等待时间和时间单位
- 支持自定义锁超时处理策略
- 支持 SpEL 表达式动态生成锁的 key

### 4. 设计模式应用
- **工厂模式**: 通过 `ServiceLockFactory` 和 `LockInfoHandleFactory` 创建锁实例
- **策略模式**: 通过 `LockTimeOutStrategy` 实现锁超时处理策略
- **AOP 切面**: 通过 `ServiceLockAspect` 实现声明式锁控制

## 技术栈

- Spring Boot 3.5.15
- Redisson 3.32.0
- Java 17
- MyBatis Plus 3.5.7

## 快速开始

### 环境要求

- JDK 17+
- Redis 服务器
- MySQL 数据库（用于测试示例）

### 配置 Redis 连接

在 `application.yml` 中配置 Redis 连接信息：

```yaml
spring:
  data:
    redis:
      database: 0
      host: 127.0.0.1
      port: 6379
      timeout: 3000
```

## 使用方式

### 方式一：注解式加锁

在需要加锁的方法上添加 `@ServiceLock` 注解：

```java
@Service
public class OrderService {

    @ServiceLock(
        lockType = LockType.Reentrant,
        name = "order",
        keys = {"#orderId"},
        waitTime = 10,
        timeUnit = TimeUnit.SECONDS
    )
    public void createOrder(String orderId) {
        // 业务逻辑
    }
}
```

#### 注解参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| lockType | LockType | Reentrant | 锁类型（Reentrant、Fair、Read、Write）|
| name | String | "" | 业务名称 |
| keys | String[] | {} | 业务key，支持 SpEL 表达式（如 `#id`、`#user.id`）|
| waitTime | long | 10 | 加锁等待时间 |
| timeUnit | TimeUnit | SECONDS | 时间单位 |
| lockTimeoutStrategy | LockTimeOutStrategy | FAIL | 锁超时处理策略 |
| customLockTimeoutStrategy | String | "" | 自定义锁超时处理方法名 |

#### 锁超时处理策略

- **FAIL**: 快速失败，抛出运行时异常（默认策略）

#### 自定义超时处理策略

```java
@Service
public class OrderService {

    @ServiceLock(
        name = "order",
        keys = {"#orderId"},
        customLockTimeoutStrategy = "handleTimeout"
    )
    public void createOrder(String orderId) {
        // 业务逻辑
    }

    private void handleTimeout(String orderId) {
        // 自定义超时处理逻辑
        log.warn("获取锁超时: orderId={}", orderId);
    }
}
```

### 方式二：方法式加锁

注入 `ServiceLockTool` 工具类，手动控制加锁：

```java
@Service
public class OrderService {

    @Autowired
    private ServiceLockTool serviceLockTool;

    public void createOrder(String orderId) {
        // 无返回值的加锁执行
        serviceLockTool.execute(() -> {
            // 业务逻辑
        }, "order", new String[]{orderId});

        // 指定锁类型和等待时间
        serviceLockTool.execute(LockType.Fair, () -> {
            // 业务逻辑
        }, "order", new String[]{orderId}, 15);

        // 有返回值的加锁执行
        Order order = serviceLockTool.submit(() -> {
            // 业务逻辑
            return order;
        }, "order", new String[]{orderId});
    }
}
```

### 方式三：编程式加锁

通过 `LockManager` 直接操作锁：

```java
@Service
public class OrderService {

    @Autowired
    private LockManager lockManager;

    public void createOrder(String orderId) {
        ServiceLocker locker = lockManager.getReentrantLocker();
        String lockKey = "order:" + orderId;

        boolean locked = locker.tryLock(lockKey, TimeUnit.SECONDS, 10);
        if (locked) {
            try {
                // 业务逻辑
            } finally {
                locker.unlock(lockKey);
            }
        } else {
            // 获取锁失败处理
        }
    }
}
```

## 锁类型说明

| 锁类型 | 枚举值 | 适用场景 |
|--------|--------|----------|
| 可重入锁 | LockType.Reentrant | 同一线程需要多次获取同一把锁的场景 |
| 公平锁 | LockType.Fair | 需要按请求顺序获取锁，避免线程饥饿的场景 |
| 读锁 | LockType.Read | 读多写少的场景，多个线程可同时读 |
| 写锁 | LockType.Write | 写操作场景，确保写操作的原子性 |

## 最佳实践

### 1. 锁与事务的配合

**问题**: 使用声明式事务（`@Transactional`）时，事务的提交发生在锁释放之后，可能导致脏读。

**推荐方案**: 使用编程式事务，将事务控制放在锁内部：

```java
@Service
public class OrderService {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ServiceLockTool serviceLockTool;

    public void updateOrder(String orderId) {
        serviceLockTool.execute(() -> {
            transactionTemplate.execute(status -> {
                // 业务逻辑
                return null;
            });
        }, "order", new String[]{orderId});
    }
}
```

### 2. 业务 key 设计

- 确保业务 key 的唯一性，避免锁冲突
- 使用 SpEL 表达式动态生成 key：`#orderId`、`#user.id`
- 锁粒度要合适，避免过大的锁粒度影响并发性能

### 3. 等待时间设置

- 根据业务执行时间合理设置 `waitTime`
- 避免设置过长的等待时间导致线程阻塞

## 项目结构

```
src/main/java/com/example/servicelock/
├── annotation/          # 注解定义
│   └── ServiceLock.java
├── aspect/             # AOP 切面
│   └── ServiceLockAspect.java
├── config/             # 自动配置
│   ├── ServiceLockAutoConfiguration.java
│   ├── RedissonCommonAutoConfiguration.java
│   └── RedissonBaseProperties.java
├── core/               # 核心组件
│   ├── LockManager.java
│   └── SpringUtil.java
├── enums/              # 枚举定义
│   └── LockType.java
├── factory/            # 工厂类
│   ├── ServiceLockFactory.java
│   └── LockInfoHandleFactory.java
├── handler/            # 超时处理
│   ├── LockTimeOutHandler.java
│   ├── LockTimeOutStrategy.java
│   └── RedissonDataHandle.java
├── info/               # 锁信息处理
│   ├── LockInfoHandle.java
│   ├── AbstractLockInfoHandle.java
│   └── ServiceLockInfoHandle.java
├── lock/               # 锁实现
│   ├── ServiceLocker.java
│   └── impl/
│       ├── RedissonReentrantLocker.java
│       ├── RedissonFairLocker.java
│       ├── RedissonReadLocker.java
│       └── RedissonWriteLocker.java
├── parser/             # 参数解析器
│   ├── ExtParameterNameDiscoverer.java
│   └── LocalVariableTableParameterNameDiscoverer.java
└── util/               # 工具类
    ├── ServiceLockTool.java
    ├── TaskRun.java
    └── TaskCall.java
```

## 许可证

本项目采用 MIT 许可证。




