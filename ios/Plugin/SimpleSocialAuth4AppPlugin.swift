import Foundation
import Capacitor
import WebKit

extension URL {
    var queryParameters: [String: String]? {
        guard let components = URLComponents(url: self, resolvingAgainstBaseURL: true),
              let queryItems = components.queryItems else { return nil }
        return queryItems.reduce(into: [String: String]()) { result, item in
            result[item.name] = item.value
        }
    }
}

@objc(SimpleSocialAuth4AppPlugin)
public class SimpleSocialAuth4AppPlugin: CAPPlugin {
    var fullScreenWebView: WKWebView?
    var closeButton: UIButton?
    var activityIndicator: UIActivityIndicatorView?
    var sessionKey: String = "";
    
    @objc func auth(_ call: CAPPluginCall) {
        let socialNetwork = call.getString("social") ?? ""
        self.sessionKey = generateRandomKey(length: 256)  // Генерация и сохранение уникального ключа

        guard let authUrl = URL(string: "https://auth4app.com/auth?soc=\(socialNetwork)&key=\(self.sessionKey)") else {
            call.reject("Invalid URL")
            return
        }

        DispatchQueue.main.async {
            self.setupWebView(authUrl: authUrl, call: call, sessionKey: self.sessionKey)
        }
    }

    private func setupWebView(authUrl: URL, call: CAPPluginCall, sessionKey: String) {
        fullScreenWebView?.removeFromSuperview()
        closeButton?.removeFromSuperview()
        activityIndicator?.removeFromSuperview()

        let config = WKWebViewConfiguration()
       // config.websiteDataStore = WKWebsiteDataStore.default()
        config.websiteDataStore = WKWebsiteDataStore.nonPersistent()
        let webView = WKWebView(frame: UIScreen.main.bounds, configuration: config)
        
        webView.navigationDelegate = self
        webView.customUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/602.1.50 (KHTML, like Gecko) CriOS/58.0.3029.110 Mobile/14E5239e Safari/602.1"

        fullScreenWebView = webView

        setupActivityIndicator()
        setupCloseButton()

        if let parentView = bridge?.webView?.superview {
            parentView.insertSubview(webView, aboveSubview: bridge!.webView!)
            parentView.addSubview(closeButton!)
            parentView.addSubview(activityIndicator!)
            call.resolve()
        } else {
            call.reject("Failed to get parent view")
        }

        webView.load(URLRequest(url: authUrl))
    }

    private func setupActivityIndicator() {
        activityIndicator = UIActivityIndicatorView(style: .large)
        activityIndicator?.center = bridge?.webView?.center ?? CGPoint(x: UIScreen.main.bounds.midX, y: UIScreen.main.bounds.midY)
        activityIndicator?.startAnimating()
        activityIndicator?.color = .gray
    }

    private func setupCloseButton() {
        closeButton = UIButton(frame: CGRect(x: UIScreen.main.bounds.width - 60, y: bridge?.webView?.safeAreaInsets.top ?? 20, width: 40, height: 40))
        closeButton?.setTitle("X", for: .normal)
        closeButton?.backgroundColor = .gray
        closeButton?.layer.cornerRadius = 20
        closeButton?.addTarget(self, action: #selector(self.closeWebView), for: .touchUpInside)
    }

    @objc func closeWebView() {
        DispatchQueue.main.async {
            self.fullScreenWebView?.removeFromSuperview()
            self.closeButton?.removeFromSuperview()
            self.activityIndicator?.stopAnimating()
            self.activityIndicator?.removeFromSuperview()
        }
    }

    private func generateRandomKey(length: Int) -> String {
        let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return String((0..<length).map{ _ in letters.randomElement()! })
    }

    private func fetchAPIResponse(key: String) {
        let url = URL(string: "https://api.auth4app.com/hash?key=\(key)")!
        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            guard error == nil else {
                self.notifyListeners("authError", data: ["key": key, "error": error!.localizedDescription])
                return
            }

            guard let data = data,
                  let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                  let type = json["type"] as? String else {
                self.notifyListeners("authError", data: ["key": key, "error": "Invalid JSON data received"])
                return
            }

            if type == "success", let userData = json["data"] as? [String: Any] {
                self.notifyListeners("authSuccess", data: ["key": key, "userInfo": userData])
            } else if let errorMessage = json["data"] as? String {
                self.notifyListeners("authError", data: ["key": key, "error": errorMessage])
            } else {
                self.notifyListeners("authError", data: ["key": key, "error": "Unknown error occurred"])
            }
        }
        task.resume()
    }

}

extension SimpleSocialAuth4AppPlugin: WKNavigationDelegate {
    public func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
        activityIndicator?.stopAnimating()

        guard let url = webView.url else { return }

        if url.absoluteString.contains("callback/success") {
            let key = self.sessionKey
            fetchAPIResponse(key: key)
            closeWebView()
        } else if url.absoluteString.contains("callback/error") {
            let key = self.sessionKey
            notifyListeners("authError", data: ["key": key])
            closeWebView()
        }
    }

    public func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
        activityIndicator?.stopAnimating()
    }
}
