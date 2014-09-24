/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.pay.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.badlogic.gdx.pay.PurchaseManagerConfig;
import com.badlogic.gdx.pay.Transaction;
import com.badlogic.gdx.pay.server.PurchaseVerifier;
import com.badlogic.gdx.pay.server.util.Base64Util;

/** Purchase verifier for iOS/Apple. Return true if the purchase appears valid.
 * @author noblemaster */
public class PurchaseVerifieriOSApple implements PurchaseVerifier {

	// sandbox URL
	private final static String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
	// production URL
	private final static String PRODUCTION_URL = "https://buy.itunes.apple.com/verifyReceipt";

	/** True for sandbox mode. */
	private boolean sandbox;
	
	public PurchaseVerifieriOSApple() {
		this(false);
	}
	
	public PurchaseVerifieriOSApple (boolean sandbox) {
		this.sandbox = sandbox;
	}

	@Override
	public String storeName () {
		return PurchaseManagerConfig.STORE_NAME_IOS_APPLE;
	}

	@Override
	public boolean isValid (Transaction transaction) {
		// the transaction data is our original == receipt!
		String receipt = transaction.getTransactionData();
		
		// encode the data
		final String receiptData = Base64Util.toBase64(receipt.getBytes());
		final String jsonData = "{\"receipt-data\" : \"" + receiptData + "\"}";
		try {
			// send the data to Apple
			final URL url = new URL(sandbox ? SANDBOX_URL : PRODUCTION_URL);
			final HttpURLConnection conn = (HttpsURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			final OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(jsonData);
			wr.flush();

			// obtain the response
			final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = rd.readLine();
			wr.close();
			rd.close();
			
			// verify the response: something like {"status":21004} etc...
			int status = Integer.parseInt(line.substring(line.indexOf(":") + 1, line.indexOf("}")));
			switch (status) {
				case 0: return true;
				case 21000: System.out.println(status + ": App store could not read"); return false;
				case 21002: System.out.println(status + ": Data was malformed"); return false;
				case 21003: System.out.println(status + ": Receipt not authenticated"); return false;
				case 21004: System.out.println(status + ": Shared secret does not match"); return false;
				case 21005: System.out.println(status + ": Receipt server unavailable"); return false;
				case 21006: System.out.println(status + ": Receipt valid but sub expired"); return false;
				case 21007: System.out.println(status + ": Sandbox receipt sent to Production environment"); return false;
				case 21008: System.out.println(status + ": Production receipt sent to Sandbox environment"); return false;
			   default:
			   	// unknown error code (nevertheless a problem)
			   	System.out.println("Unknown error: status code = " + status);
			   	return false;
			}
		} catch (IOException e) {
			// I/O-error: let's assume bad news...
			System.err.println("I/O error during verification: " + e);
			e.printStackTrace();			
			return false;
		}
	}
	
	/** Just used for testing... */
	public static void main(String[] args) {
		// test in sandbox-mode
		PurchaseVerifieriOSApple verifier = new PurchaseVerifieriOSApple(true);
		
		// our sample receipt for the sandbox (returns error 21004)
		String receipt = "{\n" +
			"\"signature\" = \"AluGxOuMy+RT1gkyFCoD1i1KT3KUZl+F5FAAW0ELBlCUbC9dW14876aW0OXBlNJ6pXbBBFB8K0LDy6LuoAS8iBiq3529aRbVRUSKCPeCDZ7apC2zqFYZ4N7bSFDMeb92wzN0X/dELxlkRH4bWjO67X7gnHcN47qHoVckSlGo/mpbAAADVzCCA1MwggI7oAMCAQICCGUUkU3ZWAS1MA0GCSqGSIb3DQEBBQUAMH8xCzAJBgNVBAYTAlVTMRMwEQYDVQQKDApBcHBsZSBJbmMuMSYwJAYDVQQLDB1BcHBsZSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEzMDEGA1UEAwwqQXBwbGUgaVR1bmVzIFN0b3JlIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MB4XDTA5MDYxNTIyMDU1NloXDTE0MDYxNDIyMDU1NlowZDEjMCEGA1UEAwwaUHVyY2hhc2VSZWNlaXB0Q2VydGlmaWNhdGUxGzAZBgNVBAsMEkFwcGxlIGlUdW5lcyBTdG9yZTETMBEGA1UECgwKQXBwbGUgSW5jLjELMAkGA1UEBhMCVVMwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMrRjF2ct4IrSdiTChaI0g8pwv/cmHs8p/RwV/rt/91XKVhNl4XIBimKjQQNfgHsDs6yju++DrKJE7uKsphMddKYfFE5rGXsAdBEjBwRIxexTevx3HLEFGAt1moKx509dhxtiIdDgJv2YaVs49B0uJvNdy6SMqNNLHsDLzDS9oZHAgMBAAGjcjBwMAwGA1UdEwEB/wQCMAAwHwYDVR0jBBgwFoAUNh3o4p2C0gEYtTJrDtdDC5FYQzowDgYDVR0PAQH/BAQDAgeAMB0GA1UdDgQWBBSpg4PyGUjFPhJXCBTMzaN+mV8k9TAQBgoqhkiG92NkBgUBBAIFADANBgkqhkiG9w0BAQUFAAOCAQEAEaSbPjtmN4C/IB3QEpK32RxacCDXdVXAeVReS5FaZxc+t88pQP93BiAxvdW/3eTSMGY5FbeAYL3etqP5gm8wrFojX0ikyVRStQ+/AQ0KEjtqB07kLs9QUe8czR8UGfdM1EumV/UgvDd4NwNYxLQMg4WTQfgkQQVy8GXZwVHgbE/UC6Y7053pGXBk51NPM3woxhd3gSRLvXj+loHsStcTEqe9pBDpmG5+sk4tw+GK3GMeEN5/+e1QT9np/Kl1nj+aBw7C0xsy0bFnaAd1cSS6xdory/CUvM6gtKsmnOOdqTesbp0bs8sn6Wqs0C9dgcxRHuOMZ2tm8npLUm7argOSzQ==\";\n" +
			"\"purchase-info\" = \"ewoJInF1YW50aXR5IiA9ICIxIjsKCSJwdXJjaGFzZS1kYXRlIiA9ICIyMDExLTEwLTEyIDIwOjA1OjUwIEV0Yy9HTVQiOwoJIml0ZW0taWQiID0gIjQ3MjQxNTM1MyI7CgkiZXhwaXJlcy1kYXRlLWZvcm1hdHRlZCIgPSAiMjAxMS0xMC0xMiAyMDoxMDo1MCBFdGMvR01UIjsKCSJleHBpcmVzLWRhdGUiID0gIjEzMTg0NTAyNTAwMDAiOwoJInByb2R1Y3QtaWQiID0gImNvbS5kYWlseWJ1cm4ud29kMW1vbnRoIjsKCSJ0cmFuc2FjdGlvbi1pZCIgPSAiMTAwMDAwMDAwOTk1NzYwMiI7Cgkib3JpZ2luYWwtcHVyY2hhc2UtZGF0ZSIgPSAiMjAxMS0xMC0xMiAyMDowNTo1MiBFdGMvR01UIjsKCSJvcmlnaW5hbC10cmFuc2FjdGlvbi1pZCIgPSAiMTAwMDAwMDAwOTk1NzYwMiI7CgkiYmlkIiA9ICJjb20uZGFpbHlidXJuLndvZCI7CgkiYnZycyIgPSAiMC4wLjgiOwp9\";\n" +
			"\"environment\" = \"Sandbox\";\n" +
			"\"pod\" = \"100\";\n" +
			"\"signing-status\" = \"0\";\n" +
			"}\n";
	
		// build a sample transaction (only receipt is important for validation)
		Transaction transaction = new Transaction();
		transaction.setTransactionData(receipt);		
		if (verifier.isValid(transaction)) {
			System.out.println("Purchase is VALID!");
		}
	}
}
