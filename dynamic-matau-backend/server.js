const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const admin = require("firebase-admin");
require("dotenv").config();

// Initialize Firebase Admin SDK
const serviceAccount = require("./firebaseServiceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://fair-5268e-default-rtdb.asia-southeast1.firebasedatabase.app/"
});

// Create the Express app
const app = express();

// Middleware
app.use(bodyParser.json());
app.use(cors());

// Root route
app.get("/", (req, res) => {
  res.send("Server is working!");
});

const db = admin.database();

// M-Pesa STK Push Endpoint
app.post("/stkpush", async (req, res) => {
  try {
    const { matatu_id, user_id, amount, phone_number, mpesa_receipt } = req.body;
    console.log("Received M-Pesa request:", req.body);

    // Create a new transaction ID
    const transactionId = db.ref("payments").push().key;

    // Payment Data
    const paymentData = {
      matatu_id,
      user_id,
      amount,
      phone_number,
      status: "Pending",
      timestamp: Date.now(),
      payment_method: "M-Pesa",
      mpesa_receipt
    };

    // Save to Firebase
    await db.ref("payments").child(transactionId).set(paymentData);

    res.json({ success: true, message: "Payment request received!", transactionId });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// Start Server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
