//
//  PlaybackManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/10/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import os
import Foundation
import PromiseKit
import AVFoundation
import UIKit

class PlaybackManager : UICommunicator<PlaybackState>, AVAudioPlayerDelegate {

    static let LIVE_STREAM_URL = Context.API_URL_PREFIX + "/linkgenerator/live.mp3?src=aHR0cHM6Ly9zdHJlYW0ucmFhLm1lZGlhL3JhYTFfbmV3Lm9nZw=="
    
    let audioSession = AVAudioSession.sharedInstance()
    var player: AVPlayer? = nil
    
    var playbackState: PlaybackState? = nil
    var personalFeedPlaybackState: PersonalFeedPlaybackState? = nil
    
    struct PropertyKey {
        static var media = "PlaybackState"
    }
    
    override init() {
        super.init()
        playbackState = PlaybackState()
    }
    
    func initiate() {
        initAudioSession()

//        Context.Instance.liveBroadcastManager.registerEventListener(listenerObject: self)
    }
    
    private func initAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, with: [AVAudioSessionCategoryOptions.allowBluetooth, AVAudioSessionCategoryOptions.mixWithOthers, AVAudioSessionCategoryOptions.duckOthers])
            
            NotificationCenter.default.addObserver(self, selector: #selector(handleAVInterruption), name: .AVAudioSessionInterruption, object: nil)

            NotificationCenter.default.addObserver(self, selector: #selector(handleMediaServiceReset), name: .AVAudioSessionMediaServicesWereReset, object: nil)

        } catch let e {
            print("Error while configuring AudioSession. Error is: " + e.localizedDescription)
        }
    }

    // Calls from item list controllers
    public func playLiveBroadcast(_ forceRestartStream: Bool = false) {
        os_log("Requested live playback", type: .default)
        self.playbackState?.programType = .Live
        
        guard Context.Instance.liveBroadcastManager.getMostRecentProgramIndex() != nil else {
            return
        }
        let program = Context.Instance.liveBroadcastManager.liveLineupData.flattenLiveLineup?[Context.Instance.liveBroadcastManager.getMostRecentProgramIndex()!]
        if program != nil {
            self.play(programId: (program?.ProgramId)!, title: (program?.Title)!, subtitle: (program?.Subtitle)!, mediaPath: PlaybackManager.LIVE_STREAM_URL, forceRestartStream: forceRestartStream)
            return
        }
    }

    public func playPublicFeed(_ feedEntryId: String) {
        os_log("Requested playback of %@", type: .default, feedEntryId)
        self.playbackState?.programType = .PublicFeed
        
        let publicFeedEntry = Context.Instance.feedManager.lookupPublicFeedEntry(feedEntryId)
        if publicFeedEntry != nil {
            self.play(programId: (publicFeedEntry!.ProgramObject?.ProgramId)!, title: publicFeedEntry!.ProgramObject?.Title, subtitle: publicFeedEntry!.ProgramObject?.Subtitle, mediaPath: (publicFeedEntry!.ProgramObject?.Show?.Clips?[0].Media?.Path)!)
        } else {
            // We should never come here
            os_log("This is a bug, we have a key but cannot find entry for it", type: .error)
        }
    }

    public func playArchiveEntry(_ archiveEntry: ArchiveEntry?) {
        if (archiveEntry == nil) {
            // We should never come here
            os_log("This is a bug, we have received a null archive entry to play", type: .error)
            return
        }
        
        os_log("Requested playback of %@", type: .default, (archiveEntry?.Program?.CanonicalIdPath)!)
        self.playbackState?.programType = .Archive
        
        self.play(programId: (archiveEntry!.Program?.ProgramId)!, title: archiveEntry!.Program?.Title, subtitle: archiveEntry!.Program?.Subtitle, mediaPath: (archiveEntry!.Program?.Show?.Clips?[0].Media?.Path)!)
    }
    
    public func playPersonalFeed(_ feedEntryId: String) {
        os_log("Requested playback of %@", type: .default, feedEntryId)
        self.playbackState?.programType = .PersonalFeed
        
        let personalFeedEntry = Context.Instance.feedManager.lookupPersonalFeedEntry(feedEntryId)
        if personalFeedEntry != nil {
            self.personalFeedPlaybackState = PersonalFeedPlaybackState()

            self.personalFeedPlaybackState?.personalFeedEntry = personalFeedEntry

            // Decide what to play
            let (isPlayingPreShow, pos) = self.calculatePersonalEntryPlaybackPosition(personalFeedEntry!)
            self.personalFeedPlaybackState?.isPlayingPreShow = isPlayingPreShow
            let mediaPath = isPlayingPreShow ? personalFeedEntry!.ProgramObject?.PreShow?.Clips?[0].Media?.Path : personalFeedEntry!.ProgramObject?.Show?.Clips?[0].Media?.Path
            
            self.play(programId: (personalFeedEntry!.ProgramObject?.ProgramId)!, title: personalFeedEntry!.ProgramObject?.Title, subtitle: personalFeedEntry!.ProgramObject?.Subtitle, mediaPath: mediaPath!, seekPosition: CMTimeMakeWithSeconds(pos, 1000))
        } else {
            // We should never come here
            os_log("This is a bug, we have a key but cannot find entry for it", type: .error)
        }
    }
    
    func calculatePersonalEntryPlaybackPosition(_ personalFeedEntry: PersonalFeedEntry) -> (Bool, Double) {
        let preShowDuration = personalFeedEntry.ProgramObject?.PreShow?.Clips![0].Media!.Duration ?? 0
        let showStartTimestamp = personalFeedEntry.ReleaseTimestamp! + preShowDuration
        
        let now = Date().timeIntervalSince1970
        // Show is already started
        if now > showStartTimestamp {
            return (false, now - showStartTimestamp)
        } else {
            return (true, now - personalFeedEntry.ReleaseTimestamp!)
        }
    }
    
    @objc func itemDidFinishPlaying() {
        if self.playbackState?.programType == .PersonalFeed && self.personalFeedPlaybackState != nil{
            if self.personalFeedPlaybackState!.isPlayingPreShow {
                self.personalFeedPlaybackState!.isPlayingPreShow = false
                let personalFeedEntry = self.personalFeedPlaybackState!.personalFeedEntry
                // Switch to show, no questions asked!
                self.play(programId: (personalFeedEntry!.ProgramObject?.ProgramId)!, title: personalFeedEntry!.ProgramObject?.Title, subtitle: personalFeedEntry!.ProgramObject?.Subtitle, mediaPath: (personalFeedEntry!.ProgramObject?.Show?.Clips?[0].Media?.Path)!)
            } else {
                // Playback is finished, close the player 
                self.stop()
            }
        } else {
            // Done! close the player (for all types of programs)
            self.stop()
        }
    }
    
    private func play(programId id: String, title: String?, subtitle: String?, mediaPath: String, forceRestartStream: Bool = true, seekPosition: CMTime = kCMTimeZero) {
        self.playbackState?.enable = true
        self.playbackState?.playing = true
        self.playbackState?.itemTitle = title
        self.playbackState?.itemSubtitle = subtitle
        
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[id]
        
        self.playbackState?.itemThumbnail = UIImage(data: entryProgramInfo?.thumbnailImageData ?? ProgramInfo.defaultThumbnailImageData)
        
        // Otherwise do not bother!
        if (self.playbackState?.mediaPath != mediaPath || forceRestartStream) {
            doPlay(mediaPath, seekPosition: seekPosition)
        }
        
        self.notifyModelUpdate(data: self.playbackState!)
    }
    
    // Calls from PlaybackController
    public func togglePlaybackState() {
        if (self.playbackState?.playing == true) {
            self.pause()
        } else {
            if playbackState != nil {
                self.resume()
            }
        }
    }

    public func resume() {
        self.playbackState?.playing = true
        self.doResume()
        self.notifyModelUpdate(data: self.playbackState!)
    }

    public func pause() {
        self.playbackState?.playing = false
        self.doPause()
        // save playback state in settings
        
        self.notifyModelUpdate(data: self.playbackState!)
    }
    
    public func stop() {
        self.playbackState?.playing = false
        self.playbackState?.enable = false
        self.doStop()
        self.notifyModelUpdate(data: self.playbackState!)
    }
    
    // Private methods
    private func doPlay(_ mediaPath: String, seekPosition: CMTime = kCMTimeZero) {
        if player != nil {
            // stop the previous player and let it get released by system
            player!.pause()
        }

        let playerItem = AVPlayerItem(asset: AVURLAsset(url: URL(string: mediaPath)!))
        player = AVPlayer(playerItem: playerItem)
        player?.seek(to: seekPosition)
        
        NotificationCenter.default.addObserver(self, selector:#selector(itemDidFinishPlaying), name:NSNotification.Name.AVPlayerItemDidPlayToEndTime, object:playerItem);
        self.player?.addObserver(self, forKeyPath: "status", options: NSKeyValueObservingOptions.initial, context: nil)
    }
    
    private func doPause() {
        if self.player != nil {
            self.player?.pause()
        }
    }
    
    private func doResume() {
        if self.player != nil {
            self.player?.rate = 1
        }
    }
    
    private func doStop() {
        if self.player != nil {
            self.player!.pause()
        }
        self.player = nil
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if object as? AVPlayer == player && keyPath == "status" {
            if player?.status == .readyToPlay {
                player?.play()
                player?.removeObserver(self, forKeyPath: "status")
            }
        }
    }
    
    @objc func handleAVInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
            let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
            let type = AVAudioSessionInterruptionType(rawValue: typeValue) else {
                return
        }
        if type == .began {
            // Interruption began, take appropriate actions
            self.pause()
        }
        else if type == .ended {
            if let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt {
                let options = AVAudioSessionInterruptionOptions(rawValue: optionsValue)
                if options.contains(.shouldResume) {
                    self.resume()
                } else {
                    // Interruption Ended - playback should NOT resume
                    self.stop()
                }
            }
        }
    }
    
    @objc func handleMediaServiceReset() {
        // We restart our stream only if in live mode
        // Later, we should think of adding support for feeds but this requires
        // seeking to the position in the file when we left off
        if self.playbackState?.programType == .Live {
            self.playLiveBroadcast(true)
        }
    }

    override func pullData() -> Promise<PlaybackState> {
        return Promise<PlaybackState> { seal in
            seal.resolve(self.playbackState, nil)
        }
    }
}

// Live lineup change listeners
extension PlaybackManager: ModelCommunicator {
    func modelUpdated(data: Any?) {
        if (self.playbackState?.programType == .Live && (self.playbackState?.enable)!) {
            let updatedLiveData = data as? LiveLineupData
            if (updatedLiveData?.liveBroadcastStatus?.StartedProgramTitle != self.playbackState?.itemTitle) {
                // Update view.
                self.playLiveBroadcast()
            } else if (updatedLiveData?.liveBroadcastStatus?.IsCurrentlyPlaying == false) {
                self.pause()
            }
        }
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

struct PersonalFeedPlaybackState {
    var personalFeedEntry: PersonalFeedEntry?
    // Flag used only in personal feeds
    var isPlayingPreShow: Bool = false
}

struct PlaybackState {
    var enable = false
    var programType: PlaybackProgramType?
    var mediaPath: String?
    var playing = false
    var itemThumbnail: UIImage?
    var itemTitle: String?
    var itemSubtitle: String?

    enum PlaybackProgramType {
        case Live
        case PublicFeed
        case PersonalFeed
        case Archive
    }
}
