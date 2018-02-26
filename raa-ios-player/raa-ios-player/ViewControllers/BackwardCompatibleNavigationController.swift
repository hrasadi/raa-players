//
//  BackwardCompatibleNavigationController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/21/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
class BackwardCompatibleNavigationController: UINavigationController {
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        if #available(iOS 11.0, *) {
            self.navigationBar.prefersLargeTitles = true
        }
    }
}
