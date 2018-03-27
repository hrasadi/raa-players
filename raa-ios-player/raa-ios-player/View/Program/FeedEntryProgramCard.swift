//
//  PublicFeedProgramCard.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

@IBDesignable
class FeedEntryProgramCard : ProgramCard {
    public var feedEntryId: String?
    public var playableState: PlayableState?

    private let currentlyPlayingBackgroundColor = UIColor(red: 227/255, green: 33/255, blue: 0/255, alpha: 1)

    public var feedDelegate: FeedEntryCardDelegate?

    override public init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initialize()
    }

    override func initialize() {
        super.initialize()

        self.timeTitle1 = "انتشار"
        self.timeTitle2 = "انقضا"
        
        self.actionable = true
    }
    
    override open func draw(_ rect: CGRect) {
        super.draw(rect)
        
        self.currentActionBtnBackgroundColor = ProgramCard.DEFAULT_ACTION_BTN_COLOR
        let btnTitle = NSAttributedString(string: "پخش", attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
        actionBtn.setAttributedTitle(btnTitle, for: .normal)
        
        if playableState == .CurrentlyPlaying {
            presentAsCurrentlyPlaying()
        } else {
            presentAsPlayable()
        }
        
        layout()
    }
    
    override func layout(animating: Bool = true) {
        super.layout(animating: animating)
    }
    
    func presentAsCurrentlyPlaying() {
        self.actionBtn.isUserInteractionEnabled = true
        self.currentActionBtnBackgroundColor = currentlyPlayingBackgroundColor
        
        let buttonText = "توقف"
        let btnTitle = NSAttributedString(string: buttonText, attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
        actionBtn.setAttributedTitle(btnTitle, for: .normal)
    }
    
    func presentAsPlayable() {
        self.actionBtn.isUserInteractionEnabled = true
        self.currentActionBtnBackgroundColor = ProgramCard.DEFAULT_ACTION_BTN_COLOR
        
        let btnTitle = NSAttributedString(string: "پخش", attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
        actionBtn.setAttributedTitle(btnTitle, for: .normal)
    }

    
    @objc override func actionButtonTapped() {
        super.actionButtonTapped()
        
        if self.feedDelegate != nil {
            self.feedDelegate!.onPlayButtonClicked(self.feedEntryId ?? "")
        }
    }
}

enum PlayableState {
    case CurrentlyPlaying
    case Playable
}

class FeedEntryCardTableViewCell : UITableViewCell {
    @IBOutlet public var card: FeedEntryProgramCard?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(style aStyle: UITableViewCellStyle, reuseIdentifier rid: String?) {
        super.init(style: aStyle, reuseIdentifier: rid)
    }
}

protocol FeedEntryCardDelegate {
    func onPlayButtonClicked(_ requestedFeedEntryId: String)
}
