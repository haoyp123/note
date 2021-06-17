spring 源码分析



 spring  两块 IOC  和AOP。

#### 1.IOC

1. ioc 控制反转

   1. ioc 容器

      ~~~xml
      <beans>
      	<bean id='abc' class=''></bean>
      </beans>
      ~~~

       ApplicationContext ac=new ClasspathXmlApplicationContext("bean.xml");

      Bean  b=ac.getBean("abc");

      大致加载流程为

      加载XML--》解析XML--》封装为beanDefinition对象--》实例化--》初始化--》交由容器管理。

      - 容器使用类型存放对象？---》map。

        map的k和v k:string v:object  、k class:v object 、k string :v objectFactory 、k string： v beandefintion

   2. 文件格式

      文件类型有很多

      xml

      yml

      properties

      注解

      。。。

      bean的来源有很多类型，最终都需要加载成为一个BeanDefinition

      在解析操作封装为beanDefinition对象中间需要一个抽象层，用来方便扩展。

      BeanDefinitionReader 来进行解析。

   3. 容器入口

      获取完beanDefinintion 后 通过反射创建对象。没有扩展性。

      所有有一个接口 BeanFactory接口 ，他是bean工厂，是整个容器的根接口，是容器的入口。

      The root interface for accessing a Spring bean container.

   4. 对于bean的后置处理

      如何在程序运行时候，动态的对bean进行扩展 ,如何动态的替换${jdbc.url}

      比如

      ~~~xml
      <bean>
          <property name=url value=${jdbc.url}></property>
      </bean>
      ~~~

      spring提供了两个重要的bean的扩展接口 beanFactoryPostProcessor 和beanPostProcessor

      beanFactoryPostProcessor是bean实例化之前的增强，对beanDefinition的增强

      beanPostProcessor有两个方法，一是before 一个是after 分别是bean实例化后调用init方法前后执行的逻辑。

      ~~~java
      beanFactoryPostProcessor 接口定义的方法 
      	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      beanPostProcessor接口定义的方法
          postProcessBeforeInitialization
          postProcessAtferInitialization
          
      ~~~

      

   5. 实例化和初始化

      实例化和初始化构成了对象的创建。

      - 实例化就是在堆出开辟一块空间，此时属性都是默认值。

      - 初始化给属性设置值。填充属性，执行初始化方法。

      - 实例化和初始化之间的事情
        1. 填充属性
        2. 设置aware接口
        3. beanPostProcessor Before
        4. init()
        5. beanPostProcessor After

        上述过程也是bean的生命中期（创建时）

        

   6. beanPostProcessor有什么作用呢？

      1. AOP作为扩展使用的

   7. 在不同阶段处理不同的事情应该如何处理？观察者模式

   8. beanFactory和factoryBean 有什么区别

      1. 都是用来创建对象的

      2. beanFactory是spring Bean的根接口为容器创建对象定义了规范。factoryBean 实际是bean，

         通过getObject()方法来获取具体对象，整个创建过程用户可以自己掌控，更加灵活。

   9. 常见接口

      1. beanFactory
      2. aware
      3. beandeFinition
      4. beandefinitionReader
      5. beanFactoryPostProcessor
      6. beanPostProcessor
      7. enviroment
      8. factoryBean

   10. 打发打发

   

2. di 依赖注入

