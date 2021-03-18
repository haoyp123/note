## 一、 redis 基础

#### 1.redis存储的数据类型

- string

- hash

- list

- set

- zset

- hyperloglogs 基数统计

- geo 地理位置

  

#### 2.redis常用命令

- string 

  - set  key value
  - expire  key time (second)
  - set key value ex|px  time  (ex 秒 px 毫秒)   nx|xx（nx not exists |xx already exists） 
  - append key value 追加
- int
  - incr  key 
  - decr key
  - incrbyfloat key  
- hash
  - hset kay  field value （field 字段）
  - hget key field
  - hmset  key field1 value1 fields value2
  - hmget key field1 field2 field3 
  - hkyes key 获取所有的field
  - hvals key 获取所有的value
- list
  - lset
  - lget
- set
  - sadd 
  - smembers
  - srandmember
  - sdiff
  - sinter
  - sunion

#### 3.redis的string的编码结构

redis是kv的数据结构，用hashtable存储。每个kv键值对用dictEntry存储。 

key用SDS存储 （simple dynamic string 简单动态字符串）

1. ##### string的数据结构

   value 先用redisObject存储，在由redisObject指向具体的数据结构。

- string的编码结构由三种

  查看编码用 object encoding key

  1. int <long的最大值的
  2. embstr <44字节
  3. raw >44自己

- sds 和 c语言的string 区别
  1. c语言没有string 用的是char[]表示 字符串  \0表示字符串结束 不安全。sds用的len判断 
  2. c语言会先分配内存，获取字符串大小需要遍历char[]，动态扩容比较麻烦。
  3. sds有空间预分配和惰性释放空间，获取长度时间复杂的是o(1)

- embstr 和raw转化
  1. embstr<44 ，这个编码结构只读。内存空间连续，只分配一次。
  2. raw 分配两次，内存不连续。

##### hash的数据结构

ziplist 压缩列表 存储不是指针 ， 存储的是上一个链表的长度和下一个链表的长度 ，节省内存。

ht hashtable

##### 跳表skiplist

通过leavel 给链表分层，减少查找次数。

## 二、redis原理

#### 1.redis的过期策略

1. 定时淘汰 expire key ttl 110 通过定时器 可以定时精准删除

2. 定期淘汰 对一些设置过期时间的数据进行扫描，进行内存清理。

3. 惰性淘汰 被动淘汰 在get set这个值的时候先判断 是否过期。set值的时候 发现内存已经达到上限了，调用一个方法释放内存。

   redis 用的是定期+惰性过期

#### 2.redis的淘汰策略

1. LRU least recently used 最近最少使用             （设置了ttl 和所用key）
2. LFU least frequently used 最近最少使用频率  （设置了ttl 和所用key）
3. 随机删除 （设置了ttl 和所用key） （设置了ttl 和所用key）
4. 根据ttl属性 将接近过期时间的key删除
5. 不删除，不在添加值。

#### 3.redis的持久化

1. rdb  redis data base

   1. 自动触发 多少秒内 多少条数据被修改

      - save 900 1
      - save 300 10
      - save 60 1000

   2. 手动触发 

      - save 

        会阻塞进程一般不用

      - bgsave

        会fork一个子进程，子进程进行持久化。

      

2. aof append to file 存储的是指令

   1. 设置记录时间

      - appendfsync everysec 每一秒
      - appendfsync always  实时
      - appendfsync no 不同步

   2. rewrite 同一样的操作指令只记录一次。为防止文件过大，回复时间过长，aof采取bg rewrite aof

      通过设置rewrite-percentage 和rewriete-max-size可以设置重写的大小。

      比如percent=100 max-size=64M说明 aof文件64M并且比上一次重写大了1倍的时候触发。

      在重写的时候新接受到的指令会存储到一个缓存中，重写完毕后追加到aof。
   
   3. rewrite触发时间
   
      1. 达到上次重写文件的100%
      2. 达到设置的大小 默认64M
      
   4. rdb和aof同时开启的时候会优先从aof恢复。

#### 4.redis的高级

##### 发布订阅

1. subscribe  chanel  
2. publish chanel
3. psubscribe 带有通配符的订阅

##### 事务

redis事务是放在队列中的，按顺序执行。

1. multi 开启事务

2. exec 执行事务

3. discard 取消事务

4. watch cas 乐观锁 如果在事务开启前对一个key监听，事务期间key发生变化，事务不会执行。

5. 事务能保证原子性吗？

   在exec前出错，如语法错误，整个事务不会执行。

   在exec后出错，正确的命令会被执行，报错的命令不会执行（不论是否在报错命令之后）。

##### reids速度快的原因

1. 纯内存

2. kv结构 时间复杂度为o1

3. 单线程多路复用，异步非阻塞IO。

   1. cpu和内存之间有个虚拟内存，通过虚拟地址映射到物理地址。

   2. 虚拟内存分为用户空间和内核空间。内核是操作系统的核心，受保护的内存空间。

      当进程运行在内核空间的时候叫内核态运行在用户空间的时候叫用户态。

   3. 上下文切换  执行不同进行 cpu挂起，将寄存器和程序计数器保存在内核中。

      使用的时候在读取内核数据。这就是上下文切换。

   4. IO阻塞。用户空间等待内核空间准备数据和内核空间将数据拷贝到用户空间。

      多路复用就是用户空间请求数据到内核空间的时候，内核空间负责将请求存储到一个队列中（形象说法）

      当一个请求完毕后内核空间通知用户空间可以拷贝数据。用户空间再次发起请求进行拷贝。

      

## 三、redis的分布式

#### 1.信息同步

1. 全量信息同步

   第一次连接到主节点的时候会有一个全量的信息同步。

   建立连接——》master 通过bgsave 生成一个rdb文件。同时新接受的指令会放到缓存区。从节点接受完rdb文件后

   在接受缓冲区的文件。

2. 增量信息同步

   命令传播。主节点接受命令后，会通过连接同步给从节点。
   
3. 分片

   redis集群采用的是数据分片实现非一致性hash，redis集群有16384个哈希槽，计算key属于哪个槽位，存储到哪里。
   
4. 虚拟节点

   节点少的时候会出现key分布不均匀的情况，通过分配虚拟节点，可以让一致性hash分布更加均匀。

#### 2.请求模式

1. client
2. pipeline 相当于批量操作。把数据缓存起来。

#### 3.redis数据一致性问题

1. 先操作redis或者数据库都是不可取的。都会造成数据的不一致性。
2. 先更新数据库，在删除缓存。
   - 数据库更新成功，删除缓存，缓存删除失败，可以放到消息队列中重复消费消息。
   - 数据库更新成功，会写binlog通过binlog删除缓存减少代码侵入。
3. 先删除缓存，在更新数据库。
   - 延迟双删。

#### 4.缓存失效问题

1. 雪崩，同一时间大量key失效。 通过随机数解决，预热更新，对于即将过期的key 
2. 缓存穿透。限流、缓存一个特殊值。布隆过滤或者缓存空对象。
   - ​	布隆过滤器：位图+hash函数+位运算存到位图。位图只有0和1 1存在0不存在。

#### 5.redis幂等性问题