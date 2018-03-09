//
//  ProgramArchiveViewController.swift
//  raa-ios-player
//
//  Created by Hamid on 2/23/18.
//  Copyright © 2018 Auto-asaad. All rights reserved.
//

import Foundation
import UIKit
import PromiseKit

class ProgramArchiveContainerViewController : PlayerViewController {
    public var programId: String?
    public var programTitle: String?
    
    @IBOutlet weak var containerView: UIView!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.navigationItem.title = programTitle
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "programArchiveTableDisplay") {
            let programArchiveController = segue.destination as! ProgramArchiveViewController
            programArchiveController.programId = self.programId
        } else if (segue.identifier == "archivePodcastSegue") {
            let programArchivePodcastDescriptionController = segue.destination as! ProgramArchivePodcastDescriptionViewController
            programArchivePodcastDescriptionController.programId = self.programId
        }
    }
    
    @IBAction func podcastDescriptionClicked(_ sender: Any) {
        performSegue(withIdentifier: "archivePodcastSegue", sender: self)
    }
}

class ProgramArchiveViewController : UITableViewController {
    public var programId: String?
    
    private var dateFormatter: DateFormatter = DateFormatter()
    
    private var programArchive: [ArchiveEntry]?
    
    struct Defaults {
        public static let CELL_HEIGHT: CGFloat = 150
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)

        dateFormatter.dateFormat = "yyyy-MM-dd"
        self.dateFormatter.locale = Locale(identifier: "en_US")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        programArchive = Context.Instance.archiveManager.loadProgramArchiveSync(programId!)
        self.tableView.reloadData()
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
}

extension ProgramArchiveViewController : ProgramArchiveEntryDelegate {
    func onPlayButtonClicked(_ requestedArchiveEntry: ArchiveEntry?) {
        Context.Instance.playbackManager.playArchiveEntry(requestedArchiveEntry)
    }
}

extension ProgramArchiveViewController {
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return Defaults.CELL_HEIGHT
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return programArchive?.count ?? 0
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        // Table view cells are reused and should be dequeued using a cell identifier.
        let cellIdentifier = "archiveEntryCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? ProgramArchiveEntryTableViewCell else {
            fatalError("The dequeued cell is not an instance of ProgramArchiveEntryTableViewCell.")
        }
        
        let row = indexPath.row
        
        cell.card?.archiveEntry = self.programArchive![row]
        
        cell.card?.programTitle = self.programArchive![row].Program?.Subtitle ?? "عنوان این برنامه‌ را پیدا نکردیم!"
        cell.card?.programSubtitle = ""
        
        let date = self.dateFormatter.date(from: self.programArchive![row].ReleaseDateString!)
        if (date != nil) {
            cell.card?.timeValue1 = Utils.getPersianLocaleDateString(date!)
        } else {
            cell.card?.timeValue1 = ""
        }
        
        
        cell.card?.backgroundImage = UIImage(data: ProgramInfo.defaultBannerImageData)
        cell.card?.setNeedsDisplay()
        
        cell.card?.archiveEntryDelegate = self
        return cell
    }
}

class ProgramArchivePodcastDescriptionViewController : UIViewController {
    var programId: String?
    
    @IBOutlet weak var PodcastURLTextView: UITextView!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        if (programId != nil) {
            self.PodcastURLTextView.text = Context.RSS_URL_PREFIX + "/" + programId! + ".xml"
        }
    }
    
    @IBAction func okButtonClicked(_ sender: Any) {
        self.dismiss(animated: true)
    }
}

