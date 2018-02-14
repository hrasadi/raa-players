//
//  MainTabBarController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/11/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit


class MainTabBarViewController : UITabBarController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        self.selectedIndex = 1
    }
}
