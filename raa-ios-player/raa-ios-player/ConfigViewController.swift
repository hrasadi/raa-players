//
//  ConfigViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 9/13/17.
//  Copyright © 2017 Auto-asaad. All rights reserved.
//

import UIKit

class ConfigViewController : UITableViewController {
        
    @IBOutlet weak var alwaysPlaySwitch: UISwitch!

    @IBOutlet weak var notifyNewProgramSwitch: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        alwaysPlaySwitch.isOn = Settings.getValue(Settings.AlwaysPlayKey)!
        notifyNewProgramSwitch.isOn = Settings.getValue(Settings.NotifyNewProgramKey)!
    }
    
    override func tableView(_ tableView: UITableView, willDisplayHeaderView view: UIView, forSection section: Int) {
        
        let header = view as! UITableViewHeaderFooterView
        header.textLabel?.font = UIFont(name: "B Roya", size: 17)!
        header.textLabel?.textColor = UIColor.gray
        header.textLabel?.textAlignment = .right

    }
    
    @IBAction func alwaysPlaySwitchValueChange(_ sender: Any) {
        Settings.setValue(Settings.AlwaysPlayKey, newValue: (sender as! UISwitch).isOn)
        
    }
    
    @IBAction func notifyNewProgramSwitchValueChange(_ sender: Any) {
        Settings.setValue(Settings.NotifyNewProgramKey, newValue: (sender as! UISwitch).isOn)
    }
    
}

