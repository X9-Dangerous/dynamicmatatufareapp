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

// M-Pesa Callback Endpoint
app.post("/mpesa-callback", async (req, res) => {
  try {
    console.log("Received M-Pesa callback:", JSON.stringify(req.body, null, 2));

    // Extract relevant information from callback
    const {
      Body: {
        stkCallback: {
          MerchantRequestID,
          CheckoutRequestID,
          ResultCode,
          ResultDesc,
          CallbackMetadata
        }
      }
    } = req.body;

    // Get payment details from metadata
    let amount = "", mpesaReceiptNumber = "", phoneNumber = "";
    if (CallbackMetadata && CallbackMetadata.Item) {
      CallbackMetadata.Item.forEach(item => {
        if (item.Name === "Amount") amount = item.Value;
        if (item.Name === "MpesaReceiptNumber") mpesaReceiptNumber = item.Value;
        if (item.Name === "PhoneNumber") phoneNumber = item.Value;
      });
    }

    // Find the payment in Firebase using phone number and amount
    const paymentsRef = db.ref("payments");
    const snapshot = await paymentsRef
      .orderByChild("phone_number")
      .equalTo(phoneNumber.toString())
      .once("value");
    
    let paymentUpdate = null;
    snapshot.forEach(child => {
      const payment = child.val();
      if (payment.status === "Pending" && payment.amount === amount) {
        paymentUpdate = {
          ref: child.ref,
          key: child.key,
          data: payment
        };
      }
    });

    if (paymentUpdate) {
      // Update payment status
      const status = ResultCode === 0 ? "Completed" : "Failed";
      await paymentUpdate.ref.update({
        status: status,
        mpesa_receipt: mpesaReceiptNumber,
        mpesa_result_code: ResultCode,
        mpesa_result_desc: ResultDesc,
        merchant_request_id: MerchantRequestID,
        checkout_request_id: CheckoutRequestID,
        updated_at: Date.now()
      });

      console.log(`Payment ${paymentUpdate.key} updated to ${status}`);
    } else {
      console.log("No matching pending payment found");
    }

    // Send response back to M-Pesa
    res.json({
      ResultCode: 0,
      ResultDesc: "Callback processed successfully"
    });
  } catch (error) {
    console.error("Error processing M-Pesa callback:", error);
    res.status(500).json({
      ResultCode: 1,
      ResultDesc: "Error processing callback"
    });
  }
});

// Start Server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
