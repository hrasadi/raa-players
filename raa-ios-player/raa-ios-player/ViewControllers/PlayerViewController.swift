//
//  PlayerViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/10/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit
import UIKit
import MediaPlayer

class PlayerViewController : UIViewController {
    @IBOutlet weak var playerViewContainer: XibContainer!
    @IBOutlet weak var playerHeightConstraint: NSLayoutConstraint!
    var playerView: PlayerView?
    
    var playerHeightWhenVisible: CGFloat!
    
    var playbackState: PlaybackState?
    
    let mpInfoCenter: MPNowPlayingInfoCenter = MPNowPlayingInfoCenter.default()

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)

        // we should also handle MPInfoCenter callbacks
        self.becomeFirstResponder()
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        // Start safe
        
        self.playerHeightWhenVisible = playerHeightConstraint.constant
        
        self.view.bringSubview(toFront: playerViewContainer)

        self.playbackState = try! hang(Context.Instance.playbackManager.pullData())
        Context.Instance.playbackManager.registerEventListener(listenerObject: self)

        self.playerView = playerViewContainer.contentView as? PlayerView
        self.playerView?.delegate = self

        self.updatePlayerView()
    }
    
    func updatePlayerView() {
        if self.playbackState == nil || self.playbackState!.enable == false {
            self.setPlayerViewVisibility(isHidden: true)
            mpInfoCenter.nowPlayingInfo = nil
            UIApplication.shared.endReceivingRemoteControlEvents()
        } else {
            self.setPlayerViewVisibility(isHidden: false)
            // Attach remote control
            UIApplication.shared.beginReceivingRemoteControlEvents()
        }

        if self.playbackState != nil && self.playbackState?.enable == true {
            if self.playbackState!.playing == true {
                self.playerView?.playbackState.setImage(#imageLiteral(resourceName: "pause"), for: UIControlState.normal)
            } else {
                self.playerView?.playbackState.setImage(#imageLiteral(resourceName: "play"), for: UIControlState.normal)
            }
         
            self.playerView?.itemTitle.text = self.playbackState?.itemTitle
            self.playerView?.itemSubtitle.text = self.playbackState?.itemSubtitle
            self.playerView?.itemThumbnail.image = self.playbackState?.itemThumbnail
            
            // Register 
            DispatchQueue.main.async {
                self.mpInfoCenter.nowPlayingInfo = [MPMediaItemPropertyAlbumTitle: self.playerView?.itemTitle.text ?? "", MPMediaItemPropertyTitle: self.playerView?.itemSubtitle.text ?? "", MPMediaItemPropertyArtist: "رادیو اتو-اسعد", MPMediaItemPropertyArtwork: MPMediaItemArtwork(boundsSize: (self.playerView?.itemThumbnail.image?.size)!) { sz in return (self.playbackState?.itemThumbnail)! }, MPNowPlayingInfoPropertyPlaybackRate: self.playbackState!.playing ? 1 : 0]
            }
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

    @objc override func remoteControlReceived(with event: UIEvent?) {
        let rc: UIEventSubtype = event!.subtype
        
        if (rc == .remoteControlPlay) {
            Context.Instance.playbackManager.resume()
            self.mpInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = 1
        } else if (rc == .remoteControlPause) {
            Context.Instance.playbackManager.pause()
            self.mpInfoCenter.nowPlayingInfo?[MPNowPlayingInfoPropertyPlaybackRate] = 0
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
        self.playbackState = data as? PlaybackState
        self.updatePlayerView()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}


