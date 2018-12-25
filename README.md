README.md
====
akka-kafka-cluster-subpub简介
----
# 初始化
本项目使用maven或sbt进行依赖更新  
在此之外需要使用zookeeper与kafka进行测试，当前版本zookeeper-3.4.13与kafka_2.12-2.1.0
# 执行
使用sbt run执行，命令行可以看到不同topic数据由不同集群的actor进行消费

# TODO
使用device进行异常处理，监管/watch机制使用，测试并发情况等等
