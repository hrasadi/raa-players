//
//  ProgramArchiveEntryCard.swift
//  raa-ios-player
//
//  Created by Hamid on 2/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class ProgramArchiveEntryCard : FeedEntryProgramCard {
    public var archiveEntry: ArchiveEntry?
    
    public var archiveEntryDelegate: ProgramArchiveEntryDelegate?
    
    required public init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        initialize()
    }

    override func initialize() {
        super.initialize()
        
        self.timeTitle1 = "انتشار"
        self.timeTitle2 = ""
        
        self.timeValue2 = ""
        
        self.actionable = true
    }
    
    override func draw(_ rect: CGRect) {
        super.draw(rect)
        
        self.programTitleLbl.numberOfLines = 3
        self.programTitleLbl.minimumScaleFactor = 0.5

        layout()
    }
    
    @objc override func actionButtonTapped() {
        super.actionButtonTapped()
        
        if self.archiveEntryDelegate != nil {
            self.archiveEntryDelegate!.onPlayButtonClicked(self.archiveEntry ?? nil)
        }
    }

}

class ProgramArchiveEntryTableViewCell : UITableViewCell {
    @IBOutlet public var card: ProgramArchiveEntryCard?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

protocol ProgramArchiveEntryDelegate {
    func onPlayButtonClicked(_ requestedArchiveEntry: ArchiveEntry?)
}
