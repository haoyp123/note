### web容器/servlet容器

~~~
class MyCat{
	List<Servlet> list;
	ServerSocket server=new ServerSocket(8080);
	server.accept();
	将项目中所有的servlet装载到tomcat中进行维护。
	list.add(servets)
}
~~~

#### servlet规范

~~~
public interface Servlet{
	init();
	servletconfig();
	service();
	destroy();
	
}
~~~

#### servlet容器

通过context可以配置web项目的路径，换句话，一个context就包含了一个web项目。

1. tomcat源码中对应的tomcat类

   1. context.java -->standardcontext.loadOnStartUp(); 

      ~~~
      public boolean loadOnStartup(Container children[]) {
      
              // Collect "load on startup" servlets that need to be initialized
              TreeMap<Integer, ArrayList<Wrapper>> map = new TreeMap<>();
              for (int i = 0; i < children.length; i++) {
                  Wrapper wrapper = (Wrapper) children[i];
                  int loadOnStartup = wrapper.getLoadOnStartup();
                  if (loadOnStartup < 0)
                      continue;
                  Integer key = Integer.valueOf(loadOnStartup);
                  ArrayList<Wrapper> list = map.get(key);
                  if (list == null) {
                      list = new ArrayList<>();
                      map.put(key, list);
                  }
                  list.add(wrapper);
              }
      ~~~

   2. 核心组件
   
      1. connector
      2. container