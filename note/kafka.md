### kafka

#### 消息队列

1. 点对点：可以有多个消费者但是一个消息只能被消费一次

2. 发布订阅：生产者发布topic 消费者订阅，发布的消息可以被订阅的消费者消费。

   因为消费者消费速度不一样，所以有两种模式。

   1. 消费者主动拉取。kafka主动拉取模式，该模式消费者会进行轮询查看是否有消息。
   2. 队列主动推送

#### kafka的基本架构

1. ###### 生产者

2. ###### 消费者

   1. 消费者组。多个消费者可以组成一个消费者组。每一个分区中的消息只能被同一个消费者组里的消费者消费。提高消费能力。消费者组里的消费者数和分区数相等的时候效率最好。

3. ###### kafka集群

   1. broker--集群中每个kafka就可以简单理解为一个broker
      - broker中有不同的topic。topic对消息进行分类。
      - partition--提高负载均衡能力。集群中同一个主题在不同的机器中。
      - leader--针对的是当前分区的leader。follower起到的是备份的作用。

**kafka的工作流程和文件存储机制**

1. ​	kafka的分片和索引。

   kafka生产者不断将消息写到log中，为防止log数据过大导致数据定位效率低下，kafka采用分片和索引机制。

   将每个partition分为多个segment。每个segment对应两个文件一个是log 一个index。

   - 索引中存在的是消息起始位置的偏移量。

2. kafka的生产者

   - 分区原因：方便扩展，提高并发

   - 分区原则：数据会被封装成为一个produceRecord（topic，partition）

   - Partition 。指定partition时候直接存入；没有指定的时候，若key存在，则将key进行hash，与partition个数取模

     ；若key也不存在，则轮询。随机生成一个数字，取模存入。

   - 数据可靠性。

     为保证数据可靠发送到指定的topic，当topic的partition发送完消息后会向producer发送一个ack（acknowledgement）,

     如果product收到ack后，会进行下一轮发送，否则重写发送。

   - 发送ack的时机。

     follower与leader同步完成后在发送ack。这样才能保证在leader挂掉之后，能选举新的leader。

     1.半数以上follower完成同步发送ack。2.全部同步成功的时候才发送。kafka的第二种。如果某种故障，一个follower同步很慢，或者挂掉了，这种情况会导致问题，因此出现了ISR同步副本。

   - ISR 同步副本。leader挂掉之后从ISR中副本选择新的leader。ISR中的follower完成同步后，leader会给follower发送ack，如果follower长时间没有返回则将follower从ISR中剔除。

   - ack的级别。0是produce不等ack，leader写完不等follower直接发送。可能丢失数据。1是只等待leader写完。-1全部返回成功后才发送。

   - 一致性问题。

     log文件中有两个名字HW（high water mark）和LEO(log end offset) .leo 是每个副本中最后一个offset。hw是所有副本中最小的offset。只有最小的offset之前的数据才能被消费者看到。leader故障后，会从isr中选出一个新的leader，为保障数据一致性，其余的follower会将各自高于hw不分截掉，重新从leader中同步。

   - 数据重复性问题解决

     ack -1 至少同步一次 ack 0 只同步一次。至少一次会有重复数据的问题。解决方式kafka引入了幂等性的概念。

     所谓幂等性就是，produce不论向server发送多少条重复数据，server只保存1次。原理：发送消息的时候会携带一个seqNumber，对于同一个pid（生产者id），patition 和seqNumber的数据做去重。

   - ack解决的是数据丢失不丢失的问题，ISR解决的是一致性问题。

**kafka消费者**

1. customer 采用自动拉取的模式。拉取模式的缺点是需要简历一个长连接不断的轮询是否有新的数据需要拉取。kafka采取的解决方式是传一个时间，如果kafka没有拉取到数据后，经过一段时间在拉取。
2. 分配策略。kafka的消费者组中有多个消费者，一个topic有多个分区，必然涉及partition的分配。一种分配是roundRobin轮询，一种是range范围。同一个消费者组里的消费者不能同时消费同一个分区。
3. offset的保存。consumer 默认将offset保存在 名为_comsumer_offsets的主题中。
4. kafka高效读写。原因是1.顺序写入，2.零复制技术。

**kafka事务**

1. 为了实现跨会话事务，kakka有个全局唯一id。并将pid和transactionID进行绑定

**消息发送流程**

1. 消息发送是异步，ack保证的是数据不丢失，不是同步异步的问题。涉及两个线程 main线程和sender线程。
2. sender（productRecoder） 拦截器 序列化器 分区器

**master的选举**

1. 注册到zookeeper 顺序节点作为master。

**kafka的实现原理**

1. 异步

   ~~~java
   properties.put(ProducerConfig.BATCH_SIZE_CONFIG,value);--批量大小
   properties.put(ProducerConfig.LINGER_MS_CONFIG,value);--间隔时间
   ~~~

   kafka异步是批量发送，数据达到一定大小的时候是异步发送。linger_ms 发送时间的间隔，都配置满足一个就行。

2. auto_commit AUTO_COMMIT_INTERVAL_MS_CONFIG 自动提交，批量确认。

3. auto_offset AUTO_OFFSET_RESET_CONFIG 消费位置

4. 消费者同一个groupId 多个group 如何消费消息，保证只有一个消费者消费掉。

5. 一个topic中数据量过大，如何解决？千亿，万亿级别。

   1. 分区。partition。根据算法分区。指定分区的时候 直接写入，没有指定分区，有key的话，按把key hash取模

      啥都没有的话，随机一个key 取模。

      消费者数少于分区数的时候，（消费者2，分区3） 一个消费者消费两个 分区的，一个消费一个分区的（条件是同一个消费者组）

   2. 什么时候触发消费者消费的分区变化、消费者消费哪个分区由谁决定。

      消费者变化的时候回rebalance。

      什么时候指定分区策略。消费者启动的时候。每个消费者启动的时候触发coodinator，做分发。

      消费者消费策略：roundRobin  range  strick

      1. range

         topic：test  partition：10  consumer：3

         分区数：0，1，2，3，4，5，6，7，8，9

         消费者：1，2，3

         m=10/3=3 分区数/消费者数 

         n=10%3=1分区数%消费者数

         原则 前m个消费者消费 n+m个分区，后的消费者消费n个分区

         消费者1消费：0，1，2，3

         消费者2消费：4，5，6

         消费者3消费：7，8，9

      2. RoundRobin轮询

         按照分区的hashCode进行轮询。

      3. strick 

         1. 尽可能均匀 。
         2. 分区的分配尽可能和上一次保持一致。
         3. 发生重新分配时候 会将宕机的分区消费的partition 重新分配。

      4. coordinator

         coordinator 保存了所有消费者的信息还有offset。而且消费者必须和coordinator 连接。

         1. 如何确定coordinator？

            每个消费者向集群的任意节点发送请求，确认coordinator 角色。负载最小的是。

         2. 确认分区策略步骤。
         
            1. join group 每个消费者组里的消费者加入发送join group 请求到coordinator。
         
            2. coordinator 选择出消费者组里的一个leader，同步给所有消费者。leader根据分区策略做分区。
         
            3. 发送同步请求。计算好的分区结果同步给coordinator。
         
               
         
      5. kafka如何保证不重复消费的问题。通过offset 保证
      
         1. offset在server端维护。
         2. 消费者组消费的偏移量数据存储在磁盘_consumer_offset_xx.算法是 group_id hash %50。
         3. 消费者重复消费消息主要是因为提交时间间隔。可以改成手动提交。
      
      6. 副本 replication
      
         1. leader 和follow
         
         2. 副本的同步机制。
         
            follow 副本发起sync
         
            ISR-->和leader副本集合差不多的副本集合。如果当前时间和副本同步时间相差过大的话，副本会在ISR中被踢掉。
            
            （in synchronizated replicatio）
            
         3. ISR节点保留的是处理性能比较好的节点，follow挂掉或者延迟过大的时候就会剔除。
         
            1. 消息如何传播
            2. 在向生产者返回之前需要保证多少个副本同步
            3. 名词.HW 高水位（消费者近可见的消息，副本已经同步完成，数据一致） LEO log end offset（每个副本中最后一条消息） 
            4. 更新过程
               1. follower 发起fatch请求。
               2. leader 读取log日志，更新remote leo，更新hw，把内容和当前分区的hw发送给follower
               3. follower写日志更新本地的leo，不会更新hw（取的是本地的leo和返回的hw取较小的值。）
               4. follower 发起ack 给leader，leader更新hw，将信息返回给follower ，follower更新hw。过程是两次轮询。
            5. ack。发送端请求是否发送到了broker上。0不不需要确认，数据丢失 风险大；1 只要leader副本返回。-1 需要ISR中所有副本确认。
            6. 数据丢失发生的地方在哪里。
               1. leader通知follower修改hw的时候 follower挂掉了，follower重启后重新同步数据的时候leader挂掉了，follower变更了leader导致数据丢失了。情况特殊只有一个副本且ack是-1的时候。
               2. leader epoch。offset for leader epoch request。
            7. 数据的持久化。
