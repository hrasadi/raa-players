//
//  ProgramDetailsViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import PromiseKit
import UIKit

class ProgramDetailsViewController : UIViewController {
    
    @IBOutlet var programDescriptionText: UITextView?
    
    public var programInfoDirectory: ProgramInfoDirectory?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        // Obtain data about program from ProgramInfoDirectoryManager
        firstly {
            Context.Instance.programInfoDirectoryManager.pullData()
        }.done { programInfoDirectory in
            self.programInfoDirectory = programInfoDirectory
        }.ensure {
            Context.Instance.programInfoDirectoryManager.registerEventListener(listenerObject: self)
        }.catch { _ in
        }
    }
        
    override func viewDidAppear(_ animated: Bool) {
        self.reloadDescription()
        super.viewDidAppear(animated)
    }
    
    func reloadDescription() {
        let programId = ((self.parent as? DetailViewController)?.card as! ProgramCard).programId
        if programId != nil {
            self.programDescriptionText?.text = "هنوز چیزی به ذهنمون نرسیده در مورد این برنامه بنویسیم!"
            if self.programInfoDirectory?.ProgramInfos[programId!] != nil {
                self.programDescriptionText?.text = self.programInfoDirectory?.ProgramInfos[programId!]?.About
            }
            self.programDescriptionText?.setNeedsDisplay()
        }
    }
}

extension ProgramDetailsViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        self.programInfoDirectory = data as? ProgramInfoDirectory
        self.reloadDescription()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}
