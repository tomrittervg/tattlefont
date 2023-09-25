
package vg.ritter.tattlefont.https;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.os.AsyncTask;
        import android.widget.ProgressBar;


        import java.security.KeyManagementException;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.security.cert.CertificateException;
        import java.security.cert.X509Certificate;

        import javax.net.ssl.HttpsURLConnection;
        import javax.net.ssl.SSLContext;
        import javax.net.ssl.TrustManager;
        import javax.net.ssl.X509TrustManager;

        import vg.ritter.tattlefont.utility.OnPostExecuteCallback;


public abstract class AbstractPoster<T> extends AsyncTask<String, Void, T> {
    protected final Context context;
    public final ProgressBar ProgressBar;
            private final OnPostExecuteCallback onPostExecuteListener; // The generic lambda

    public AbstractPoster(Context context, OnPostExecuteCallback onPostExecuteListener, ProgressBar progressBar) throws NoSuchAlgorithmException, KeyManagementException {
        this.context = context;
        this.ProgressBar = progressBar;
        this.onPostExecuteListener = onPostExecuteListener; // Store the lambda

        X509TrustManager customTrustManager = createCustomTrustManager();
        TrustManager[] trustManagers = new TrustManager[]{customTrustManager};
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
    }

    @SuppressLint("CustomX509TrustManager")
    private X509TrustManager createCustomTrustManager() {
        return new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // Not used for client-side validation
            }


            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Check the certificate fingerprint against the provided fingerprint
                try {
                    for (X509Certificate cert : chain) {
                        byte[] fingerprintBytes = getSHA256Fingerprint(cert);
                        String fingerprintHex = bytesToHex(fingerprintBytes);
                        if (hardcodedFingerprint.equals(fingerprintHex)) {
                            return; // Certificate fingerprint matches
                        }
                    }
                } catch (Exception e) {}

                throw new CertificateException("Certificate fingerprint does not match.");
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0]; // Not used
            }
        };
    }

    private byte[] getSHA256Fingerprint(X509Certificate certificate) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(certificate.getEncoded());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    static String hostname = "https://ritter.vg:25497";
    static String hardcodedFingerprint = "8b4031ca7f0205f5d566bec56be5b2022c9b1fb6f52c4e61f6974dffffda77be";

    protected void onProgressUpdate(Integer... values) {
        if (values != null && values.length > 0) {
            int progress = values[0];
            ProgressBar.setProgress(progress);
        }
    }

            protected void onPostExecute(T result) {
                if (onPostExecuteListener != null) {
                    onPostExecuteListener.onPostExecute(result); // Call the lambda
                }
            }
}
