import SwiftUI

extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(.sRGB, red: r, green: g, blue: b, opacity: 1)
    }
}

/// AI School brand palette (from lillytechsystems.com/ai-school). Dark-first.
enum Brand {
    static let primary = Color(hex: 0x6C63FF)   // indigo/violet
    static let secondary = Color(hex: 0xFF6584) // coral/pink
    static let accent = Color(hex: 0x43E97B)    // green
    static let cyan = Color(hex: 0x22D3EE)
    static let bg = Color(hex: 0x13131A)        // near-black
    static let card = Color(hex: 0x1E1E2F)
    static let border = Color(hex: 0x2A2A3C)
    static let text = Color(hex: 0xE4E4E7)
    static let textDim = Color(hex: 0x9CA3AF)

    /// Per-pillar accent (matches the VW-styled automotive preview).
    static func accent(for category: String) -> Color {
        switch category {
        case Pillars.infrastructure: return cyan
        case Pillars.advancedTuning: return secondary
        default: return primary
        }
    }

    static func icon(for category: String) -> String {
        switch category {
        case Pillars.infrastructure: return "memorychip"
        case Pillars.advancedTuning: return "slider.horizontal.3"
        default: return "sparkles"
        }
    }
}

func timeString(_ seconds: Double) -> String {
    guard seconds.isFinite, seconds >= 0 else { return "0:00" }
    let total = Int(seconds)
    return String(format: "%d:%02d", total / 60, total % 60)
}
