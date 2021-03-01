## spring cloud 源码分析

#### 

#### eureka 

1. 服务发现

   1. 服务发现的顶级是discoveryClient

   eurekaDiscoveryClient继承了该接口，通过组合eurekaClient实现接口功能。

   ~~~java
   //通过服务ID获取实例信息	

   ~~~

   1.  DiscoveryCilent

      1. 实现了eurekaClient接口，而eurekaClient继承了lookupService接口。

         - lookupService 注册和获取实例信息 

           ~~~
           Application getApplication(String appName);
           Applications getApplications();
           List<InstanceInfo> getInstancesById(String id);
           ~~~

         - eurekaClient对其进行了扩展。健康检查、监听更新等。

           ~~~
           public void registerHealthCheck(HealthCheckHandler healthCheckHandler);
           public boolean unregisterEventListener(EurekaEventListener eventListener);
           ~~~

           

         