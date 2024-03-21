# Java SPI解读：揭秘服务提供接口的设计与应用

# 一、什么是SPI


在 Java 编程中，SPI（Service Provider Interface）是实现**可插拔**式应用的一种机制。它就像是应用程序的魔法盒，让你可以随时添加新的功能实现，而不需搞得一团糟。通过SPI，我们可以在运行时**动态加载**具体的服务实现，这意味着你的应用程序可以像变戏法一样，轻松地变身成不同的形态。

SPI的关键特性就是**可插拔性**和**动态加载**。这意味着你可以随心所欲地向应用程序添加新功能，而不会破坏原有的代码。这两个关键点也是开发人员常常提及的，让我们来解开这些概念的迷雾，首先要从 API 开始说起。




## 1.1、API

API是应用程序接口（Application Programming Interface）的缩写，它是开发者编写定义的一组接口和与之对应的众多实现类，外部用户可以根据需要选择具体的实现方式。

看到接口是不是想到了SPI中提到的可插拔性？没错，这确实与面向接口编程有关。举个例子：

```java
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // 面向接口编程
        List<String> myList = new ArrayList<>();
        myList.add("Hello");
        myList.add("World");
        for (String str : myList) {
            System.out.println(str);
        }
    }
}
```

在这个例子中，我们创建了一个 `ArrayList` 集合，但是引用却指向了 `List` 接口。为什么要这样做呢？这是为了让程序更具扩展性。假设 `myList` 变量后续需要使用不同的集合类，我们只需修改新的集合实现类即可，而不需要修改后续其他代码。比如：

```java
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // 面向接口编程
        List<String> myList = new ArrayList<>();
		// 切换集合
        myList = new LinkedList<>();
        
        myList.add("Hello");
        myList.add("World");
        for (String str : myList) {
            System.out.println(str);
        }
    }
}
```

看起来比较优雅，说它是可插拔也算合适。但这里实现了动态加载吗？请注意，这里说的动态加载并不是Java的多态：

- **动态加载（Dynamic Loading）：** 是指在程序运行时根据需要加载类或模块的过程。这通常发生在运行时，而不是在编译时确定。动态加载可以使程序更加灵活，可以根据需要加载不同的类，以及在程序运行期间根据条件决定加载哪些类。

- **多态（Polymorphism）：** 是面向对象编程的一个重要概念，它允许不同的子类对象以自己的方式来实现父类的方法。多态性是通过方法的重写和动态绑定来实现的。

> 我们上面的代码展示了多态，但并没有实现动态加载，因为用户仍然需要手动更改具体实现类。

这种由 JDK 提供的接口和各种实现类，被称为 API；这种方式实现了可插拔性，但并没有实现动态加载，设计结构如下图：

![image01.png](picture%2Fimage01.png)

> 了解了 API 的概念，接下来我们讨论SPI



##  1.2、SPI

1. SPI（Service Provider Interface）是在JDK 6版本后引入的一项新特性。它通过接口、约定和动态加载的方式，实现了模块之间无需更改代码便可无缝衔接的功能。其中，最典型的例子是JDBC。JDK提供了数据库连接的接口，各个数据库厂商根据这个接口实现了不同的逻辑。开发人员在使用时，只需要依赖对应的数据库驱动即可。他们编写的代码仍然是基于JDK提供的接口，而无需更改代码形式。这种设计使得应用程序更加灵活、可扩展，并且降低了对特定数据库的依赖。如下：

``` java
// 使用的是jdk提供的接口, 在Java 6及以上版本，符合SPI的驱动包不再需要显式调用Class.forName()来加载驱动程序
import java.sql.*;

public class SimpleJDBCExample {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/mydatabase";
        String user = "username";
        String password = "password";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM mytable")) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println("ID: " + id + ", Name: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

```

2. SPI主要通过三个步骤来实现各应用无缝衔接的功能：

    - **接口**：应用需要提供一个抽象接口，供其他模块进行实现，例如在JDK中的`Connection`接口。
    - **约定**：其他模块需要在其jar包的`META-INF/services/`目录下创建一个以服务接口命名的文件，文件中列出了实现该接口的具体类的全限定名称。
    - **动态加载**：JDK 6引入了`ServiceLoader`类，它的主要功能是检测所有jar包里的`META-INF/services`目录下的文件，并创建对应的实现类。

3. 以上理论太过抽象，来个示例：

    1. 定义一个Connection接口

       ``` java
       public interface Connection {
           String getName();
       }
       ```

    2. 创建`MysqlConnection`实现类

       ``` java
       public class MysqlConnection implements Connection {
           @Override
           public String getName() {
               return "MysqlConnection";
           }
       }
       ```

    3. 创建`OracleConnection`实现类

       ``` java
       public class OracleConnection implements Connection {
           @Override
           public String getName() {
               return "OracleConnection";
           }
       }
       ```

    4. 新建`META-INF/services`目录并创建`org.apache.spi.example.jdbc.Connection`文件

       ``` 
       resources
           └─META-INF
               └─services
                   └─org.apache.spi.example.jdbc.Connection
       ```

    5. 将实现类全限定名写入`org.apache.spi.example.jdbc.Connection`文件中，如下：

       ``` 
       org.apache.spi.example.jdbc.MysqlConnection
       org.apache.spi.example.jdbc.OracleConnection
       ```

    6. 编写测试类

       ``` java
       public class Example {
           @Test
           public void example01() {
               ServiceLoader<Connection> serviceLoader = ServiceLoader.load(Connection.class);
               for (Connection search : serviceLoader) {
                   System.out.println(search.getName());
               }
           }
       }
       ```

       结果打印：

       ``` 
       MysqlConnection
       OracleConnection
       ```

4. 至于为什么将配置文件放在META-INF/services下面，原因在于`ServiceLoader`代码中固定了文件扫描路径(约定)，如下：

   ```text
   private static final String PREFIX = "META-INF/services/"
   ```

5. 以上便是SPI的基本使用，通过约定配置、面向接口编程以及`ServiceLoader`实现了不同模块的可插拔性，设计结构如下图：

![image02.png](picture%2Fimage02.png)

# 二、对比其他实现方式

看到这里有的读者可能会有疑问，SPI是否可以通过反射或Spring的自动装配来实现上述需求？答案是肯定的。SPI的动态加载实际上就是使用了反射来实现。但值得注意的是，**SPI本身是一种设计思想**，它通过接口、约定和动态加载来实现模块之间的解耦和扩展性。

在JDK 6及以上版本中，SPI提供了一种默认实现，但开发人员完全可以根据自己的定制化需求，按照公司内部的约定来定义配置文件。

为了更好地理解SPI的设计思想，下面将对比几种常见的实现方式。

## 3.1、反射

反射是一种在运行时动态获取类的信息并调用其方法或创建其实例的机制。然而，光靠反射是无法实现SPI的设计思想的，因为反射本身缺乏约定性；在使用反射时，首先需要确定要加载的范围，即包名，示例如下：

``` java
import java.lang.reflect.*;

public class ReflectionExample {
    public static void main(String[] args) {
        String packageName = "java.util"; // 指定的包名
        String interfaceName = "List"; // 指定的接口名

        try {
            Class<?>[] classes = Package.getPackage(packageName).getClasses();
            for (Class<?> clazz : classes) {
                if (clazz.getInterfaces().length > 0 && clazz.getInterfaces()[0].getSimpleName().equals(interfaceName)) {
                    System.out.println("Found class implementing interface " + interfaceName + ": " + clazz.getSimpleName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

虽然上面的示例展示了如何在指定包名下查找实现了特定接口的类，但这种方式存在一定局限性。对于自身应用来说，还相对好处理，因为我们自己开发的包名是确定的。但对于其他实现同一接口的第三方jar包，我们无法事先知道其包名，因此无法直接使用这种方式来加载其他jar包的实现类。

此外，与自行通过反射实现相比，JDK 6提供的ServiceLoader类更为便捷和高效。它是SPI设计思想的默认实现方式，能够自动加载指定接口的所有实现类。`ServiceLoader`会搜索所有jar包下是否存在`META-INF/services`目录，并读取这些目录下的配置文件来获取实现类的信息。这意味着即使这些实现类分布在应用程序所依赖的不同jar包中，`ServiceLoader`也能够加载它们。这使得使用JDK提供的`ServiceLoader`类更加简便，避免了重复造轮子的问题，提高了开发效率。



## 3.2、Spring-IOC

Spring的IOC（Inversion of Control）和SPI（Service Provider Interface）是两种不同的设计思想，它们在目的和应用场景上有所不同：

1. IOC（Inversion of Control）：

    - IOC是Spring框架的核心概念之一，它指的是控制反转，即将对象的创建和依赖关系的维护交给Spring容器管理。

    - 在IOC中，开发者将对象的创建和依赖关系的维护交给Spring容器，通过配置文件或注解来声明Bean的依赖关系，而不需要手动创建和管理对象。

    - 其作用范围是在其自身开发应用`application类`或`@ComponentScan`扫描范围内，如下:

      ``` java
      import org.springframework.context.annotation.ComponentScan;
      import org.springframework.context.annotation.Configuration;
      
      @Configuration
      @ComponentScan(value = {"tech.qifu.jinke.yushu.dam", "tech.qifu.jinke.yushu.dis"})
      public class YushuDamAppAutoConfiguration {
      }
      ```

2. SPI（Service Provider Interface）：

    - SPI是一种Java设计模式，它通过接口、约定和动态加载来实现模块之间的解耦和扩展性。
    - 在SPI中，应用接口由平台或框架定义，不同的模块或厂商可以根据接口实现自己的逻辑，然后通过约定的方式将实现类注册到框架中。
    - 其作用范围在应用及应用所依赖所有jar包范围内，搜索所有jar包下是否存在`META-INF/services`目录，并读取这些目录下的配置文件来获取实现类的信息。

因此，IOC和SPI虽然都是用于降低程序的耦合度，但它们的实现方式和应用场景是不同的。IOC主要用于管理对象的创建和依赖关系，而SPI主要用于实现多模块之间的解耦和扩展性。在Spring框架中，IOC和SPI常常结合使用，以实现更灵活、可扩展的应用架构。

此外，使用IOC必须要引入Spring相关依赖，并且引入了一定的运行时开销。



## 3.3、spring.factories

`spring.factories`是Spring框架中的一种特殊配置文件，用于自动化配置和加载Spring应用中的扩展点。

在Spring Boot应用中，`spring.factories`文件通常位于`META-INF/spring.factories`路径下。这个文件使用标准的Java properties格式，其中包含了各种Spring应用中需要自动化加载的配置信息，如下图：

![image-20240320180210440](C:\Users\gaoliang1-jk\AppData\Roaming\Typora\typora-user-images\image-20240320180210440.png)

开发者可以在`spring.factories`文件中注册各种扩展点，例如自定义的`EnableAutoConfiguration`、`BeanFactoryPostProcessor`、`ApplicationListener`等。这些扩展点可以是自己编写的类，也可以是第三方库提供的。

Spring框架在启动时会自动扫描所有jar包中`META-INF/spring.factories`文件中定义的扩展点，从而实现自动化配置和加载。这使得Spring应用的开发和管理更加简便，可以方便地集成各种第三方库和自定义功能。

从设计思想上看和SPI机制很像，只是约定文件从`META-INF/services/`变为`META-INF/spring.factories`，且spring将所有自定义扩展整合到一个配置文件中，故该方式又被称为：Spring中SPI机制。



# 四、实操

## 4.1、JDBC示例

1. jdbc是SPI的典型应用，这里我们简单模拟一下，这里使用maven工具创建三个model模块，结构如下：

   ``` 
   spi-examples
   	└─spi-jdbc
   	└─spi-mysql-connector
   	└─spi-oracle-connector
   ```

2. `spi-jdbc`模块中创建:`Connection`接口及`DriverManager`类，代码如下：

   ``` java
   public interface Connection {
       String getName();
   }
   ```

   ``` java
   public class DriverManager {
   
       public static void getConnection() {
           ServiceLoader<Connection> connectionLoader = ServiceLoader.load(Connection.class);
           for (Connection connection : connectionLoader) {
               System.out.println(connection.getName());
           }
       }
   }
   ```

3. `spi-mysql-connector`模块pom.xml 依赖 `spi-jdbc`模块，并创建`MysqlConnection`实现类，代码如下：

   ``` java
   import org.apache.spi.employ.jdbc.Connection;
   
   public class MysqlConnection implements Connection {
       @Override
       public String getName() {
           return "MysqlConnection";
       }
   }
   ```

4. `spi-mysql-connector`模块`resources`文件下创建`META-INF/services/org.apache.spi.employ.jdbc.Connection`文件:

   ``` 
   org.apache.spi.realize.jdbc.mysql.MysqlConnection
   ```

5. 接着在`spi-oracle-connector`模块pom.xml 依赖 `spi-jdbc`模块，并创建`OracleConnection`实现类，代码如下：

   ``` java
   import org.apache.spi.employ.jdbc.Connection;
   
   public class OracleConnection implements Connection {
       @Override
       public String getName() {
           return "OracleConnection";
       }
   }
   ```

6. 在`spi-oracle-connector`模块`resources`文件下创建`META-INF/services/org.apache.spi.employ.jdbc.Connection`文件:

   ``` 
   org.apache.spi.realize.jdbc.oracle.OracleConnection
   ```

7. 以上准备工作完成，接下来我们模拟用户使用，创建一个`spi-user`模块，pom中依赖`spi-mysql-connector`模块，如下：

   ``` xml
   <artifactId>spi-user</artifactId>
   
   <dependencies>
       <dependency>
           <groupId>org.example</groupId>
           <artifactId>spi-mysql-connector</artifactId>
           <version>1.0-SNAPSHOT</version>
       </dependency>
   </dependencies>
   ```

8. `spi-user`中编写测试类，如下:

   ``` java
   import org.apache.spi.employ.jdbc.DriverManager;
   import org.junit.Test;
   
   public class Example {
       @Test
       public void example() {
           DriverManager.getConnection();
       }
   }
   ```

9. 此时打印结果为：MysqlConnection，证明已经成功注入`MysqlConnection`。

10. 接下来我们更改`spi-user`的pom依赖为`spi-mysql-connector`，如下:

    ``` xml
    <artifactId>spi-user</artifactId>
    
    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>spi-oracle-connector</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    ```

11. 此时运行测试类`Example`，结果打印为：OracleConnection，证明已经成功注入`OracleConnection`。

**注意：此时`spi-user`模块的代码没有任何修改，却可以根据不同的依赖包灵活的使用不同的连接器，这便是SPI的可插拔性及动态加载特性！**



## 4.2、SPI实现IOC

在上述 JDBC 示例中，我们展示了跨 JAR 包之间通过该SPI动态注入的示例。然而，SPI 也可以在应用自身的 JAR 包中发挥作用。在大数据领域，特别是大数据开发的程序，除了服务应用外，通常不会使用 Spring 相关的依赖。但是，Spring 的 IOC（控制反转）功能确实非常便利。在这种情况下，我们可以通过 SPI 来实现控制反转，从而提高程序的可读性和可扩展性。示例如下：

1. 新增`spi-self`模块，创建`Identity`及`Service`接口，如下：

   ``` java
   public interface Identity {
       String getIdentity();
   }
   ```

   ``` java
   public interface Service extends Identity {
       void execute();
   }
   ```

   > Identity 接口用于区分多个 Service 实现类，类似于 Spring 中的 Bean ID。

2. 创建`CommodityServiceImpl`及`OrderServiceImpl`实现类，如下：

   ``` java
   import org.apache.spi.self.service.Service;
   
   public class OrderServiceImpl implements Service {
       @Override
       public void execute() {
           System.out.println("OrderServiceImpl");
       }
   
       @Override
       public String getIdentity() {
           return "OrderServiceImpl";
       }
   }
   ```

   ``` java
   import org.apache.spi.self.service.Service;
   
   public class CommodityServiceImpl implements Service {
       @Override
       public void execute() {
           System.out.println("CommodityServiceImpl");
       }
   
       @Override
       public String getIdentity() {
           return "CommodityServiceImpl";
       }
   }
   ```

3. 在`resources`文件下创建`META-INF/services/org.apache.spi.self.service.Service`文件，内容如下:

   ``` 
   org.apache.spi.self.service.impl.CommodityServiceImpl
   org.apache.spi.self.service.impl.OrderServiceImpl
   ```

4. 创建`PluginDiscovery`功能类，通过SPI获取实现类并对外提供两个获取接口函数，代码如下:

   ``` java
   import org.apache.spi.self.service.Service;
   
   import java.util.ServiceLoader;
   
   public class PluginDiscovery {
   
       /** 按类型匹配, 取默认第一个 */
       public static Service discoveryService() {
           ServiceLoader<Service> serviceLoader = ServiceLoader.load(Service.class);
           return serviceLoader.iterator().next();
       }
   
       /** 按类型及ID匹配 */
       public static Service discoveryService(String id) {
           ServiceLoader<Service> serviceLoader = ServiceLoader.load(Service.class);
           for (Service service : serviceLoader) {
               if (service.getIdentity().equalsIgnoreCase(id)) {
                   return service;
               }
           }
           throw new RuntimeException(String.format("not find Id:%s Service Class", id));
       }
   
   }
   ```

5. 创建测试类`Example`，代码如下:

   ``` java
   import org.apache.spi.self.service.Service;
   import org.junit.Test;
   
   public class Example {
       @Test
       public void example01() {
           Service service = PluginDiscovery.discoveryService();
           service.execute();
       }
   
       @Test
       public void example02() {
           Service service = PluginDiscovery.discoveryService("OrderServiceImpl");
           service.execute();
       }
   }
   ```

6. 最终结果正确打印，我们成功通过类型和 ID 匹配到了相应的实现类。这一切都是通过 SPI 间接实现了类似于 Spring IOC 的功能，使得代码更加简洁、灵活，增强了程序的可维护性和可扩展性。



## 4.3、注解实现SPI

以上两个示例均需要手动编写 `META-INF/services` 配置文件，这种人工操作不仅耗时，还会增加出错的风险。因此，我们需要一个可以自动生成 SPI 配置文件并自动写入实现类的工具来简化这个过程。幸运的是，谷歌提供了一个名为 `auto-service-annotations` 的包，可以帮助我们实现这一需求。示例如下：

1. 新增`spi-self-auto`模块，pom依赖如下：

   ``` xml
   <artifactId>spi-self-auto</artifactId>
   
       <properties>
           <auto-service.version>1.1.1</auto-service.version>
       </properties>
   
       <dependencies>
           <dependency>
               <groupId>com.google.auto.service</groupId>
               <artifactId>auto-service-annotations</artifactId>
               <version>${auto-service.version}</version>
           </dependency>
           <dependency>
               <groupId>com.google.auto.service</groupId>
               <artifactId>auto-service</artifactId>
               <version>${auto-service.version}</version>
           </dependency>
       </dependencies>
   ```

2. `Identity` 和 `Service` 接口以及 `PluginDiscovery` 类与之前示例中的 `spi-self` 模块保持一致。不过，我们重新编写了 `CommodityServiceImpl` 和 `OrderServiceImpl` 实现类，并在它们上面添加了 `@AutoService` 注解，代码如下：

   ``` java
   import com.google.auto.service.AutoService;
   import org.apache.spi.auto.service.Service;
   
   @AutoService(Service.class)
   public class OrderServiceImpl implements Service {
       @Override
       public void execute() {
           System.out.println("OrderServiceImpl");
       }
   
       @Override
       public String getIdentity() {
           return "OrderServiceImpl";
       }
   }
   ```

   ``` java
   import com.google.auto.service.AutoService;
   import org.apache.spi.auto.service.Service;
   
   @AutoService(Service.class)
   public class CommodityServiceImpl implements Service {
       @Override
       public void execute() {
           System.out.println("CommodityServiceImpl");
       }
   
       @Override
       public String getIdentity() {
           return "CommodityServiceImpl";
       }
   }
   ```

3. 创建测试类`Example`，代码如下:

   ``` java
   import org.apache.spi.auto.service.Service;
   import org.junit.Test;
   
   public class Example {
       @Test
       public void example01() {
           Service service = PluginDiscovery.discoveryService();
           service.execute();
       }
   
       @Test
       public void example02() {
           Service service = PluginDiscovery.discoveryService("OrderServiceImpl");
           service.execute();
       }
   }
   ```

4. 最终结果正确打印。在这个示例中，我们没有手动编写 `META-INF/services` 配置文件，而是通过 `@AutoService` 注解，在编译期间自动生成了配置文件，并将其存放在 `target/classes` 目录下，结构如下：

   ``` 
   target
      └─classes
   	  └─META-INF
   	      └─services
   	          └─org.apache.spi.auto.service.Service
   ```

5. 而在package/install 操作时则会自动将该配置类打入jar包中，这样的结构使得SPI的使用更加便捷，减少了手动维护的工作，同时确保了代码的可读性和可维护性。



# 五、总结

SPI（Service Provider Interface）是一种用于实现组件化和插件化的 Java 标准。通过 SPI，开发者可以定义服务接口，并允许外部实现这些接口，然后在运行时动态加载并使用这些实现。以下是 SPI 的主要特点和总结：

1. **灵活性和可扩展性：** SPI 允许系统在运行时动态地加载并使用外部实现，从而增加了系统的灵活性和可扩展性。系统可以根据需求动态选择和加载合适的实现，而无需在代码中显式指定。
2. **松耦合：** SPI 通过接口和实现类的分离，实现了组件之间的松耦合。组件之间只通过接口进行通信，而不直接依赖具体的实现，使得组件更易于替换和升级。
3. **自动发现机制：** SPI 提供了自动发现机制，使得系统可以自动扫描并加载符合条件的实现类。开发者只需在实现类上添加特定的注解或者遵循约定，就可以实现自动注册和加载。
4. **标准化：** SPI 是 Java 标准库提供的一种机制，因此具有良好的兼容性和稳定性。开发者可以借助 SPI 实现应用程序的插件化，而无需依赖第三方框架或库。
5. **易用性：** SPI 的使用相对简单，只需要定义接口、实现接口并添加特定的注解或配置即可实现插件的加载和使用，无需复杂的配置和编码。

总的来说，SPI 是一种强大的机制，可以帮助开发者实现组件化和插件化，提高系统的灵活性、可扩展性和可维护性，是 Java 开发中常用的设计模式之一。



# 六、相关资料

- [SPI官网介绍](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html)

- [Difference between SPI and API?](https://stackoverflow.com/questions/2954372/difference-between-spi-and-api)

  