//
//  ConfigViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 9/13/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit

class ConfigViewController : UITableViewController {
        
    @IBOutlet weak var backgroundPlaySwitch: UISwitch!

    @IBOutlet weak var notifyNewProgramSwitch: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        backgroundPlaySwitch.isOn = Settings.getValue(Settings.BackgroundPlayKey)!
        
        if (!Settings.authorizedToSendNotification) {
            notifyNewProgramSwitch.isEnabled = false
            // Show table footer (see below)
        } else {
            notifyNewProgramSwitch.isEnabled = true
            // Hide the help label
        }
        notifyNewProgramSwitch.isOn = Settings.getValue(Settings.NotifyNewProgramKey)!
    }
    
    override func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        let header = view as! UITableViewHeaderFooterView
        header.textLabel?.textColor = UIColor.gray
        header.textLabel?.textAlignment = .right

    }
    
    override func tableView(_ tableView: UITableView, willDisplayFooterView view: UIView, forSection section: Int) {
        let footer = view as! UITableViewHeaderFooterView
        
        if (!Settings.authorizedToSendNotification) {
            footer.textLabel?.textAlignment = .right
        } else {
            footer.textLabel?.text = ""
        }
    }
    
    @IBAction func backgroundPlaySwitchValueChange(_ sender: Any) {
        Settings.setValue(Settings.BackgroundPlayKey, newValue: (sender as! UISwitch).isOn)
        
    }
    
    @IBAction func notifyNewProgramSwitchValueChange(_ sender: Any) {
        Settings.setValue(Settings.NotifyNewProgramKey, newValue: (sender as! UISwitch).isOn)
    }
    
}

