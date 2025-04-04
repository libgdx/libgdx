<!DOCTYPE html>
<html>
<head>
  <title>Vulnerable Demo Page</title>
  <script>
    // Critical: DOM-based XSS vulnerability
    const params = new URLSearchParams(window.location.search);
    const username = params.get("user");
    document.write("Welcome " + username); // ❌ No escaping — XSS risk

    // High: Hardcoded credentials
    const API_KEY = "12345-abcdef-SECRET"; // ❌ Secrets should not be in frontend code

    // Medium: Insecure random number generation
    const token = Math.random(); // ❌ Not cryptographically secure

    // Low: Unused variable
    let unusedFlag = false;

    // Hotspot: Inefficient DOM access
    function updateUI() {
      for (let i = 0; i < 1000; i++) {
        document.getElementById("status").innerText = "Updated " + i;
      }
    }
  </script>
  <style>
    /* Low: Inline CSS with !important */
    .hidden { display: none !important; }
  </style>
</head>
<body>
  <h1>Login Page</h1>
  <form action="login.php" method="POST">
    <!-- High: CSRF vulnerability, no token present -->
    <label>Email: <input type="email" name="email"></label><br>
    <label>Password: <input type="password" name="password"></label><br>
    <input type="submit" value="Login">
  </form>

  <!-- Medium: HTTP-only cookie not set, allows JS access -->
  <script>
    document.cookie = "session=abc123"; // ❌ Should use HttpOnly, Secure flags
  </script>

  <div id="status"></div>
</body>
</html>

<!--
====== METRICS SUMMARY ======

Security Issues:
- Critical: 1 (DOM-based XSS)
- High: 2 (Hardcoded credentials, CSRF)
- Medium: 2 (Insecure random, Cookie flag missing)
- Low: 2 (Unused var, !important in CSS)

Code Smells (Hotspots):
- Hotspot: 1 (Inefficient DOM manipulation)

-->
