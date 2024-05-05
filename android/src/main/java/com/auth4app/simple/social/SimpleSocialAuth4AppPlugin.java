package com.auth4app.simple.social;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Button;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.graphics.Color;
import android.widget.ProgressBar;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;
import org.json.JSONObject;
import android.webkit.CookieManager;
import android.webkit.WebViewDatabase;
import java.security.SecureRandom;

@CapacitorPlugin(name = "SimpleSocialAuth4App")
public class SimpleSocialAuth4AppPlugin extends Plugin {

    private SimpleSocialAuth4App implementation = new SimpleSocialAuth4App();

    private WebView fullScreenWebView;
    private Button closeButton;
    private ProgressBar progressBar;

    @PluginMethod
    public void auth(PluginCall call) {
        String socialNetwork = call.getString("social", "");
        String sessionKey = generateRandomKey(256);

        String authUrl = "https://auth4app.com/auth?soc=" + socialNetwork + "&key=" + sessionKey;

        getActivity().runOnUiThread(() -> {
            setupWebView(authUrl, sessionKey, call);
        });

        JSObject ret = new JSObject();
        ret.put("key", sessionKey);
        call.resolve(ret);
    }
    private void setupWebView(String url, String sessionKey, PluginCall call) {
        fullScreenWebView = new WebView(getContext());
        fullScreenWebView.getSettings().setJavaScriptEnabled(true);

        String userAgent = "Mozilla/5.0 (Linux; Android 10; SM-G981U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.152 Mobile Safari/537.36";
        fullScreenWebView.getSettings().setUserAgentString(userAgent);


        clearWebViewCache(fullScreenWebView);
        fullScreenWebView.setWebViewClient(new WebViewClient() {

            public  void  onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
               closeWebView();
            }
            public void onPageFinished(WebView view, String url) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (url.contains("callback/success")) {
                    fetchAPIResponse(sessionKey); // Вызов метода после успешной авторизации
                    closeWebView();
                } else if (url.contains("callback/error")) {

                    closeWebView();
                }
            }
        });

        fullScreenWebView.loadUrl(url);

        closeButton = new Button(getContext());
        closeButton.setText("X");
        closeButton.setBackgroundColor(Color.BLACK);
        closeButton.setTextColor(Color.WHITE); // Set text color to white

        closeButton.setOnClickListener(v -> closeWebView());

        // Setup progress bar
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleLarge);
        progressBar.setVisibility(View.VISIBLE);

        // Set the size of the progress bar
        FrameLayout.LayoutParams progressBarParams = new FrameLayout.LayoutParams(50, 50);
        progressBarParams.gravity = android.view.Gravity.CENTER; // Center the progress bar

        // Layout configuration
        FrameLayout.LayoutParams webViewParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0, 50, 20, 0);
        buttonParams.gravity = android.view.Gravity.TOP | android.view.Gravity.RIGHT;

        FrameLayout layout = new FrameLayout(getContext());
        layout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); // Ensure the layout fills the screen
        layout.addView(fullScreenWebView, webViewParams);
        layout.addView(closeButton, buttonParams);
        layout.addView(progressBar, progressBarParams);

        ((ViewGroup) bridge.getWebView().getParent()).addView(layout);
    }

    private void clearWebViewCache(WebView webView) {
        webView.clearCache(true);
        webView.clearHistory();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            WebViewDatabase.getInstance(getContext()).clearFormData();
        } else {
            webView.clearFormData();
        }
    }

    private void closeWebView() {
        if (fullScreenWebView != null) {
            fullScreenWebView.setVisibility(View.GONE);
            ((ViewGroup) fullScreenWebView.getParent()).removeView(fullScreenWebView);
            fullScreenWebView = null;
        }
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
            ((ViewGroup) closeButton.getParent()).removeView(closeButton);
            closeButton = null;
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
            ((ViewGroup) progressBar.getParent()).removeView(progressBar);
            progressBar = null;
        }
    }

    private void fetchAPIResponse(String key) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL("https://api.auth4app.com/hash?key=" + key);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    String response = new BufferedReader(new InputStreamReader(inputStream))
                            .lines().collect(Collectors.joining("\n"));

                    JSONObject jsonResponse = new JSONObject(response);
                    String type = jsonResponse.getString("type");

                    if ("success".equals(type)) {
                        JSONObject userData = jsonResponse.getJSONObject("data");
                        JSObject ret = new JSObject();
                        ret.put("key", key);
                        ret.put("userInfo", userData.toString());
                        notifyListeners("authSuccess", ret);
                    } else {
                        String errorMessage = jsonResponse.getString("data");
                        notifyListeners("authError", new JSObject().put("key", key).put("error", errorMessage));
                    }
                } else {
                    notifyListeners("authError", new JSObject().put("key", key).put("error", "HTTP error with code: " + responseCode));
                }
                connection.disconnect();
            } catch (Exception e) {
                notifyListeners("authError", new JSObject().put("key", key).put("error", e.toString()));
            }
        });
        thread.start();
    }


    private String generateRandomKey(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        return sb.toString();
    }
}
