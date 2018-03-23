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
        super.viewDidLoad()
        
        self.selectedIndex = 0
    }
}

extension MainTabBarViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        let targetItemIndex = data as? Int
        if targetItemIndex != nil {
            self.selectedIndex = targetItemIndex!
        }
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
    
    
}
