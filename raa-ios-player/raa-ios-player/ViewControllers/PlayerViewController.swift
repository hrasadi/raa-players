//
//  PlayerViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/10/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import MediaPlayer

class PlayerViewController : UIViewController {
    @IBOutlet weak var playerViewContainer: XibContainer!
    @IBOutlet weak var playerHeightConstraint: NSLayoutConstraint!
    var playerView: PlayerView?
    
    var playerHeightWhenVisible: CGFloat!
    
    var playbackState: PlaybackManager.PlaybackState?
    
    let mpInfoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default()

    override func viewDidLoad() {
        self.playerHeightWhenVisible = playerHeightConstraint.constant
        
        self.view.bringSubview(toFront: playerViewContainer)

        self.playerView = playerViewContainer.contentView as? PlayerView
        self.playerView?.delegate = self
        
        Context.Instance.playbackManager.registerEventListener(listenerObject: self)
        self.playbackState = Context.Instance.feedManager.pullData() as? PlaybackManager.PlaybackState
        
        self.updatePlayerView()
    }
    
    func updatePlayerView() {
        if self.playbackState == nil || self.playbackState!.enable == false {
            self.setPlayerViewVisibility(isHidden: true)
        } else {
            self.setPlayerViewVisibility(isHidden: false)
        }

        if self.playbackState != nil {
            if self.playbackState!.playing == true {
                self.playerView?.playbackState.setImage(#imageLiteral(resourceName: "pause"), for: UIControlState.normal)
                self.playerView?.itemTitle.text = self.playbackState?.itemTitle
            } else {
                self.playerView?.playbackState.setImage(#imageLiteral(resourceName: "play"), for: UIControlState.normal)
            }
         
            self.playerView?.itemThumbnail.image = self.playbackState?.itemThumbnail
        }
    }
    
    func setPlayerViewVisibility(isHidden: Bool = false, animated: Bool = true) {
        if (isHidden) {
            playerHeightConstraint.constant = 0
            playerViewContainer.isHidden = true
        } else {
            playerHeightConstraint.constant = playerHeightWhenVisible
            playerViewContainer.isHidden = false
        }

        if animated {
            UIView.animate(withDuration: 0.2, animations: {
                () -> Void in
                self.view.layoutIfNeeded()
            }, completion: nil)
        } else {
            view.layoutIfNeeded()
        }
    }
}

extension PlayerViewController : PlayerViewDelegate {
    func onPlayPauseButtonClicked() {
        // toggle playback state
        Context.Instance.playbackManager.togglePlaybackState()
    }
}

extension PlayerViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        self.playbackState = Context.Instance.playbackManager.pullData() as? PlaybackManager.PlaybackState
        self.updatePlayerView()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}


