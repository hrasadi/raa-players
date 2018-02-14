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

    static let LIVE_STREAM_URL = Context.LIVE_STREAM_URL_PREFIX + "/raa1.ogg"
    
    let audioSession = AVAudioSession.sharedInstance()
    var player: AVPlayer? = nil
    

    var playbackState: PlaybackState? = nil
    
    override init() {
        super.init()
        
        playbackState = PlaybackState()
    }

    // Calls from item list controllers
    public func playLiveBroadcast(_ currentProgram: CProgram) {
        os_log("Requested live playback")
        
        self.playbackState?.enable = true
        self.playbackState?.playing = true
        self.playbackState?.itemTitle = ""
        self.playbackState?.itemSubtitle = ""

        doPlay(PlaybackManager.LIVE_STREAM_URL)

        // Try to fetch info regarding the program being played
        self.updateLiveProgramPlaybackState(currentProgram)
    }

    public func updateLiveProgramPlaybackState(_ program: CProgram) {
        let programInfo = Context.Instance.programInfoDirectoryManager.programInfoDirectory?.ProgramInfos[(program.ProgramId)!]
        
        self.playbackState?.itemTitle = program.Title
        self.playbackState?.itemThumbnail = UIImage(data: programInfo?.thumbnailImageData ?? ProgramInfo.defaultThumbnailImageData)
        self.notifyModelUpdate()
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
        
        self.notifyModelUpdate()
    }
    
    // Calls from PlaybackController
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



