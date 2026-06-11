import SwiftUI
import WebKit

/// Renders a live AI School topic page, stripping the site's global chrome
/// (nav, sidebar, cookie banner) so only the lesson content shows, matching the
/// Android mobile flavor.
struct LessonWebView: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator
        webView.isOpaque = false
        webView.backgroundColor = UIColor(Brand.bg)
        webView.scrollView.backgroundColor = UIColor(Brand.bg)
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator() }

    final class Coordinator: NSObject, WKNavigationDelegate {
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            webView.evaluateJavaScript(Self.cleanupJS, completionHandler: nil)
        }

        static let cleanupJS = """
        (function() {
          var css = `
            header, nav, .navbar, .sidebar, aside, footer,
            [class*="cookie"], [id*="cookie"], [class*="consent"], [id*="consent"],
            [class*="banner"], [class*="newsletter"], .ad, [class*="ads"] { display:none !important; }
            html, body { background:#13131A !important; color:#E4E4E7 !important; margin:0 !important; }
            main, .content, .main, .container, article {
              margin:0 !important; padding:16px !important; width:100% !important; max-width:100% !important; }
          `;
          var s = document.createElement('style'); s.innerHTML = css; document.head.appendChild(s);
        })();
        """
    }
}
