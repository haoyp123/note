### kafka

1. #### 消息队列

   1. 点对点：可以有多个消费者但是一个消息只能被消费一次

   2. 发布订阅：生产者发布topic 消费者订阅，发布的消息可以被订阅的消费者消费。

      因为消费者消费速度不一样，所以有两种模式。

      1. 消费者主动拉取。kafka主动拉取模式，该模式消费者会进行轮询查看是否有消息。
      2. 队列主动推送

2. #### kafka的基本架构

   1. ###### 生产者

   2. ###### 消费者

      1. 消费者组。多个消费者可以组成一个消费者组。每一个分区中的消息只能被同一个消费者组里的消费者消费。提高消费能力。消费者组里的消费者数和分区数相等的时候效率最好。

   3. ###### kafka集群

      1. broker--集群中每个kafka就可以简单理解为一个broker
         - broker中有不同的topic。topic对消息进行分类。
         - partition--提高负载均衡能力。集群中同一个主题在不同的机器中。
         - leader--针对的是当前分区的leader。follow起到的是备份的作用。
   
3. **kafka的工作流程和文件存储机制**

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
   
4. **kafka消费者**

   1. customer 采用自动拉取的模式。拉取模式的缺点是需要简历一个长连接不断的轮询是否有新的数据需要拉取。kafka采取的解决方式是传一个时间，如果kafka没有拉取到数据后，经过一段时间在拉取。
   2. 分配策略。kafka的消费者组中有多个消费者，一个topic有多个分区，必然涉及partition的分配。一种分配是roundRobin轮询，一种是range范围。同一个消费者组里的消费者不能同时消费同一个分区。

