//
//  ArchiveViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/23/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import PromiseKit

class ArchiveContainerViewController : PlayerViewController {
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
}

class ArchiveViewController : UITableViewController {
    private var programInfoDirectory: ProgramInfoDirectory?
    private var archiveData: ArchiveData?

    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        NotificationCenter.default.addObserver(self, selector: #selector(rotated), name: NSNotification.Name.UIDeviceOrientationDidChange, object: nil)
        
        self.fullRedraw()
    }
    
    // FIXES A BUG THAT LAYOUTS TABLE WRONG IN IOS 10
    func fixTableViewInsets() {
        let zContentInsets = UIEdgeInsets.zero
        tableView.contentInset = zContentInsets
        tableView.scrollIndicatorInsets = zContentInsets
    }
    
    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        fixTableViewInsets()
    }
    // END BUG FIX

    @objc func rotated() {
        self.fullRedraw()
    }

    private func fullRedraw() {
        firstly {
            Context.Instance.archiveManager.pullData()
        }.done { archiveData in
            self.programInfoDirectory = try! hang(Context.Instance.programInfoDirectoryManager.pullData())
            self.archiveData = archiveData
            self.tableView?.reloadData()
        }.ensure {
            Context.Instance.archiveManager.registerEventListener(listenerObject: self)
        }.catch { _ in
        }
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        if (self.archiveData?.archiveURLDirectory == nil) {
            return 0
        }
        return (self.archiveData?.archiveURLDirectory?.count)!
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellIdentifier = "programArchiveCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? ArchiveProgramTableViewCell else {
            fatalError("The dequeued cell is not an instance of ArchiveProgramTableViewCell.")
        }
        
        let programId = Array((self.archiveData?.archiveURLDirectory?.keys)!)[indexPath.row]
        
        cell.ProgramId = programId
        cell.ProgramNameLabel.text = self.programInfoDirectory?.ProgramInfos[programId]?.Title
        cell.ProgramIcon.image = UIImage(data: self.programInfoDirectory?.ProgramInfos[programId]?.thumbnailImageData ?? ProgramInfo.defaultThumbnailImageData)
        cell.ProgramIcon.setNeedsLayout()
        
        return cell
    }
    
    private var selectedProgramId: String? = nil
    private var selectedProgramTitle: String? = nil
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let currentCell = tableView.cellForRow(at: indexPath)! as! ArchiveProgramTableViewCell
        selectedProgramId = currentCell.ProgramId
        selectedProgramTitle = currentCell.ProgramNameLabel.text

        performSegue(withIdentifier: "archiveProgramCardsSegue", sender: self)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "archiveProgramCardsSegue") {
            let programArchiveController = segue.destination as! ProgramArchiveContainerViewController

            programArchiveController.programId = self.selectedProgramId
            programArchiveController.programTitle = self.selectedProgramTitle
        }
    }    
}

extension ArchiveViewController : ModelCommunicator {
    func modelUpdated(data: Any?) {
        self.archiveData = data as? ArchiveData
        self.tableView?.reloadData()
    }
    
    func hashCode() -> Int {
        return ObjectIdentifier(self).hashValue
    }
}

class ArchiveProgramTableViewCell : UITableViewCell {
    var ProgramId: String?
    @IBOutlet weak var ProgramIcon: UIImageView!
    @IBOutlet weak var ProgramNameLabel: UILabel!
    
    
}

