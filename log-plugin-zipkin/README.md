# 部署zipkin服务

## 1、 使用Zipkin官方的Shell下载，使用如下命令可下载最新版本：

```
# 下载最新版本
curl -sSL https://zipkin.io/quickstart.sh | bash -s
# 启动zipkin服务  下载下来的文件名为 `zipkin.jar`
java jar zipkin.jar
```

## 2、 通过docker安装，命令如下：

```
docker run -d -p 9411:9411 openzipkin/zipkin
```

安装好后，使用浏览器访问9411端口，主页面如下所示：

![image-20200109103438048](https://jessica-1259671334.cos.ap-chengdu.myqcloud.com/zipkin/image-20200109103438048.png)

# 部署zipkin客户端

在各个微服务中将之前的sleuth依赖替换成如下依赖：

```xml
<!-- 这个依赖包含了sleuth和zipkin -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

在各个微服务配置文件application.yml中，增加zipkin相关的配置项。如下：

```yaml
spring:
  ...
  zipkin:
    base-url: http://127.0.0.1:9411/  # zipkin服务器的地址
    # 关闭服务发现，否则Spring Cloud会把zipkin的url当做服务名称       
    discoveryClientEnabled: false   
    sender:
      type: web  # 设置使用http的方式传输数据
  sleuth:
    sampler:
      probability: 1  # 设置抽样采集率为100%，默认为0.1，即10%      
```

配置好后重启项目，并访问接口。到zipkin页面上就可以查看到服务的链路信息了：

# Zipkin数据持久化

Zipkin默认是将监控数据存储在内存的，如果Zipkin挂掉或重启的话，那么监控数据就会丢失。所以如果想要搭建生产可用的Zipkin，就需要实现监控数据的持久化。而想要实现数据持久化，自然就是得将数据存储至数据库。好在Zipkin支持将数据存储至([官网文档](https://github.com/openzipkin/zipkin#storage-component))：

- 内存（默认）
- MySQL
- Elasticsearch

## 1、 Elasticsearch持久化

[官网文档:elasticsearch-storage](https://github.com/openzipkin/zipkin/tree/master/zipkin-server#elasticsearch-storage)

[官网文档:zipkin-storage/elasticsearch](https://github.com/openzipkin/zipkin/tree/master/zipkin-storage/elasticsearch)

搭建好Elasticsearch服务后，启动：

```bash
STORAGE_TYPE=elasticsearch ES_HOSTS=localhost:9200 java -jar zipkin.jar
```

**Tips：**

- 其中，`STORAGE_TYPE`和`ES_HOSTS`是环境变量，`STORAGE_TYPE`用于指定Zipkin的存储类型是啥；而`ES_HOSTS` 则用于指定Elasticsearch地址列表，有多个节点时使用逗号（ `,` ）分隔。关于其他环境变量，可参考官方文档 [environment-variables](https://github.com/openzipkin/zipkin/tree/master/zipkin-server#environment-variables)

# 服务依赖关系图

Zipkin的数据持久化，并整合了Elasticsearch作为Zipkin的存储数据库。但此时会有一个问题，就是Zipkin在整合Elasticsearch后会无法分析服务之间的依赖关系图，因为此时数据都存储到Elasticsearch中了，无法再像之前那样在内存中进行分析。

如果使用了非内存持久化方式，需要下载并使用Zipkin的一个子项目：[Zipkin Dependencies](https://github.com/openzipkin/zipkin-dependencies)

## 部署Zipkin Dependencies子项目

又要Zipkin Dependencies属于是一个job，不是服务，不会持续运行，而是每运行一次才分析数据。所以想持续运行的话，需要自己写个定时脚本来定时运行这个job

1. 使用官方的Shell下载，使用如下命令可下载最新版本：

   ```shell
   # 下载最新版
    curl -sSL https://zipkin.io/quickstart.sh | bash -s io.zipkin.dependencies:zipkin-dependencies:LATEST zipkin-dependencies.jar
    # 运行 分析当天数据
    STORAGE_TYPE=elasticsearch ES_HOSTS=localhost:9200 java jar zipkin-dependencies.jar
   
   ```

2. 通过docker下载并运行，命令如下：

```shell
docker run --env STORAGE_TYPE=elasticsearch --env ES_HOSTS=192.168.190.129:9200 openzipkin/zipkin-dependencies
```

Zipkin Dependencies默认分析的是当天的数据，可以通过如下命令（Linux）让Zipkin Dependencies分析指定日期的数据：

- 分析昨天数据

```shell
# 非容器 方式
STORAGE_TYPE=elasticsearch ES_HOSTS=localhost:9200 java jar zipkin-dependencies.jar `date -u -d '1 day ago' + F%`
```

- 分析指定日期数据

```shell
STORAGE_TYPE=elasticsearch ES_HOSTS=localhost:9200 java jar zipkin-dependencies.jar 2018-08-08
```

## 定时运行Zipkin Dependencies子项目

1. 编辑auto-update-zikpin-dependencies.sh shell脚本

```shell
#!/bin/sh
STORAGE_TYPE=elasticsearch ES_HOSTS=localhost:9200 java jar zipkin-dependencies.jar
```

2. 赋予可执行权限

   ```
   chmod +x auto-update-zikpin-dependencies.sh
   ```

3. 设置定时任务

```shell
# 进入定时任务编辑界面
crontab -e
# 将auto-update-zikpin-dependencies.sh执行脚本加入到系统计划任务，到点自动执行 每天凌晨30清理日志
30 0 * * * /root/auto-update-zikpin-dependencies.sh
# 使任务生效
/sbin/service crond restart
```

**crontab中的同步任务时而成功，时而不成功，什么原因呢？定位问题的关键点，就要通过日志来分析**

- linux

看 /var/log/cron这个文件就可以，可以用tail -f /var/log/cron观察

- unix

在 /var/spool/cron/tmp文件中，有croutXXX001864的tmp文件，tail 这些文件就可以看到正在执行的任务了。

- mail任务

在 /var/spool/mail/root 文件中，有crontab执行日志的记录，用tail -f /var/spool/mail/root 即可查看最近的crontab执行情况。