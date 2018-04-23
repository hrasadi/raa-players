//
//  PlaybackModeRequesterModal.swift
//  raa-ios-player
//
//  Created by Hamid on 3/25/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import Presentr

class PlaybackModeRequesterModal : UIViewController {

    public var requestedEntry: Playable?

    @IBOutlet weak var resumePlaybackButton: UIButton!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        
    }
    
    override func viewWillAppear(_ animated: Bool) {
        var remainingMinsToPlayString:String? = nil

        // Some migrated media does not contain media length information
        if let _ = requestedEntry?.getMediaLength() {
            let remainingMinsToPlay: Int = Int(((requestedEntry?.getMediaLength())! -  Context.Instance.playbackManager.getLastPlaybackState((requestedEntry?.getMediaPath())!)) / 60)
            
            if remainingMinsToPlay == 0 {
                remainingMinsToPlayString = "کمتر از یک"
            } else {
                remainingMinsToPlayString = Utils.convertToPersianLocaleString(String(remainingMinsToPlay))
            }
        }
        
        if remainingMinsToPlayString != nil {
            resumePlaybackButton.setTitle("ادامه‌ی پخش (" + remainingMinsToPlayString! + " دقیقه مانده)", for: UIControlState.normal)
        } else {
            resumePlaybackButton.setTitle("ادامه‌ی پخش", for: UIControlState.normal)
        }
        super.viewWillAppear(animated)
    }
    
    @IBAction func onResumeButtonClicked(_ sender: Any) {
        if let requestedArchiveEntry = requestedEntry as? ArchiveEntry {
            Context.Instance.playbackManager.playArchiveEntry(requestedArchiveEntry, fromPos: Context.Instance.playbackManager.getLastPlaybackState((requestedEntry?.getMediaPath())!))
        } else if let requestedFeedEntry = requestedEntry as? PublicFeedEntry {
            Context.Instance.playbackManager.playPublicFeed(requestedFeedEntry.Id, fromPos: Context.Instance.playbackManager.getLastPlaybackState((requestedEntry?.getMediaPath())!))
        }
        dismiss(animated: true, completion: nil)
    }
    
    @IBAction func onRestartButtonClicked(_ sender: Any) {
        if let requestedArchiveEntry = requestedEntry as? ArchiveEntry {
            Context.Instance.playbackManager.playArchiveEntry(requestedArchiveEntry)
        } else if let requestedFeedEntry = requestedEntry as? PublicFeedEntry {
            Context.Instance.playbackManager.playPublicFeed(requestedFeedEntry.Id)
        }
        dismiss(animated: true, completion: nil)
    }
}

