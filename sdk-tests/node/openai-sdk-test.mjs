import OpenAI from "openai";

const baseURL = process.env.OPENAI_BASE_URL || "http://localhost:8081/v1";
const apiKey = process.env.OPENAI_API_KEY || "test";

const client = new OpenAI({ apiKey, baseURL });

async function testNonStreaming() {
  const res = await client.chat.completions.create({
    model: "qwen-turbo",
    messages: [{ role: "user", content: "Hello from OpenAI SDK" }],
    stream: false,
    temperature: 0.7,
  });

  console.log("[non-stream] id:", res.id);
  console.log("[non-stream] model:", res.model);
  console.log("[non-stream] content:", res.choices?.[0]?.message?.content);
}

async function testStreaming() {
  const stream = await client.chat.completions.create({
    model: "qwen-turbo",
    messages: [{ role: "user", content: "Stream from OpenAI SDK" }],
    stream: true,
    temperature: 0.7,
  });

  let full = "";
  for await (const chunk of stream) {
    const delta = chunk.choices?.[0]?.delta?.content || "";
    full += delta;
    process.stdout.write(delta);
  }
  process.stdout.write("\n");
  console.log("[stream] done; length:", full.length);
}

async function main() {
  console.log("baseURL:", baseURL);
  console.log("apiKey:", apiKey ? "(set)" : "(empty)");

  await testNonStreaming();

  console.log("\n--- streaming ---");
  await testStreaming();
}

main().catch((err) => {
  console.error("SDK test failed:");
  console.error(err);
  process.exitCode = 1;
});
