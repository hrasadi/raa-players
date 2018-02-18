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

class PlaybackManager : UICommunicator<PlaybackState> {

    static let LIVE_STREAM_URL = Context.LIVE_STREAM_URL_PREFIX + "/raa1.ogg"
    
    let audioSession = AVAudioSession.sharedInstance()
    var player: AVPlayer? = nil
    

    var playbackState: PlaybackState? = nil
    
    override init() {
        super.init()
        
        playbackState = PlaybackState()
        
        initAudioSession()
    }
    
    private func initAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, with: [AVAudioSessionCategoryOptions.allowBluetooth, AVAudioSessionCategoryOptions.mixWithOthers, AVAudioSessionCategoryOptions.duckOthers])
            //NotificationCenter.default.addObserver(self, selector: #selector(handleAVInterruption), name: .AVAudioSessionInterruption, object: nil)
        } catch let e {
            print("Error while configuring AudioSession. Error is: " + e.localizedDescription)
        }
    }

    // Calls from item list controllers
    public func playLiveBroadcast() {

        os_log("Requested live playback", type: .default)

        let program = Context.Instance.liveBroadcastManager.liveLineupData.flattenLiveLineup?[Context.Instance.liveBroadcastManager.getMostRecentProgramIndex()!]
        if program != nil {
            self.play(programId: (program?.ProgramId)!, title: (program?.Title)!, subtitle: (program?.Subtitle)!, mediaPath: PlaybackManager.LIVE_STREAM_URL)
            return
        }
    }

    public func playFeed(_ feedEntryId: String) {
        os_log("Requested playback of %@", type: .default, feedEntryId)
        
        let publicFeedEntry = Context.Instance.feedManager.lookupPublicFeedEntry(feedEntryId)
        if publicFeedEntry != nil {
            self.play(programId: (publicFeedEntry!.ProgramObject?.ProgramId)!, title: publicFeedEntry!.ProgramObject?.Title, subtitle: publicFeedEntry!.ProgramObject?.Subtitle, mediaPath: (publicFeedEntry!.ProgramObject?.Show?.Clips?[0].Media?.Path)!)
            return
        } else {
            let personalFeedEntry = Context.Instance.feedManager.lookupPersonalFeedEntry(feedEntryId)
            if personalFeedEntry != nil {
                self.play(programId: (personalFeedEntry!.ProgramObject?.ProgramId)!, title: personalFeedEntry!.ProgramObject?.Title, subtitle: personalFeedEntry!.ProgramObject?.Subtitle, mediaPath: (personalFeedEntry!.ProgramObject?.Show?.Clips?[0].Media?.Path)!)
                return
            }
        }

        // We should never come here
        os_log("This is a bug, we have an entry that is neither personal or public", type: .error)
    }
    
    private func play(programId id: String, title: String?, subtitle: String?, mediaPath: String) {
        self.playbackState?.enable = true
        self.playbackState?.playing = true
        self.playbackState?.itemTitle = title
        self.playbackState?.itemSubtitle = subtitle
        
        let entryProgramInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[id]
        
        self.playbackState?.itemThumbnail = UIImage(data: entryProgramInfo?.thumbnailImageData ?? ProgramInfo.defaultThumbnailImageData)
        
        doPlay(mediaPath)
        
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
        self.notifyModelUpdate(data: self.playbackState!)
    }
    

    // Private methods
    private func doPlay(_ mediaPath: String) {
        if player != nil {
            // stop the previous player and let it get released by system
            player!.pause()
        }
        
        let playerItem = AVPlayerItem(asset: AVURLAsset(url: URL(string: mediaPath)!))
        player = AVPlayer(playerItem: playerItem)
        
        self.player?.addObserver(self, forKeyPath: "status", options: NSKeyValueObservingOptions.initial, context: nil)
    }
    
    private func doPause() {
        if self.player != nil {
            player?.pause()
        }
    }
    
    private func doResume() {
        if self.player != nil {
            player?.rate = 1
        }
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if object as? AVPlayer == player && keyPath == "status" {
            if player?.status == .readyToPlay {
                player?.play()
                player?.removeObserver(self, forKeyPath: "status")
            }
        }
    }

    override func pullData() -> Promise<PlaybackState> {
        return Promise<PlaybackState> { seal in
            seal.resolve(self.playbackState, nil)
        }
    }
}

struct PlaybackState {
    var enable = false
    var playing = false
    var itemThumbnail: UIImage?
    var itemTitle: String?
    var itemSubtitle: String?
}



