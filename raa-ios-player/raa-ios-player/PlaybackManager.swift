//
//  PlaybackManager.swift
//  raa-ios-player
//
//  Created by Hamid on 9/14/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit
import Foundation
import AVFoundation
import UserNotifications
import MediaPlayer

class PlaybackManager : NSObject {

    let audioSession = AVAudioSession.sharedInstance()
    let mpInfoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default()

    var player: AVPlayer? = nil

    var isCurrentlyPlaying: Bool?

    // When playback in progress
    var currentBox: String?
    var currentProgram: String?
    var currentClip: String?
    
    // When playback not in progress
    var nextBoxId: String?
    var nextBoxStartTime: Date?
    
    private var activeTimer: Timer?
    private let dateFormatter = DateFormatter()
    private let coarseDateFormatter = DateFormatter()

    private var unsentProgramStartMessage: String?
    // Returns the new program name
    private var onProgramStartCallback: ((String) -> Void)?
    public var programStartCallback: ((String) -> Void)? {
        get {
            return onProgramStartCallback
        }
        set(callback) {
            onProgramStartCallback = callback
            if unsentProgramStartMessage != nil {
                onProgramStartCallback!(unsentProgramStartMessage!)
            }
        }
    }
    
    private var unsentPlaybackStopMessage: (nextBoxId: String?, nextBoxStartTime: Date?)?
    // Returns the upcoming box Id and date for start
    private var onPlaybackStopCallback: ((String?, Date?) -> Void)?
    public var playbackStopCallback: ((String?, Date?) -> Void)?  {
        get {
            return onPlaybackStopCallback
        }
        set(callback) {
            onPlaybackStopCallback = callback
            if unsentPlaybackStopMessage != nil {
                onPlaybackStopCallback!(unsentPlaybackStopMessage!.nextBoxId, unsentPlaybackStopMessage!.nextBoxStartTime)
            }
        }
    }
    
    override init() {
        super.init()
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZZZZ"
        coarseDateFormatter.dateFormat = "yyyy-MM-dd"
                        
        // And also configure AudioSession to show on control center
        do {
            try audioSession.setCategory(AVAudioSessionCategoryPlayback, with: [AVAudioSessionCategoryOptions.allowBluetooth, AVAudioSessionCategoryOptions.mixWithOthers, AVAudioSessionCategoryOptions.duckOthers])
            NotificationCenter.default.addObserver(self,
                                           selector: #selector(handleAVInterruption),
                                           name: .AVAudioSessionInterruption,
                                           object: nil)
        } catch let e {
            print("Error while configuring AudioSession. Error is: " + e.localizedDescription)
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
            player?.pause()
        }
        else if type == .ended {
            if let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt {
                let options = AVAudioSessionInterruptionOptions(rawValue: optionsValue)
                if options.contains(.shouldResume) {
                    do {
                        try audioSession.setActive(true)
                        player?.play()
                    } catch let e {
                        print("Error happened while starting playback: " + e.localizedDescription)
                    }
                } else {
                    // Interruption Ended - playback should NOT resume
                    self.stop()
                    unpopulateMediaInfoCenterNowPlaying()
                }
            }
        }
    }
    
    /*
     IMPLEMENTATION NOTE: In some cases, the activate func is called, without prior deactivate. 
                        Example is when user opens the control center drawer on top of the active app and then closes it. iOS will call activate only on drawer closing. We need to handle this case also
     */
    func activate() {
        // Run once
        loadStatus(true)
        
        self.play()
        
        if (activeTimer == nil) {
            activeTimer = Timer.scheduledTimer(timeInterval: 10, target: self, selector: #selector(self.loadStatus), userInfo: nil, repeats: true)
        } else {
            // There is already an active time, so do not do anything
            print("Have already an active timer. Skipping timer instantiation")
        }
    }
    
    func deactivate() {
        // Stop the player and unpopulate the media info center unless user allow us to it
        // Also, if nothing is playing, there is not need to continue playback in background
        if (!(Settings.getValue(Settings.BackgroundPlayKey) ?? false) || !(isCurrentlyPlaying ?? false)) {
            self.stop()
            unpopulateMediaInfoCenterNowPlaying()
        }
        
        activeTimer?.invalidate()
        activeTimer = nil
    }
    
    func play() {
        if (player == nil) {
            let playerItem = AVPlayerItem(asset: AVURLAsset(url: URL(string: "https://stream.raa.media/raa1.ogg")!))
            player = AVPlayer(playerItem: playerItem)

            self.player?.addObserver(self, forKeyPath: "status", options: NSKeyValueObservingOptions.initial, context: nil)
        }
        
        populateMediaInfoCenterNowPlaying()
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if object as? AVPlayer == player && keyPath == "status" {
            if player?.status == .readyToPlay {
                player?.play()
                player?.removeObserver(self, forKeyPath: "status")
            } 
        }
    }
    
    func stop(unpopulateMediaInfoCenter: Bool = false) {
        // Deactivate player when going inactive (we must re-activate if we are supposed to playback in backgound)
        player?.pause()
        player = nil
    }

    func shutDown() {
        do {
            try audioSession.setActive(false)
        } catch let e {
            print("Error while deactivating the audio session. Inner exception is: " + e.localizedDescription)
        }
    }

    func populateMediaInfoCenterNowPlaying() {
        let image = UIImage(named: "raa-logo-256.png")!
        if (currentProgram != nil) {
            mpInfoCenter.nowPlayingInfo = [MPMediaItemPropertyAlbumTitle: currentClip ?? "",
                                           MPMediaItemPropertyTitle: (currentProgram! != "BLANK") ? currentProgram! : "بخش بعدی برنامه‌ها به زودی",
                                           MPMediaItemPropertyArtist: "رادیو اتو-اسعد",
                                           MPMediaItemPropertyArtwork: MPMediaItemArtwork(boundsSize: image.size) { sz in return image
                                            },
                                           MPNowPlayingInfoPropertyPlaybackRate: player?.rate ?? 0]
            
            // listen info center events
            UIApplication.shared.beginReceivingRemoteControlEvents()
            
            MPRemoteCommandCenter.shared().pauseCommand.addTarget { (event) -> MPRemoteCommandHandlerStatus in
                self.stop()
                self.mpInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = 0
                return .success
            }
            MPRemoteCommandCenter.shared().stopCommand.addTarget { (event) -> MPRemoteCommandHandlerStatus in
                self.stop()
                self.mpInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = 0
                return .success
            }
            MPRemoteCommandCenter.shared().playCommand.addTarget { (event) -> MPRemoteCommandHandlerStatus in
                self.play()
                return .success
            }

        }
    }
    
    func unpopulateMediaInfoCenterNowPlaying() {
        mpInfoCenter.nowPlayingInfo = nil
    }

    @objc func loadStatus(_ forceUpdateUI: Bool = false) {
        if let data = try? Data.init(contentsOf: URL(string: "http://raa.media/lineups/status.json")!) {
            let status = try? JSONSerialization.jsonObject(with: data, options: []) as! Dictionary<String, Any>
            
            let isCurrentlyPlaying_new = status?["isCurrentlyPlaying"] as? Bool ?? false

            let currentBox_new = status?["currentBox"] as? String
            let currentProgram_new = status?["currentProgram"] as? String
            let currentClip_new = status?["currentClip"] as? String

            let nextBoxId_new = status?["nextBoxId"] as? String
            var nextBoxStartTime_new: Date?;
            if let nextBoxStartTimeString = status?["nextBoxStartTime"] as? String {
                nextBoxStartTime_new = dateFormatter.date(from: nextBoxStartTimeString)
            }
            
            // Before moving on, we decide whether or not a new program was started since last status
            if isCurrentlyPlaying_new {
                // Is this a new program?
                // Note that we also cover the transition of playback start by knowing the fact that 'currentProgram' in the case of no program is 'BLANK'
                if currentProgram_new != currentProgram || currentBox_new != currentBox || forceUpdateUI {
                    // Update player bar in foreground mode
                    if onProgramStartCallback != nil {
                        onProgramStartCallback!(currentProgram_new!)
                    } else {
                       unsentProgramStartMessage = currentProgram_new
                    }
                }
            } else {
                if (!isCurrentlyPlaying_new && isCurrentlyPlaying_new != isCurrentlyPlaying) || forceUpdateUI {
                    // If playback stopped
                    // Update player bar
                    if onPlaybackStopCallback != nil {
                        onPlaybackStopCallback!(nextBoxId_new, nextBoxStartTime_new)
                    } else {
                        unsentPlaybackStopMessage = (nextBoxId: nextBoxId_new, nextBoxStartTime: nextBoxStartTime_new)
                    }
                }
            }

            // Update state
            isCurrentlyPlaying = isCurrentlyPlaying_new
            currentBox = currentBox_new
            currentProgram = currentProgram_new
            currentClip = currentClip_new
            nextBoxId = nextBoxId_new
            nextBoxStartTime = nextBoxStartTime_new
            
            populateMediaInfoCenterNowPlaying()
        }
    }    
}

