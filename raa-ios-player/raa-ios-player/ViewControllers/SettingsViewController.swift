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
    @IBOutlet weak var notifyOnLiveProgram: UISwitch!
    @IBOutlet weak var publicProgramNotificationDetailsLink: UILabel!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
    }
    
    override func viewWillAppear(_ animated: Bool) {
        let user = Context.Instance.userManager.user
        // Show settings in simulator. but hide them on device if user said so
        #if !((arch(i386) || arch(x86_64)) && os(iOS))
        if (user.NotificationToken == nil) {
            self.tableView.isUserInteractionEnabled = false
            self.notifyOnPersonalProgram.isEnabled = false
            self.notifyOnLiveProgram.isEnabled = false
        } else {
            self.tableView.isUserInteractionEnabled = true
        }
        #endif
        self.notifyOnPersonalProgram.isOn = Bool.init(exactly: user.NotifyOnPersonalProgram as NSNumber)!
        self.notifyOnLiveProgram.isOn = Bool.init(exactly: user.NotifyOnLiveProgram as NSNumber)!
    }
    
    
    @IBAction func SettingsValueChanged(_ sender: Any) {
        let user = Context.Instance.userManager.user
        var dirty = false
        
        if self.notifyOnPersonalProgram.isOn != Bool.init(exactly: user.NotifyOnPersonalProgram as NSNumber)! {
            Context.Instance.userManager.user.NotifyOnPersonalProgram = self.notifyOnPersonalProgram.isOn ? 1 : 0
            dirty = true
        }
        if self.notifyOnLiveProgram.isOn != Bool.init(exactly: user.NotifyOnLiveProgram as NSNumber)! {
            Context.Instance.userManager.user.NotifyOnLiveProgram = self.notifyOnLiveProgram.isOn ? 1 : 0
            dirty = true
        }
        
        if dirty {
            Context.Instance.userManager.registerUser()
        }
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
    }
}
