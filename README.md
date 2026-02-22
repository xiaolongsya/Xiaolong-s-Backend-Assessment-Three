## 项目简介
本系统是一个兼容 OpenAI 规范 的对话补全中转服务器，支持流式 (SSE) 与非流式输出。

## 技术栈
核心框架: Spring Boot 3.1.5, Java 17

持久化: MySQL + MyBatis Plus

响应流: Project Reactor (Flux)

## 核心功能实现说明
接口兼容性: 严格遵循 OpenAI API 规范，支持 POST /v1/chat/completions。

流式输出: 实现标准 SSE 协议，响应头包含 text/event-stream。

持久化机制: 每一条 Chat 请求均会在生成前分配唯一 ID 并存入数据库，任务结束后异步更新生成结果。

安全验证: 接入 JWT 拦截器，校验 Authorization: Bearer 请求头。

## 快速启动
修改 application.yml 中的数据库连接信息。

执行 db/init.sql 初始化表结构。

运行 OpenApiSeverApplication。

使用 Postman 导入附带的 postman_collection.json 进行测试。