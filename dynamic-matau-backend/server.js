const express = require("express");
const bodyParser = require("body-parser");
require("dotenv").config(); // Load environment variables

const app = express();
const PORT = process.env.PORT || 3000;

app.use(bodyParser.json());

// Sample M-Pesa STK Push Route
app.post("/stkpush", (req, res) => {
    console.log("M-Pesa STK Push request received:", req.body);
    res.json({ message: "M-Pesa request received!" });
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
