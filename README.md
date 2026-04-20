## 项目简介
本项目是一个 **OpenAI API 风格** 的后端服务（MVP 方向：Chat Completions），用于将统一的 `/v1/chat/completions` 请求转发到上游 **OpenAI 兼容接口**（当前实现对接：阿里云百炼 DashScope 兼容模式），并将每次生成请求在 MySQL 中持久化记录。

> 本仓库的考核要求来源：`openai.md`。

## 功能完成度（对照 openai.md）

### 已实现
- Chat Completions：`POST /v1/chat/completions`（支持 `stream=false/true`）
- 请求持久化：生成前分配 `chatcmpl-...` 唯一 ID 并写库；流式结束/非流式返回后更新生成内容
- 基础异常处理：未携带/非法 Bearer Token 时返回 401（拦截器能力已具备）
- OpenAPI 文档：集成 springdoc（可通过 Swagger UI 访问）
- 模型白名单：`model` 必须存在于 `ai_models` 且 `enabled=1`，否则返回 400（OpenAI 风格 `invalid_request_error`）
- 模型列表：`GET /v1/models`（仅返回 `enabled=1`）

### 未实现/待补全（README 如实说明）
- 生成结果管理：`GET/DELETE /v1/chat/completions/{id}`、`POST /v1/chat/completions/{id}/cancel` 当前未提供 Controller 路由（Service 层已有 `getById/removeById` 基础方法）
- “所有接口都强制 Bearer Token” 的强制策略：当前为了联调，`/v1/chat/completions` 在配置中被排除出 JWT 校验（见下方“鉴权说明”）

## 技术栈
- Java 17
- Spring Boot 3.1.5（WebMVC + WebFlux：上游调用使用 `WebClient`）
- MyBatis-Plus + MySQL
- Spring Security（当前以 permitAll 为主，便于本地调试）
- springdoc-openapi（Swagger UI）

## 架构设计简述
- `controller`：对外暴露 OpenAI 风格路由（当前实现 `ChatController`）
- `service`：生成请求生命周期管理（保存请求、完成更新等）
- `mapper/entity`：MyBatis-Plus 持久化 `ChatCompletion`
- `config`：CORS、安全策略、`WebClient` 配置
- `interceptor`：JWT Bearer Token 校验（可从 Token 解析 userId 并注入 request attribute）

数据流（非流式）：
1) 客户端调用 `/v1/chat/completions` → 2) 服务端生成 `chatcmpl-...` 并写入 `chat_completion` → 3) WebClient 转发到上游兼容接口 → 4) 返回响应给客户端 → 5) 将最终回答内容写回数据库。

数据流（流式）：
1) 保存请求记录 → 2) WebClient 建立 SSE 连接 → 3) 服务端将上游 SSE 数据块转发给客户端 → 4) 流结束后汇总 content 并写回数据库。

## 鉴权与生成流程说明

### Bearer Token（按考核要求）
`openai.md` 要求：**所有接口**必须使用 `Authorization: Bearer <token>` 鉴权，非法/缺失返回 401。

### 当前实现的实际行为
- `/v1/**` 的所有 API 均由 `JwtAuthInterceptor` 强制 Bearer Token 校验（包含 `/v1/chat/completions`）
- JWT 的 subject 会被解析为 `userId`，用于请求持久化（便于区分用户）
- Swagger 文档页面默认不走 `/v1/**`，因此不受此拦截影响

## API 使用说明

### 1) Chat Completions
`POST /v1/chat/completions`

模型白名单说明：
- 请求中的 `model` 必须在数据库表 `ai_models` 中存在，且 `enabled=1`
- 否则返回 400：
	```json
	{
		"error": {
			"message": "Model not available",
			"type": "invalid_request_error"
		}
	}
	```

请求体（最小集）：
```json
{
	"model": "qwen-turbo",
	"messages": [
		{"role": "system", "content": "You are a helpful assistant."},
		{"role": "user", "content": "Hello"}
	],
	"temperature": 0.7,
	"stream": false
}
```

curl（非流式）：
```bash
curl -X POST http://localhost:8081/v1/chat/completions \
	-H "Content-Type: application/json" \
	-H "Authorization: Bearer test" \
	-d '{"model":"qwen-turbo","messages":[{"role":"user","content":"Hello"}],"stream":false}'
```

curl（流式 SSE）：
```bash
curl -N http://localhost:8081/v1/chat/completions \
	-H "Content-Type: application/json" \
	-H "Authorization: Bearer test" \
	-d '{"model":"qwen-turbo","messages":[{"role":"user","content":"Hello"}],"stream":true}'
```

说明：
- `stream=true` 时响应 `Content-Type` 为 `text/event-stream`，服务端转发上游的 SSE 数据块，并在结束时落库完整内容
- 服务端会生成 `chatcmpl-...` 作为本次请求的 `id`：
	- 非流式：直接写入响应体 `id`
	- 流式：会尽量在每个 SSE chunk 的 JSON 中重写/补齐 `id` 字段为服务端生成的 `chatcmpl-...`

### 2) 生成结果管理

获取某次生成结果：
`GET /v1/chat/completions/{completion_id}`

删除某次生成结果：
`DELETE /v1/chat/completions/{completion_id}`

取消正在进行的生成：
`POST /v1/chat/completions/{completion_id}/cancel`

示例（将 `{completion_id}` 替换为上一步返回/流式 chunk 中的 `id`）：
```bash
curl http://localhost:8081/v1/chat/completions/{completion_id} \
	-H "Authorization: Bearer <YOUR_JWT>"

curl -X DELETE http://localhost:8081/v1/chat/completions/{completion_id} \
	-H "Authorization: Bearer <YOUR_JWT>"

curl -X POST http://localhost:8081/v1/chat/completions/{completion_id}/cancel \
	-H "Authorization: Bearer <YOUR_JWT>"
```

## 数据库表结构（最小可运行）
当前代码默认使用 MyBatis-Plus 的命名转换（`ChatCompletion` → `chat_completion`，字段 camelCase → snake_case）。

仓库提供了初始化脚本：[db/init.sql](db/init.sql)

也可以手动执行以下 SQL（按需调整字段长度/字符集）：
```sql
CREATE TABLE IF NOT EXISTS ai_models (
	model_id  VARCHAR(128) NOT NULL PRIMARY KEY,
	owned_by  VARCHAR(128) NULL,
	enabled   TINYINT      NOT NULL DEFAULT 1,
	created   BIGINT       NULL
);

CREATE TABLE IF NOT EXISTS chat_completion (
	id              VARCHAR(64)  NOT NULL PRIMARY KEY,
	user_id         VARCHAR(64)  NULL,
	model           VARCHAR(128) NULL,
	request_messages TEXT        NULL,
	response_content MEDIUMTEXT  NULL,
	status          VARCHAR(32)  NULL,
	created_at      BIGINT       NULL
);
```

初始化模型白名单示例：
```sql
INSERT INTO ai_models (model_id, owned_by, enabled, created)
VALUES ('qwen-turbo', 'dashscope', 1, UNIX_TIMESTAMP());
```

## 模型列表接口
`GET /v1/models`

示例：
```bash
curl http://localhost:8081/v1/models \
	-H "Authorization: Bearer <YOUR_JWT>"
```

## 快速启动

### 0) 前置条件
- JDK 17
- Maven 3.8+
- MySQL 8.x（或兼容版本）

### 1) 配置
编辑 [open-api-sever/src/main/resources/application.yml](open-api-sever/src/main/resources/application.yml)：
- `spring.datasource.*`：改成你本地/自己的数据库地址与账号
- `dashscope.url`：上游 OpenAI 兼容接口地址（当前默认：DashScope compatible-mode）
- `dashscope.key`：上游鉴权 Key（请勿提交真实 Key）

### 2) 启动
在 `open-api-sever` 目录执行：
```bash
mvn spring-boot:run
```
默认端口：`8081`

## API 文档（Swagger / OpenAPI）
- Swagger UI：`http://localhost:8081/swagger-ui/index.html`
- OpenAPI JSON：`http://localhost:8081/v3/api-docs`

## SDK 可用性验证（官方 OpenAI SDK）
按考核要求，建议使用官方 SDK 直连本服务（通过 `baseURL` 指向本地）。本仓库提供一个 Node.js 示例脚本：

- 目录：`sdk-tests/node`
- 运行：
	```bash
	cd sdk-tests/node
	npm install
	set OPENAI_BASE_URL=http://localhost:8081/v1
	node generate-jwt.mjs
	# 把上一行输出的 token 复制到这里
	set OPENAI_API_KEY=PASTE_YOUR_JWT_HERE
	node openai-sdk-test.mjs
	```

说明：
- 该脚本会分别测试非流式与流式调用，并打印 SDK 能否正常解析响应
- `generate-jwt.mjs` 使用的密钥默认与服务端 `application.yml` 的 `jwt.secret-key` 一致；如果你改了服务端密钥，请同时设置环境变量：
	- `set JWT_SECRET_KEY=...`
	- `set JWT_SUBJECT=...`（可选：作为 userId）

## AIGC 使用说明
- 使用工具：GitHub Copilot Chat（GPT-5.2）
- 用途：补全与规范化 README 文档内容（接口说明、启动说明、SDK 自测说明）
