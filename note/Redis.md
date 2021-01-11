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

1. 定时淘汰 expire key ttl 10 通过定时器 可以定时精准删除

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

   2. rewrite 同一样的操作指令只记录一次。bgrewriteaof

      在重写的时候新接受到的指令会存储到一个缓存中，重写完毕后追加到aof。

   3. rewrite触发时间

      1. 达到上次重写文件的100%
      2. 达到设置的大小 默认64M

#### 4.redis的高级

1. 