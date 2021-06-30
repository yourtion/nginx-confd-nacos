# nginx-confd-nacos

通过Nacos的服务注册功能，准实时更新Nginx负载均衡配置

## 开始使用

### Nginx 基础配置

```conf
events {
  worker_connections  512;
}
http {
  server {
    listen    3000;
    include   /tmp/server.location;
  }
  include   /tmp/nacos.upstream;
}
```

### 环境变量

#### 测试环境变量

- `NginxUtilTestCMD` 配置 Nginx 测试路径

## 架构与逻辑

### 流程

1. 加载配置信息（本地配置、Nacos配置）
2. 根据配置检查环境信息（Nginx路径、运行状态）
3. 获取 service 列表并根据列表获取实例信息生成初始化配置
4. 定时轮询并根据黑白名单配置 service 监听列表
    - 如果服务的实例发生变化记录，则加入本地待处理队列
    - 如果所有实例都被移除，则删除监听
5. 定时检查待处理队列，如果不为空则进行更新
    - 根据待处理队列信息与本地信息进行合并
    - 移动旧配置文件并根据合并后数据生成新配置
    - 执行配置文件检查，如果成功则刷新nginx，并把配置更新到本地缓存
    - 如果检查失败，则将待处理 service 重新加入待处理队列

## 功能列表

- [X] 支持配置 Nginx 路径、Nginx 配置文件路径
- [ ] 支持配置 Nginx 模板文件，配置详细信息、生成配置文件名
- [ ] 配置服务名称黑白名单、监听服务分组
- [X] 通过 Nacos 加载配置信息
- [ ] 支持执行失败 webhook 告警
- [ ] 支持 backend 服务健康检查/只配置健康实例
