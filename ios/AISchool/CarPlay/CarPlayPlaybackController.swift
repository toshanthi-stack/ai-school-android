import Foundation
import AVFoundation
import MediaPlayer
import CarPlay

/// Audio playback for the CarPlay scene. Self-contained and separate from the
/// SwiftUI `AudioPlayer` so the car experience mirrors the Android Automotive
/// media service: audio-only, driven by the system Now Playing template and the
/// remote command center, with bundled-narration-first / stream-fallback loading.
///
/// A lesson selection plays the whole course in order (audio is the lesson in
/// the car); auto-advance walks the course and stops at the end.
@MainActor
final class CarPlayPlaybackController {
    static let shared = CarPlayPlaybackController()

    private weak var interfaceController: CPInterfaceController?

    private var player: AVPlayer?
    private var timeObserver: Any?
    private var endObserver: NSObjectProtocol?
    private var commandsConfigured = false

    private var course: Course?
    private var queue: [Lesson] = []   // automotive-safe lessons of `course`
    private var index = 0

    private var duration: Double = 0
    private var isPlaying = false

    private init() {}

    // MARK: Scene lifecycle

    func attach(interfaceController: CPInterfaceController) {
        self.interfaceController = interfaceController
        configureRemoteCommands()
    }

    func detach() {
        stop()
        interfaceController = nil
    }

    // MARK: Playback control

    /// Plays `lesson`, queuing the rest of its course's audio-safe lessons for
    /// auto-advance, and surfaces the Now Playing template.
    func play(lesson: Lesson, in course: Course) {
        self.course = course
        queue = course.lessons.filter(\.isAutomotiveSafe)
        index = queue.firstIndex(of: lesson) ?? 0
        startCurrent()
        presentNowPlaying()
    }

    private func startCurrent() {
        guard index >= 0, index < queue.count else { stop(); return }
        let lesson = queue[index]
        teardownPlayer()

        duration = Double(lesson.durationSeconds)
        let url = AudioPlayer.bundledNarrationURL(for: lesson.id) ?? URL(string: lesson.audioUrl)
        guard let url else { return }

        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio)
        try? AVAudioSession.sharedInstance().setActive(true)

        let item = AVPlayerItem(url: url)
        let avPlayer = AVPlayer(playerItem: item)
        player = avPlayer

        // Real asset duration once available (the bundled narration is the
        // source of truth over the catalog's nominal length).
        Task { [weak self] in
            if let assetDuration = try? await item.asset.load(.duration) {
                let seconds = CMTimeGetSeconds(assetDuration)
                if seconds.isFinite, seconds > 0 {
                    self?.duration = seconds
                    self?.updateNowPlayingInfo()
                }
            }
        }

        let interval = CMTime(seconds: 0.5, preferredTimescale: 600)
        timeObserver = avPlayer.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] _ in
            // Delivered on the main queue, so main-actor state is safe to touch.
            MainActor.assumeIsolated { self?.updateElapsed() }
        }

        endObserver = NotificationCenter.default.addObserver(
            forName: AVPlayerItem.didPlayToEndTimeNotification, object: item, queue: .main
        ) { [weak self] _ in
            MainActor.assumeIsolated { self?.advance() }
        }

        avPlayer.play()
        isPlaying = true
        updateNowPlayingInfo()
    }

    func toggle() {
        guard let player else { return }
        if isPlaying { player.pause() } else { player.play() }
        isPlaying.toggle()
        updateNowPlayingInfo()
    }

    /// Next lesson in the course, or stop at the end.
    func advance() {
        if index + 1 < queue.count {
            index += 1
            startCurrent()
        } else {
            stop()
        }
    }

    /// Previous lesson, or restart the current one if already first.
    func rewind() {
        if index > 0 {
            index -= 1
            startCurrent()
        } else {
            player?.seek(to: .zero)
            updateElapsed()
        }
    }

    func stop() {
        teardownPlayer()
        isPlaying = false
        duration = 0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }

    private func teardownPlayer() {
        if let timeObserver { player?.removeTimeObserver(timeObserver) }
        timeObserver = nil
        if let endObserver { NotificationCenter.default.removeObserver(endObserver) }
        endObserver = nil
        player?.pause()
        player = nil
    }

    // MARK: Now Playing template + info center

    private func presentNowPlaying() {
        guard let ic = interfaceController else { return }
        let nowPlaying = CPNowPlayingTemplate.shared
        if !ic.templates.contains(where: { $0 === nowPlaying }) {
            ic.pushTemplate(nowPlaying, animated: true, completion: nil)
        }
    }

    private func elapsed() -> Double {
        guard let t = player?.currentTime() else { return 0 }
        let seconds = CMTimeGetSeconds(t)
        return seconds.isFinite ? seconds : 0
    }

    private func updateElapsed() {
        var info = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? [:]
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = elapsed()
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }

    private func updateNowPlayingInfo() {
        guard index >= 0, index < queue.count else { return }
        let lesson = queue[index]
        var info: [String: Any] = [:]
        info[MPMediaItemPropertyTitle] = lesson.title
        info[MPMediaItemPropertyAlbumTitle] = course?.title ?? "AI School"
        info[MPMediaItemPropertyArtist] = course?.category ?? "AI School"
        info[MPMediaItemPropertyPlaybackDuration] = duration
        info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = elapsed()
        info[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0
        MPNowPlayingInfoCenter.default().nowPlayingInfo = info
    }

    // MARK: Remote command center (CarPlay transport + steering-wheel controls)

    private func configureRemoteCommands() {
        guard !commandsConfigured else { return }
        commandsConfigured = true
        let center = MPRemoteCommandCenter.shared()
        center.playCommand.addTarget { [weak self] _ in
            MainActor.assumeIsolated {
                guard let self, !self.isPlaying else { return .commandFailed }
                self.toggle()
                return .success
            }
        }
        center.pauseCommand.addTarget { [weak self] _ in
            MainActor.assumeIsolated {
                guard let self, self.isPlaying else { return .commandFailed }
                self.toggle()
                return .success
            }
        }
        center.togglePlayPauseCommand.addTarget { [weak self] _ in
            MainActor.assumeIsolated { self?.toggle(); return .success }
        }
        center.nextTrackCommand.addTarget { [weak self] _ in
            MainActor.assumeIsolated { self?.advance(); return .success }
        }
        center.previousTrackCommand.addTarget { [weak self] _ in
            MainActor.assumeIsolated { self?.rewind(); return .success }
        }
        center.nextTrackCommand.isEnabled = true
        center.previousTrackCommand.isEnabled = true
    }
}
