//
//  PublicProgramNotificationViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/22/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import PromiseKit

class PublicProgramNotificationViewContainer : UIViewController {
    @IBOutlet weak var containerView: UIView!
    @IBOutlet weak var notifyOnPublicProgramSwitch: UISwitch!
    
    var detailsVC: PublicProgramNotificationViewController?
    
    var user: User?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)

        self.user = Context.Instance.userManager.user
    }
    
    @IBAction func notifyOnPublicProgramSwitchValueChanged(_ sender: Any) {
        if self.notifyOnPublicProgramSwitch.isOn {
            self.containerView.subviews[0].isHidden = false
        } else {
            self.containerView.subviews[0].isHidden = true
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "details_embed" {
            self.detailsVC = segue.destination as? PublicProgramNotificationViewController
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        // If the public notifications are turned off
        if self.notifyOnPublicProgramSwitch.isOn == false && self.user?.NotifyOnPublicProgram == 1 {
            Context.Instance.userManager.user.NotifyOnPublicProgram = 0
            Context.Instance.userManager.registerUser()
        } else if self.notifyOnPublicProgramSwitch.isOn == true {
            var shouldRegister = false
            if self.user?.NotifyOnPublicProgram == 0 {
                Context.Instance.userManager.user.NotifyOnPublicProgram = 1
                shouldRegister = true
            }
            
            let programNotificationChangedValues = self.detailsVC?.dirtyDict
            
            if programNotificationChangedValues != nil && programNotificationChangedValues!.count > 0 {
                for (key, value) in programNotificationChangedValues! {
                    user?.NotificationExcludedPublicProgramsObject[key] = value
                }
                shouldRegister = true
            }
            
            if shouldRegister {
                Context.Instance.userManager.registerUser()
            }
        }
    }
}

class PublicProgramNotificationViewController : UITableViewController {

    private var filteredProgramInfos: [String: ProgramInfo]?
    private var filteredProgramInfoCount: Int = 0
    private var user: User?
    
    public var dirtyDict: [String: Bool] {
        var result: [String: Bool] = [:]
        for cellIdx in 0..<filteredProgramInfoCount {
            let indexPath = IndexPath(row: cellIdx, section: 0)
            let cell = tableView.cellForRow(at: indexPath) as! PublicProgramNotificationViewCell
            let previousValue = !(user?.NotificationExcludedPublicProgramsObject[cell.ProgramId!] ?? false)
            // Value changed, add to dirty entries
            if cell.NotifyOnProgramStart.isOn != previousValue {
                result[cell.ProgramId!] = !(cell.NotifyOnProgramStart.isOn)
            }
        }
        return result;
    }
    
    public var notificationExcludedPublicPrograms:[String: Bool] = [: ]
    
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
            return value.Feed == "Public" ? true : false
        })
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
        let cellIdentifier = "publicProgramNotifitationSettingsCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? PublicProgramNotificationViewCell else {
            fatalError("The dequeued cell is not an instance of PublicProgramNotificationViewCell.")
        }

        let programId = Array((self.filteredProgramInfos?.keys)!)[indexPath.row]
        let programInfo = self.filteredProgramInfos?[programId]
        
        cell.ProgramId = programId
        cell.ProgramNameLabel.text = programInfo?.Title
        cell.ProgramIcon.image = UIImage(data: programInfo?.thumbnailImageData ?? ProgramInfo.defaultThumbnailImageData)
        cell.ProgramIcon.setNeedsLayout()
        // If excluded == 1 -> turn off notifications (hence switch)
        cell.NotifyOnProgramStart.isOn = !(user?.NotificationExcludedPublicProgramsObject[programId] ?? false)
        
        return cell
    }
}

extension PublicProgramNotificationViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        let programInfoDirectory = data as? ProgramInfoDirectory
        self.filterProgramInfos(programInfoDirectory)
        
        self.tableView.reloadData()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

class PublicProgramNotificationViewCell : UITableViewCell {
    var ProgramId: String?
    @IBOutlet weak var ProgramIcon: UIImageView!
    @IBOutlet weak var ProgramNameLabel: UILabel!
    @IBOutlet weak var NotifyOnProgramStart: UISwitch!
}
