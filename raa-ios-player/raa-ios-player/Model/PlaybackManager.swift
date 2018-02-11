//
//  PlaybackManager.swift
//  raa-ios-player
//
//  Created by Hamid on 2/10/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import os
import AVFoundation
import UIKit

class PlaybackManager : UICommunicator {

    let audioSession = AVAudioSession.sharedInstance()
    var player: AVPlayer? = nil
    

    var playbackState: PlaybackState? = nil
    
    override init() {
        super.init()
        
        playbackState = PlaybackState()
    }

    // Messages from item list controllers
    
    public func playLiveBroadcast() {
        
    }
    
    public func playPersonalFeed() {
        
    }
    
    public func playFeed(_ feedEntryId: String) {
        os_log("Requested playback of %@", type: .default, feedEntryId)
        
        let requestedFeedEntry = Context.Instance.feedManager.lookupPublicFeedEntry(feedEntryId)
        
        if requestedFeedEntry != nil {
            self.playbackState?.enable = true
            self.playbackState?.playing = true
            self.playbackState?.itemTitle = requestedFeedEntry?.ProgramObject?.Title
            //self.playbackState?.itemSubtitle = requestedFeedEntry?.ProgramObject.
            
            doPlay((requestedFeedEntry!.ProgramObject?.Show?.Clips?[0].Media?.Path)!)
            
            self.notifyModelUpdate()
        }
    }
    
    // Messages from playbackController
    
    public func togglePlaybackState() {
        if (self.playbackState?.playing == true) {
            self.playbackState?.playing = false
            self.doPause()
        } else {
            if playbackState != nil {
                self.playbackState?.playing = true
                self.doResume()
            }
        }
        
        self.notifyModelUpdate()
    }
    
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

    override func pullData() -> Any? {
        return self.playbackState
    }

    struct PlaybackState {
        var enable = false
        var playing = false
        var itemThumbnail: UIImage?
        var itemTitle: String?
        var itemSubtitle: String?
    }
}



