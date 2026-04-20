import jwt from "jsonwebtoken";

// Must match open-api-sever/src/main/resources/application.yml (jwt.secret-key)
const secret = process.env.JWT_SECRET_KEY || "thisIsApiKey-thisIsApiKey-thisIsApiKey-32";

// Subject will be used as userId in server persistence
const subject = process.env.JWT_SUBJECT || "test_user";

// JJWT validates exp; set a reasonable default.
const expiresIn = process.env.JWT_EXPIRES_IN || "7d";

const token = jwt.sign({}, secret, {
  algorithm: "HS256",
  subject,
  expiresIn,
});

console.log(token);
