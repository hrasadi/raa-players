//
//  ProgramDetailsViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/1/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class ProgramDetailsViewController : UIViewController {
    
    @IBOutlet var programDescriptionText : UITextView?
    
    // The program for which we want to show the details (set in Live and Feed controllers)
    public var program : CProgram?

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        // Show program details
        // Obtain data about program from ProgramManager
        // Show
        programDescriptionText?.text = "سلام. در این برنامه ما یکی از خفن‌ترین کارهای ممکن رو می‌کنیم!"
        programDescriptionText?.sizeToFit()
    }
    
}
