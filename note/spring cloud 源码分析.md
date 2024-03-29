## spring cloud 源码分析

#### 网约车的目标

1. ##### 乘客端

   1. 登录注册
   2. 短信验证，三档验证防止恶意发短信
   3. 查看开通区域
   4. 纠偏。

2. ##### 司机端

   1. 发送验证码
   2. 登录注册
   3. 查看，改变司机状态。
   4. 司机抢单（分布式锁）
   5. 订单状态的变更
   6. 发起收款

3. ##### 微服务的设计

   1. 架构，模式，隔离的目标是什么：**隔离系统的变化点**。
   2. 具体点。
      - 高内聚，低耦合。订单派单，撤单
      - 高度自治。每个服务都是独立服务，开发部署测试以及运行，无状态。
   3. 以业务为中心
   4. 弹性设计。容错 降级 隔离
   5. 自动化。持续集成，持续交付。
   6. 粒度把控。任何服务不要因为自己的服务发布，影响到别的服务。

4. ##### 如何抗住高并发

   1. x轴：通过部署多实例，上层有负载均衡。
   2. y轴：把单体的大功能拆分出多个小功能。
   3. z轴：数据分区。北京，天津，上海等。
   
5. ##### 架构设计

   展示层   web、小程序 等等

   负载层   软件  nginx  硬件 f5

   网关层   zuul gateway  限流 黑白名单 鉴权

   业务层   各种api 乘客api  司机api boos api 听单 api

   能力层   各种服务 用户服务 订单服务 应用更新管理  短信验证码等

   存储层   
   
6. ##### 流程介绍

   用户--》nginx集群--》网关--》api--》技术熔断

7. ##### 服务的拆分

   业务层

   | 模块     | 项目名称      | 描述     |
   | -------- | ------------- | -------- |
   | 乘客端   | api-passenger | 乘客端   |
   | 司机端   | api-driver    | 司机端   |
   | 司机听单 | api-listener  | 司机听单 |

   能力层

   | api升级      | service-app-update        |
   | ------------ | ------------------------- |
   | 订单         | service-order             |
   | 派单         | service-order-dispatch    |
   | 乘客用户管理 | service-passenger-user    |
   | 短信         | service-sms               |
   | 计价         | service-valuation         |
   | 验证码       | service-verification-code |
   | 钱包         | service-wallet            |
   |              |                           |
   |              |                           |

   

8. ##### 技术栈

   boot cloud maven git  mysql  redis mq

   第三方：

   短信：阿里

   语音：

   文件oss

   消息推送 极光

   支付：微信

   航旅纵横：查航班

   发票：百望云

9. ##### 接口设计

   协议：http协议  rest风格

   域名：/restapi.yuming.com

   版本：v1

   路径：

   动作：post  put  delete get

10. ##### 微服务的项目结构

    项目在独立的仓库中

    整体设计

    |--online text three

    ​	|-- 项目a

    ​	|--项目b

    单独服务设计

    |--pom

    |--src

    ​	|--controller

    ​	|--service

    ​		impl

    ​		interface

    ​	|--dao

    ​		entity

    ​		mapper

    ​	|--manager 对service的一些沉淀

    ​	|--constant 放置常量

    ​	|--request

    ​	|--response

11. ##### 异常处理

    dao层：不打日志，catch抛出 

    service层： 打日志 相信信息 时间 参数

    controller层：封装成状态码 返回

#### 1.eureka--注册中心

1. ##### 	eureka 原理

   

   ~~~
   @EnableEurekaServer  创建了一个空的bean。 这个bean 作为一个条件，加载eurekaserver的条件。
   @Configuration(proxyBeanMethods = false)
   public class EurekaServerMarkerConfiguration {
   
   	@Bean
   	public Marker eurekaServerMarkerBean() {
   		return new Marker();
   	}
   
   	class Marker {
   
   	}
   
   }
   ~~~

   

2. 

   

​    











