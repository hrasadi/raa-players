//
//  LiveBroadcastProgramCard.swift
//  raa-ios-player
//
//  Created by Hamid on 2/13/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

@IBDesignable
class LiveBroadcastProgramCard : ProgramCard {
    public var feedEntryId: String?
    
    public var liveBroadcastDelegate: LiveBroadcastDelegate?

    public var liveBroadcastPlayableState: LiveBroadcastPlayableState = .NotPlayable
    
    public var nextInLineCountdownValue: String? {
        didSet {
            if self.liveBroadcastPlayableState == .NextInLine {
                let buttonText = (nextInLineCountdownValue != nil) ? ("شروع در " + nextInLineCountdownValue!) : "به زودی"
                let btnTitle = NSAttributedString(string: buttonText, attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
                actionBtn.setAttributedTitle(btnTitle, for: .normal)
            }
        }
    }
    
    private var nextInLineBtnBackgroundColor = UIColor(red: 225/255, green: 227/255, blue: 62/255, alpha: 1)
    
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
        
        self.liveBroadcastPlayableState = .NotPlayable
    }
    
    func presentAsNextInLine() {
        self.actionable = true

        self.actionBtn.isUserInteractionEnabled = false // No touch
        actionBtn.layer.backgroundColor = nextInLineBtnBackgroundColor.cgColor

        let buttonText = (self.nextInLineCountdownValue != nil) ? ("شروع در " + nextInLineCountdownValue!) : "به زودی"
        let btnTitle = NSAttributedString(string: buttonText, attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
        actionBtn.setAttributedTitle(btnTitle, for: .normal)
    }
    
    func presentAsPlayable() {
        self.actionable = true

        self.actionBtn.isUserInteractionEnabled = true // No touch
        actionBtn.layer.backgroundColor = actionBtnBackgroundColor.cgColor

        let btnTitle = NSAttributedString(string: "پخش", attributes: [ NSAttributedStringKey.font : UIFont.systemFont(ofSize: 16, weight: .medium), NSAttributedStringKey.foregroundColor : UIColor.white])
        actionBtn.setAttributedTitle(btnTitle, for: .normal)
    }
    
    override open func draw(_ rect: CGRect) {
        super.draw(rect)
        
        switch liveBroadcastPlayableState {
        case .NotPlayable:
            self.actionable = false
            break
        case .NextInLine:
            self.presentAsNextInLine()
            break
        case .Playable:
            self.presentAsPlayable()
        }
        
        layout()
    }
    
    override func layout(animating: Bool = true) {
        super.layout(animating: animating)
    }

    enum LiveBroadcastPlayableState {
        case NotPlayable
        case NextInLine
        case Playable
    }
}

class LiveBroadcastProgramCardTableViewCell : UITableViewCell {
    @IBOutlet public var card: LiveBroadcastProgramCard?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override init(style aStyle: UITableViewCellStyle, reuseIdentifier rid: String?) {
        super.init(style: aStyle, reuseIdentifier: rid)
    }
}

protocol LiveBroadcastDelegate {
    func onPlayButtonClicked()
}
