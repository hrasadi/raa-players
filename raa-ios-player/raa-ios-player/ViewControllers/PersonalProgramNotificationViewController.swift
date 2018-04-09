//
//  PersonalProgramNotificationViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/22/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import PromiseKit

class PersonalProgramNotificationViewContainer : UIViewController {
    @IBOutlet weak var containerView: UIView!
    @IBOutlet weak var notifyOnPersonalProgramSwitch: UISwitch!
    
    var detailsVC: PersonalProgramNotificationViewController?
    
    var user: User?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.user = Context.Instance.userManager.user
    }
    
    override func viewWillAppear(_ animated: Bool) {
        let user = Context.Instance.userManager.user
        // Show settings in simulator. but hide them on device if user said so
        #if !((arch(i386) || arch(x86_64)) && os(iOS))
            if (user.NotificationToken == nil) {
                self.notifyOnPersonalProgramSwitch.isEnabled = false
                self.notifyOnPersonalProgramSwitch.isOn = false
            }
        #endif

        if self.notifyOnPersonalProgramSwitch.isEnabled {
            self.notifyOnPersonalProgramSwitch.isOn = Bool.init(exactly: NSNumber(value: user.NotifyOnPersonalProgram))!
        }

        self.determineProgramListsViewVisibility()
    }

    @IBAction func notifyOnPersonalProgramSwitchValueChanged(_ sender: Any) {
        self.determineProgramListsViewVisibility()
        self.registerChangesOnServerIfNeeded()
    }
    
    func determineProgramListsViewVisibility() {
        if self.notifyOnPersonalProgramSwitch.isOn {
            self.containerView.subviews[0].isHidden = false
        } else {
            self.containerView.subviews[0].isHidden = true
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "details_embed" {
            self.detailsVC = segue.destination as? PersonalProgramNotificationViewController
            self.detailsVC?.personalProgramNotificationContainerVC = self
        }
    }
    
    func registerChangesOnServerIfNeeded() {
        // If the public notifications are turned off
        if self.notifyOnPersonalProgramSwitch.isOn == false && self.user?.NotifyOnPersonalProgram == 1 {
            Context.Instance.userManager.user.NotifyOnPersonalProgram = 0
            Context.Instance.userManager.registerUser()
        } else if self.notifyOnPersonalProgramSwitch.isOn == true {
            var shouldRegister = false
            if self.user?.NotifyOnPersonalProgram == 0 {
                Context.Instance.userManager.user.NotifyOnPersonalProgram = 1
                shouldRegister = true
            }
            if shouldRegister {
                Context.Instance.userManager.registerUser()
            }
        }
    }
}

class PersonalProgramNotificationViewController : UITableViewController {
    
    public var personalProgramNotificationContainerVC: PersonalProgramNotificationViewContainer?
    
    private var filteredProgramInfos: [String: ProgramInfo]?
    private var filteredProgramInfoCount: Int = 0
    private var user: User?
    
    public var dirtyDict: [String: Bool] {
        var result: [String: Bool] = [:]
        for cellIdx in 0..<filteredProgramInfoCount {
            let indexPath = IndexPath(row: cellIdx, section: 0)
            let cell = tableView.cellForRow(at: indexPath) as! PersonalProgramNotificationViewCell
            let previousValue = !(user?.NotificationExcludedPersonalProgramsObject[cell.ProgramId!] ?? false)
            // Value changed, add to dirty entries
            if cell.NotifyOnProgramStart.isOn != previousValue {
                result[cell.ProgramId!] = !(cell.NotifyOnProgramStart.isOn)
            }
        }
        return result;
    }
    
    public var notificationExcludedPersonalPrograms:[String: Bool] = [: ]
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        user = Context.Instance.userManager.user
        firstly {
            Context.Instance.programInfoDirectoryManager.pullData()
            }.done { programInfoDirectory in
                let programInfoDirectory = programInfoDirectory
                self.filterProgramInfos(programInfoDirectory)
                
                self.tableView.reloadData()
            }.ensure {
                Context.Instance.programInfoDirectoryManager.registerEventListener(listenerObject: self)
            }.catch { _ in
        }
    }
    
    func filterProgramInfos(_ programInfoDirectory: ProgramInfoDirectory?) {
        self.filteredProgramInfos = programInfoDirectory?.ProgramInfos.filter({ (key: String, value: ProgramInfo) -> Bool in
            return value.Feed == "Personal" ? true : false
        })
    }
    
    @IBAction func programNotificationSettingsChanged(_ sender: Any) {
        let rowNumber = (sender as! UISwitch).tag
        let newValue = !(sender as! UISwitch).isOn
        
        let programId = Array((filteredProgramInfos?.keys)!)[rowNumber]
        
        if (user?.NotificationExcludedPersonalProgramsObject[programId] ?? false) != newValue {
            user?.NotificationExcludedPersonalProgramsObject[programId] = newValue
            Context.Instance.userManager.registerUser()
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        self.tableView.reloadData()
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (self.filteredProgramInfos == nil) {
            self.filteredProgramInfoCount = 0
        } else {
            self.filteredProgramInfoCount = Array((self.filteredProgramInfos?.keys)!).count
        }
        return self.filteredProgramInfoCount
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellIdentifier = "personalProgramNotifitationSettingsCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? PersonalProgramNotificationViewCell else {
            fatalError("The dequeued cell is not an instance of PersonalProgramNotificationViewCell.")
        }
        
        let programId = Array((self.filteredProgramInfos?.keys)!)[indexPath.row]
        let programInfo = self.filteredProgramInfos?[programId]
        
        cell.ProgramId = programId
        cell.ProgramNameLabel.text = programInfo?.Title
        cell.ProgramIcon.image = UIImage(data: programInfo?.thumbnailImageData ?? ProgramInfo.defaultThumbnailImageData)
        cell.ProgramIcon.setNeedsLayout()
        // If excluded == 1 -> turn off notifications (hence switch)
        cell.NotifyOnProgramStart.isOn = !(user?.NotificationExcludedPersonalProgramsObject[programId] ?? false)
        cell.NotifyOnProgramStart.tag = indexPath.row // Used in value changed callback

        return cell
    }
}

extension PersonalProgramNotificationViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        let programInfoDirectory = data as? ProgramInfoDirectory
        self.filterProgramInfos(programInfoDirectory)
        
        self.tableView.reloadData()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

class PersonalProgramNotificationViewCell : UITableViewCell {
    var ProgramId: String?
    @IBOutlet weak var ProgramIcon: UIImageView!
    @IBOutlet weak var ProgramNameLabel: UILabel!
    @IBOutlet weak var NotifyOnProgramStart: UISwitch!
}

