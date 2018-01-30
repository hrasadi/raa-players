//
//  FeedView.swift
//  raa-ios-player
//
//  Created by Hamid on 1/24/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class FeedViewController : UIViewController {

    @IBOutlet var player: PlayerView!;

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }

    override func viewDidLoad() {
        super.viewDidLoad();
        
        // XIB file, please be silent and behave to the constraint your superview tell you :)
        player.translatesAutoresizingMaskIntoConstraints = false
    }
}
