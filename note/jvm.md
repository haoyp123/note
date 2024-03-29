# JVM

java文件通过词法分析---》语法分析---》语法树--》字节码生成器---》class文件

## The Class File Structure

~~~
ClassFile {
    u4             magic;                
    u2             minor_version;
    u2             major_version;
    u2             constant_pool_count;
    cp_info        constant_pool[constant_pool_count-1];
    u2             access_flags;
    u2             this_class;
    u2             super_class;
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
CAFEBABE
0000
0034
000D
0A0003000A07000B07000C0100063C696E69743E010003282956010004436F646501000F4C696E654E756D6265725461626C6501000A536F7572636546696C6501000B506572736F6E2E6A6176610C00040005010006506572736F6E0100106A6176612F6C616E672F4F626A656374002100020003000000000001000100040005000100060000001D00010001000000052AB70001B1000000010007000000060001

magic 表示class文件的一个识别。 u4 表示16进制文件的前四位。
cafe babe 就表示的是class文件
minor_version 最小版本
major_version 最大版本
constrant_pool_count 常量池数量
~~~



### 1.类加载机制

class-------》jvm 步骤

1. 装载---------classloader

   1. 找到文件所在位置 磁盘 网络等。。类加载器加载类

      - brootstrap classloader 加载的是 lib/rt.jar里的class文件 

      - extension classloader

      - app classloader  加载classpath下的

      - custome classloader 自定义

        装载机制：双亲委派机制。

   2. 将文件信息交给jvm-------------存放在方法区  类的描述信息

   3. 类文件所对应的对象交给jvm--------堆

2. 链接

   1. 验证---》验证是否正确

   2. 准备--》为类的静态变量分配空间，并初始化为默认值。

      static int a=10; 此时赋值 a=0；

   3. 解析--》将类中的符号引用转为直接引用。

      符号引用就说：class文件中的符号描述如magic等。解析为内存的直接地址。
      
      string str=内存地址。

3. 初始化

   为静态变量初始化真正的值。此时赋值 a=10;



### 2.运行时数据区

1. ##### **方法区**

   每个jvm中有一个方法区，线程共享，线程不安全，存储的主要是类信息、常量、静态变量、即使编译之后的代码。

   大小不够的时候会报out of memory error。oom

2. **堆（heap）**

   线程共享的区域。存储的是对象，大小不够的时候会报out of memory error。oom

   对象分为新生代和老年代。经过一次垃圾回收，对象年龄+1，超过15进入老年代，或者创建的对象较大，直接进入老年代。

3. **java虚拟机栈**

   栈的报错就说stack over flow error。
   
   javap -c x.class>x.txt 反编译字节码指令。字节码指令，描述了类文件在虚拟机当中的状态。
   
   栈帧：局部变量、操作数栈（对操作数进行入栈和出栈）、动态链接（每一个栈帧都包含指向运行时常量池中该栈帧所属方法的引用，类的元信息放在方法区的 程序在运行的时候类型才会确定 比如多态）、方法返回地址（方法执行完，继续执行，需要返回到进入方法的位置）。
   
   ~~~java
     public static int calc(int, int);
       Code:
          0: iconst_3--------将int类型常量3压入栈
          1: istore_0--------将int值存储局部变量0
          2: iload_0---------从局部变量0装载int值入栈
          3: iload_1---------从局部变量1装载int值入栈
          4: iadd------------将栈顶int元素弹出栈进行add操作
          5: istore_2--------将栈的值弹出来赋值给局部变量2（有一个局部变量表）
          6: iload_2
          7: ireturn
   ~~~
   
   局部变量可以指向堆 局部变量中的Object obj=new Object（），方法区可以指向堆 static obj=new Object（）
   
   堆也可以指向方法区，涉及java对象的内存布局。
   
4. **java对象的内存布局**

   1. 对象头
      - mark word   标记位，锁状态，分代年龄，哈希码等。 8字节
      - class pointer 指向对象对应的类数据内存地址。8字节
      - length 数组特有，记录长度。4字节
   2. 实例数据
   3. 对齐填充

   

5. **gc**

   young  GC 年轻代垃圾回收  minor gc

   old GC 老年代的垃圾回收 major gc 一般 major gc伴随着minor 

   二者加一起就是full gc

   1. 如何确认垃圾
      - 引用计数法----- 有缺陷 循环引用的问题无法解决。
      - 可达性分析算法----GC Root
      - GC Root
        1. 局部变量表
        2. static成员
        3. 常量
        4. 本地方法栈中的对象
        5. 类加载器
        6. 线程

   
   
6. **回收策略**

   1. 复制算法----少量对象存活的部分

      - 浪费空间。

   2. 标记清除法

      - 会产生大量碎片。
      - 需要执行两次，标记一次，清除一次。效率比较低

   3. 标记整理算法

   4. 垃圾收集器

      1. 线程、针对的算法、针对的哪个代。
         - G1 初始标记（判断哪些是垃圾，暂停用户线程）-》并发标记--》最终标记（标记增量）--》筛选回收
         - CMS concurrent mark sweep 并发标记整理 针对老年底。清除过程：初始标记 单线程，全量标记。-》重新标记（多线程增量更新）-》并发清除 。有一个阈值，整个垃圾回收的时间。在阈值范围内选择性的回收。
         - Serial Old 单线程 复制算法 老年代 标记整理
         - parallel old 多线程 标记整理 老年代
         - Serial GC  单线程 复制算法 会阻塞进程 针对新生代
         - parNew  多线程 复制算法 老年代
         - parallel scavenge  新生代
      2. 收集器分类
         - 并行收集器：parNew parallel 
         - 并发收集器：G1 CMS
         - 串行收集器：serial serial old

      

7. **JVM调优**

   1. GC日志，垃圾回收的停顿时间和吞吐量，降低时间提高吞吐量。
      - 停顿时间：垃圾收集器进行垃圾回收终端的相应时间。G1可以设置停顿时间。停顿时间少的，适合web开发。
      - 吞吐量：代码执行时间/（执行时间+垃圾回收时间）。适合跑后台。
   2. 内存使用的维度。
      - 设置堆大小。
   3. 调优设置
      - jvm自己选择
      - 对于时间有要求使用G1和CMS
      - 调整堆大小
   4. ​	查看使用了什么垃圾回收器方法
      - jps 查看所有进程
      - jinfo -flag UseG1GC 进程号

   

8. **JVM参数**

   1. 标准参数

      不随jdk版本变化而变化，比如 java -version 、java -help

   2. -X参数

      非标准参数，随jdk版本变动而变动，比如Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, mixed mode) 

      jvm 的模式 为混合模式，解释模式和编译模式，通过-X可以设置。

      java -Xint -version 设置为解释模式 Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, interpreted mode)

      java -Xcomp -version 设置为编译模式 Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, compiled mode)

      java -Xmixed -version 设置为混合模式 Java HotSpot(TM) 64-Bit Server VM (build 25.271-b09, mixed mode)

   3. -XX参数

      1. 垃圾回收

         1. Boolean 类型 +/- 启用关闭类型
            1. 串行
               - -XX: +UseSerialGC
               - -XX: +UseSerialOldGC
            2. 并行
               - -XX: +UserPallelGC
               - -XX: +UsePallelOldGC
            3. 并发
               - -XX: +UseG1GC
               - -XX: +UseConcMarkSweepGC
         2. 非Boolean 类型 name=value类型
            1. 设置最大堆内存 -XX:MaxHeapSize=100M
            2. 新老年代比例 NewRatio 2 新老比例为1：2
            3. 发生oom的时候dump一个文件  -XX: +HeapDumpOnOutOfMemoryError -xx:HeapDumpPath=heap.hprof

   4. 其他参数

      1. -Xms100M==> -XX:InitalHeapSize=100M 初始化堆内存
      2. -Xmx100M==>-XX:MaxHeapSize=100M最大堆内存
      3. -Xss100k===>-XX:ThreadStackSize=100k栈深度
      
   5. 查看所有的参数

      1. -XX:+PrintFlagsFinal
      
   6. 修改参数

      1. idea中修改
      2. java -XX:+UseG1GC -xxx.jar 
      3. tomcat 修改配置
      4. 实时修改 jinfo flag name=value  条件是 manageable类型的值
      5. jstat 查看信息的 jstat  进程id -class/gc 1000 10 每个1秒打印1次内存信息或者垃圾回收信息
      6. jstack 进程id 查看线程信息
      7. jmap -heap 进程id 查看堆内存信息 。将堆内存快照 dump成文件。jmap -dump:format=b,file=heap.hprof pid
      
   7. 工具

      1. jconsole
      2. arthas

   8. 调优目标

      1. 堆内存---》oom 通过工具分析

         通过MAT工具分析

      2. 垃圾回收

         通过日志分析

      3. 什么时候发生垃圾回收

         1. minor gc 年轻代或者s区空间不足的时候
         2. major gc 老年代空间不足
         3. -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log
         4. gc查看工具 gc viewer。
      5. CMS垃圾回收。
            1. 初始标记。该阶段进行可达性分析，标记GC ROOT能直接关联到的对象。SWT
         2. 并发标记。由前阶段标记过的绿色对象出发，所有可到达的对象都在本阶段中标记。
            3. 重新标记。暂停所有用户线程，重新扫描堆中的对象，进行可达性分析，标记活着的对象。这个阶段是以新生代中对象为根来判断对象是否存活的。SWT
         4. 并发清理。
   
   9. 优化
   
      1. 调整堆大小 ---》会减少回收次数，但是回收时长会拉伸。因为每次回收垃圾增多。
   
         使用G1GC 设置最长暂停时间。出现没有回收完的情况，会增加回收次数。通过不断调整时间，优化吞吐量和gc耗时。
         
      2. G1GC调休
      
         1. 尽量不调整年轻代大小，不调整比例。
         
      3. 实战优化
      
         1. GC次数频频 怎么办？
            - 查看gc日志。分析问题产生原因。1.内存大小、2垃圾回收器的选择。
      
      4. 内存泄漏和内存溢出的区别
      
         1. 内存泄漏 是内存对象无法获得回收。
         
         2. 内存泄漏会导致内存溢出。
         
         3. 动态年龄计算：Hotspot遍历所有对象时，按照年龄从小到大对其所占用空间进行累积，当累积的某个年龄大小超过
         
            了surivor区的一半时，取这个年龄和MaxTenuringThreshold中更小的一个值，作为晋升年龄的阈值。
