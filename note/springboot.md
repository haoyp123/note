### springboot

##### 1. 入口

~~~java
@SpringBootApplication
public class SpringbootStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootStudyApplication.class, args);
    }

}

~~~

##### 2.优先级

既有properties 和yml的时候，优先加载的是yml

##### 3.配置文件优先级

 

##### 4.srpingboot 整合sevlet

 继承httpServlet 注入sevletBean

##### 5.springboot启动源码分析

1. 创建一个springApplicationd对象

2. 创建SpringApplication主要步骤

   1. 加载资源。默认值为null。

   2. 将主类存放到LinkedHashSet。

   3. 设置容器类型。

   4. 通过java SPI机制加载所有的MATA-INFO/spring.factories 文件。

      ~~~
      SpringFactoriesLoader-->主要是加载文件的
      	loadSpringFactories--> 扫描java包加载MATA-INFO/spring.factories下。
      	
      ~~~

      

      

   5. 实例化 ApplicationContextInitializer类。

      ~~~java
      SpringApplication-->实例化对象
          getSpringFactoriesInstance-->通过反射机制创建 ApplicationContextInitializer的实例。
      ~~~

      

   6. 实例化监听器。

   7. 创建主类

      具体构造函数如下

   ~~~java
   	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   		this.resourceLoader = resourceLoader;
   		设置资源加载器 默认值null
   
   		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
   		
   		this.webApplicationType = WebApplicationType.deduceFromClasspath();
   		设置容器类型servlet reative none
   		
   		this.bootstrapRegistryInitializers = getBootstrapRegistryInitializersFromSpringFactories();
   		加载sprint.factories下的所有类 结构式Map<String List<T>>的kv结构 放到cache中。
   		
   		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
   		生成实例对象。ApplicationContextInitializer 处理applicationContext事件
   		
   		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
   		生成监听对象 处理监听事件
   		
   		this.mainApplicationClass = deduceMainApplicationClass(); 
   		创建主方法所在类
   	}
   
   ~~~

   

3. 加载spring.factories  核心方法式loadspringfactories.加载器是applicationClassloader。

   ~~~java
   	private static Map<String, List<String>> loadSpringFactories(ClassLoader classLoader) {
   		Map<String, List<String>> result = cache.get(classLoader);
   		if (result != null) {
   			return result;
   		}
   
   		result = new HashMap<>();
   		try {
   			Enumeration<URL> urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);
               通过appClassLoader 加载所有的spring.factories内容生成实例
   			while (urls.hasMoreElements()) {
   				URL url = urls.nextElement();
   				UrlResource resource = new UrlResource(url);
   				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
   				for (Map.Entry<?, ?> entry : properties.entrySet()) {
   					String factoryTypeName = ((String) entry.getKey()).trim();
   					String[] factoryImplementationNames =
   							StringUtils.commaDelimitedListToStringArray((String) entry.getValue());
   					for (String factoryImplementationName : factoryImplementationNames) {
   						result.computeIfAbsent(factoryTypeName, key -> new ArrayList<>())
   								.add(factoryImplementationName.trim());
   					}
   				}
   			}
   
   			// Replace all lists with unmodifiable lists containing unique elements
   			result.replaceAll((factoryType, implementations) -> implementations.stream().distinct()
   					.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));
   			cache.put(classLoader, result);
   		}
   		catch (IOException ex) {
   			throw new IllegalArgumentException("Unable to load factories from location [" +
   					FACTORIES_RESOURCE_LOCATION + "]", ex);
   		}
   		return result;
   	}
   ~~~

4. 实例化完springbootApplication后，调用run方法。





##### 5.springboot的自动注入原理

```java
1.@SpringBootApplication
2.@EnableAutoConfiguration
    该注解有import方法 导入了AutoConfigurationImportSelector.class
@Import注解是用来导入配置类或者一些需要前置加载的类.这些类需要有@configuration 或者实现importSelector 或者实现
    importBeanDefinitionRegister
3.AutoConfigurationImportSelector
        1.getAutoConfigurationEntry-->
        List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);--获取所有的加载信息 通过SpringFactoriesLoader
        
使用类加载器加载所有FACTORIES_RESOURCE_LOCATION(META-INF/spring.factories)的信息保存在缓存中。
```



##### 6. run方法

创建完springbootApplication后会执行run 方法。

~~~java
	public ConfigurableApplicationContext run(String... args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start(); //设置启动实际
		DefaultBootstrapContext bootstrapContext = createBootstrapContext();//创建上下文。
		ConfigurableApplicationContext context = null;
		configureHeadlessProperty();//设置系统变量
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting(bootstrapContext, this.mainApplicationClass);//观察者模式
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);//设置参数
			ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);设置环境变量
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment); //打印banner
			context = createApplicationContext();//创建上下文
			context.setApplicationStartup(this.applicationStartup);
			prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);//准备上下文
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}
~~~



