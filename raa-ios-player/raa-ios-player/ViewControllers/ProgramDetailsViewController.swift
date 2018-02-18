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
    // The program for which we want to show the details (set in Live and Feed controllers)
    public var program: CProgram?
    public var programInfo: ProgramInfo?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        // Obtain data about program from ProgramInfoDirectoryManager
        firstly {
            Context.Instance.programInfoDirectoryManager.pullData()
        }.done { programInfoDirectory in
            self.programInfoDirectory = programInfoDirectory
            self.reloadDecription();
        }.ensure {
            Context.Instance.programInfoDirectoryManager.registerEventListener(listenerObject: self)
        }.catch { _ in
        }
    }
        
    func reloadDecription() {
        // Show program details
        if self.program != nil {
            if self.programInfoDirectory?.ProgramInfos[self.program!.ProgramId] != nil {
                self.programInfo = self.programInfoDirectory?.ProgramInfos[self.program!.ProgramId]
                self.programDescriptionText?.text = self.programInfo!.About
                self.programDescriptionText?.setNeedsDisplay()
            }
        }
    }
}

extension ProgramDetailsViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        self.programInfoDirectory = data as? ProgramInfoDirectory
        self.reloadDecription()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}
