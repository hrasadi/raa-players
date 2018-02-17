//
//  SettingsViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 1/25/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit

class SettingsViewController : UITableViewController {

    @IBOutlet weak var notifyOnPersonalProgram: UISwitch!
    @IBOutlet weak var notifyOnPublicProgram: UISwitch!
    @IBOutlet weak var notifyOnLiveProgram: UISwitch!
    @IBOutlet weak var playInBackground: UISwitch!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        var user: User = Context.Instance.userManager.user
        
        self.notifyOnPersonalProgram.isOn = Bool.init(exactly: user.NotifyOnPersonalProgram! as NSNumber)!
        self.notifyOnPublicProgram.isOn = Bool.init(exactly: user.NotifyOnPublicProgram! as NSNumber)!
        self.notifyOnLiveProgram.isOn = Bool.init(exactly: user.NotifyOnLiveProgram! as NSNumber)!
        
        // TODO
        //self.playInBackground.isOn = (user?.PlayInBackground)!
    }
}
